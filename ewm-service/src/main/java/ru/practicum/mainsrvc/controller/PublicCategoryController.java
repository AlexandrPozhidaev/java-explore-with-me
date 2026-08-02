package ru.practicum.mainsrvc.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CategoryDto;
import ru.practicum.mainsrvc.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    public PublicCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0 || size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Некорректные параметры пагинации: from >= 0, 0 < size <= 1000");
        }

        PageRequest pageRequest = PageRequest.of(from, size, Sort.unsorted());
        Page<ru.practicum.mainsrvc.entity.Category> page = categoryService.getCategoriesPage(pageRequest);

        List<CategoryDto> dtoList = page.getContent().stream()
                .map(this::toCategoryDto)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    private CategoryDto toCategoryDto(ru.practicum.mainsrvc.entity.Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        return dto;
    }
}