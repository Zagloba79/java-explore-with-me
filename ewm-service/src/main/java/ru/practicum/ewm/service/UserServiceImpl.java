package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.exception.ObjectAlreadyExistsException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    @Transactional
    public UserDto createUserAdmin(NewUserRequest newUser) {
        Optional<User> userFromRep = userRepository.findByName(newUser.getName());
        if (userFromRep.isPresent()) {
            throw new ObjectAlreadyExistsException("Пользователь с таким именем уже есть");
        }
        User user = UserMapper.toUser(newUser);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsersAdmin(List<Long> ids, Integer from, Integer size) {
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
    public void deleteUserAdmin(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        userRepository.deleteById(userId);
    }
}