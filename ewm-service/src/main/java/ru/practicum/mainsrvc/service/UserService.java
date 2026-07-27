package ru.practicum.mainsrvc.service;

import org.springframework.data.domain.PageRequest;
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
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserFullDto createUser(UserFullDto dto) {
        // Валидация имени
        String name = dto.getName();
        if (name == null || name.length() < 2 || name.length() > 250) {
            throw new IllegalArgumentException("Длина имени должна быть от 2 до 250 символов");
        }

        String email = dto.getEmail();
        if (email == null || email.length() < 6 || email.length() > 254) {
            throw new IllegalArgumentException("Длина email должна быть от 6 до 254 символов");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setActive(false);

        User saved = userRepository.save(user);

        return new UserFullDto(saved.getId(), saved.getName(), saved.getEmail(), saved.getActive());
    }

    public List<UserShortDto> getAllUsers(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Смещение (from) не может быть отрицательным");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Размер страницы (size) должен быть от 1 до 1000");
        }

        // Используем сортировку по ID, чтобы порядок записей был стабильным
        var pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        var usersPage = userRepository.findAll(pageRequest);
        var users = usersPage.getContent();

        return users.stream()
                .map(this::toUserShortDto)
                .collect(Collectors.toList());
    }

    public UserShortDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserShortDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    @Transactional
    public UserShortDto activateUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        u.setActive(true);
        u = userRepository.save(u);
        return toUserShortDto(u);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserShortDto toUserShortDto(User u) {
        return new UserShortDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                Boolean.TRUE.equals(u.getActive())
        );
    }
}
