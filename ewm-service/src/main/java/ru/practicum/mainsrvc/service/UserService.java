package ru.practicum.mainsrvc.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainsrvc.dto.UserShortDto;
import ru.practicum.mainsrvc.entity.User;
import ru.practicum.mainsrvc.exception.EntityNotFoundException;
import ru.practicum.mainsrvc.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserShortDto> getAllUsers(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Поле не может быть отрицательным");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Значение должно быть не более 1000 символов");
        }

        var pageRequest = PageRequest.of(from, size, Sort.unsorted());
        var users = userRepository.findAll(pageRequest).getContent();
        return users.stream()
                .map(this::toUserShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserShortDto activateUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));
        u.setActive(true);
        u = userRepository.save(u);
        return toUserShortDto(u);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь не найден: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserShortDto toUserShortDto(User u) {
        return new UserShortDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                Objects.requireNonNullElse(u.getActive(), false)
        );
    }
}
