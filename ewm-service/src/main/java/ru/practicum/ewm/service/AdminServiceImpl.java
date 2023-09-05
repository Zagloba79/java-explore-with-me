package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entity.*;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.StateActionForAdmin;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.PENDING;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUser) {
        Optional<User> userFromRep = userRepository.findByName(newUser.getName());
        if (userFromRep.isPresent()) {
            throw new ObjectAlreadyExistsException("Пользователь с таким именем уже есть");
        }
        User user = UserMapper.toUser(newUser);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        if (ids == null) {
            users = userRepository.findAll(pageable).toList();
        } else {
            users = userRepository.findAllByIdIn(ids, pageable);
        }
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(UserMapper::toUserDto).collect(toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (newCategoryDto.getName().isBlank()) {
            throw new ValidationException("Название категории не может быть пустым");
        }
        Optional<Category> categoryFromRep = categoryRepository.findByName(newCategoryDto.getName());
        if (categoryFromRep.isPresent()) {
            //return CategoryMapper.toCategoryDto(categoryFromRep.get());
            throw new DataIsNotCorrectException("Категория уже есть");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Нет данной категории"));
        Optional<Category> categoryFromRep = categoryRepository.findByName(categoryDto.getName());
        if (categoryFromRep.isPresent() && !categoryFromRep.get().getId().equals(categoryId)) {
            throw new ObjectAlreadyExistsException("Категория с таким названием уже есть");
        }
        if (categoryDto.getName().length() > 50) {
            throw new ValidationException("Очень длинное название");
        }
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ObjectContainsDataException("STOP! Категории с событиями не удаляем.");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getTitle().isBlank()) {
            throw new ValidationException("Пустое название");
        }
        if (newCompilationDto.getTitle().length() > 50) {
            throw new ValidationException("Очень длинное название");
        }
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        Set<Event> events = findEvents(newCompilationDto.getEvents());
        if (newCompilationDto.getEvents() != null) {
            compilation.setEvents(events);
        }
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        compilation.setPinned(updateCompilationRequest.isPinned());
        if (updateCompilationRequest.getTitle() != null && (!updateCompilationRequest.getTitle().isBlank())) {
            if (updateCompilationRequest.getTitle().length() >= 51) {
                throw new ValidationException("Очень длинное название");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(findEvents(updateCompilationRequest.getEvents()));
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        boolean isExist = compilationRepository.existsById(compId);
        if (!isExist) {
            throw new ObjectNotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new OperationIsNotSupportedException("Поздняк метаться");
        }
        if (eventDto.getAnnotation() != null && !eventDto.getTitle().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (LocalDateTime.parse(eventDto.getEventDate(), FORMATTER).isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Поздняк метаться");
            } else {
                event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
            }
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            if (!event.getState().equals(PENDING)) {
                throw new DataIsNotCorrectException("Можно изменять события только в статусе 'Pending'");
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getAllEvents(List<Long> users, List<State> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Даты попутаны");
        }
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByParam(users, states, categories, rangeStart, rangeEnd, pageable);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(toList());
    }

    private Set<Event> findEvents(List<Long> eventIds) {
        if (eventIds == null) {
            return Collections.emptySet();
        }
        return eventRepository.findAllByIdIn(eventIds);
    }

}