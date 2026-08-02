package ru.practicum.mainsrvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CompilationDto;
import ru.practicum.mainsrvc.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    public PublicCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getPublicCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' должен быть >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Параметр 'size' должен быть больше 0");
        }
        if (size > 1000) {
            size = 1000;
        }

        List<CompilationDto> compilations = compilationService.getPublicCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        CompilationDto compilation = compilationService.getCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}