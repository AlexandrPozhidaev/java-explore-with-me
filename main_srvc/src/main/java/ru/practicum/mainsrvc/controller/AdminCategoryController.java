package ru.practicum.mainsrvc.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CategoryDto;
import ru.practicum.mainsrvc.dto.NewCategoryDto;
import ru.practicum.mainsrvc.dto.UpdateCategoryDto;
import ru.practicum.mainsrvc.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody NewCategoryDto dto) {
        return ResponseEntity.status(201).body(categoryService.createCategory(dto));
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody UpdateCategoryDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(catId, dto));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }
}
