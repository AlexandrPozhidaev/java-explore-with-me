package ru.practicum.mainsrvc.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainsrvc.dto.UserFullDto;
import ru.practicum.mainsrvc.dto.UserShortDto;
import ru.practicum.mainsrvc.entity.User;
import ru.practicum.mainsrvc.exception.ConflictException;
import ru.practicum.mainsrvc.exception.NotFoundException;
import ru.practicum.mainsrvc.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserShortDto> getAllUsers(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        var page = userRepository.findAll(pageable);
        return page.getContent().stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<User> users = userRepository.findAllById(ids);

        if (users.size() != ids.size()) {
            List<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toList());
            List<Long> notFound = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new NotFoundException("Пользователи не найдены: " + notFound);
        }

        return users.stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserShortDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return toShortDto(user);
    }

    @Transactional
    public UserFullDto createUser(UserFullDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setActive(dto.getActive() != null ? dto.getActive() : true);

        user = userRepository.save(user);
        return toFullDto(user);
    }

    @Transactional
    public UserShortDto activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        user.setActive(true);
        user = userRepository.save(user);
        return toShortDto(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    private UserShortDto toShortDto(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        return dto;
    }

    private UserFullDto toFullDto(User user) {
        UserFullDto dto = new UserFullDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        return dto;
    }
}