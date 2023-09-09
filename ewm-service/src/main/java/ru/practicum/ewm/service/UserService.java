package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsersAdmin(List<Long> ids, Integer from, Integer size);

    UserDto createUserAdmin(NewUserRequest newUserRequest);

    void deleteUserAdmin(Long userId);
}
