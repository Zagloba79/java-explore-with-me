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
import java.util.*;

import static java.util.stream.Collectors.toList;
import static ru.practicum.ewm.enums.State.PENDING;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUser) {
        User user = UserMapper.toUser(newUser);
        userValidate(user);
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
        Category category = CategoryMapper.toCategory(newCategoryDto);
        categoryValidate(category);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Нет данной категории"));
        category.setName(categoryDto.getName());
        categoryValidate(category);
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
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        Set<Event> events = findEvents(newCompilationDto.getEvents());
        if (!events.isEmpty()) {
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Compilation not found"));
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (!updateCompilationRequest.getTitle().isBlank()) {
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
            throw new OperationIsNotSupported("Поздняк метаться");
        }
        if (eventDto.getAnnotation() != null && !eventDto.getTitle().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategoryId())
                    .orElseThrow(() -> new ObjectNotFoundException("Category not found")));
        }
        if (!eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new OperationIsNotSupported("Поздняк метаться");
            } else {
                event.setEventDate(eventDto.getEventDate());
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
                throw new OperationIsNotSupported("Можно изменять события только в статусе 'Pending'");
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        if (!eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getAllEvents(List<Long> users, List<State> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findEventsByParamsForAdmin(users, states, categories, rangeStart, rangeEnd, pageable);
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

    private void userValidate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new ValidationException("Некорректный e-mail пользователя");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Некорректный логин пользователя");
        }
    }

    private void categoryValidate(Category category) {
        if (category.getName().isBlank()) {
            throw new ValidationException("Название категории некорректно");
        }
        for (Category categoryFromRep : categoryRepository.findAll()) {
            if (!categoryFromRep.getName().equals(category.getName())) {
                throw new ObjectAlreadyExistsException("The category already exists");
            }
        }
    }
}