package ru.practicum.mainsrvc.controller;

import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainsrvc.dto.CompilationDto;
import ru.practicum.mainsrvc.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;

    public PublicCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getPublicCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(compilationService.getPublicCompilations(pinned, from, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long id) {
        return ResponseEntity.ok(compilationService.getCompilationById(id));
    }
}
