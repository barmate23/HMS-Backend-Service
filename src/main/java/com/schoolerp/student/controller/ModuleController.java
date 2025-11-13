package com.schoolerp.student.controller;

import com.schoolerp.staff.dto.module.*;
import com.schoolerp.staff.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse create(@Valid @RequestBody ModuleCreateRequest req) {
        return service.create(req);
    }

    @GetMapping
    public Page<ModuleResponse> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @PutMapping("/{id}")
    public ModuleResponse update(@PathVariable Long id, @Valid @RequestBody ModuleUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
