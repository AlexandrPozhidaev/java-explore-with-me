package ru.practicum.mainsrvc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainsrvc.dto.CategoryDto;
import ru.practicum.mainsrvc.dto.NewCategoryDto;
import ru.practicum.mainsrvc.dto.UpdateCategoryDto;
import ru.practicum.mainsrvc.entity.Category;
import ru.practicum.mainsrvc.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Категория '" + dto.getName() + "' уже существует");
        }
        Category c = new Category();
        c.setName(dto.getName());
        c = categoryRepository.save(c);
        return toCategoryDto(c);
    }

    @Transactional
    public CategoryDto updateCategory(Long catId, UpdateCategoryDto dto) {
        Category c = categoryRepository.findById(catId)
                .orElseThrow(() -> new org.springframework.dao.EmptyResultDataAccessException("Категория не найдена", 1));

        if (dto.getName() != null && !dto.getName().equals(c.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("Категория '" + dto.getName() + "' уже существует");
            }
            c.setName(dto.getName());
        }

        c = categoryRepository.save(c);
        return toCategoryDto(c);
    }

    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new org.springframework.dao.EmptyResultDataAccessException("Категория не найдена", 1);
        }
        categoryRepository.deleteById(catId);
    }

    private CategoryDto toCategoryDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        return dto;
    }
}
