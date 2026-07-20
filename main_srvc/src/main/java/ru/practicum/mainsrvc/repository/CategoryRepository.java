package ru.practicum.mainsrvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainsrvc.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
