package com.schoolerp.student.controller;

import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.DepartmentCreateRequest;
import com.schoolerp.student.dto.DepartmentResponse;
import com.schoolerp.student.dto.DepartmentUpdateRequest;
import com.schoolerp.student.dto.PageResponse;
import com.schoolerp.student.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staffservice/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping
    public StandardResponse<?> create(@Valid @RequestBody DepartmentCreateRequest req) {
        return service.create(req);
    }

    // -------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------
    @GetMapping
    public StandardResponse<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.search(q, page, size);
    }

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    @GetMapping("/{id}")
    public StandardResponse<?> get(@PathVariable Long id) {
        return service.get(id);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping("/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest req
    ) {
        return service.update(id, req);
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------
    @DeleteMapping("/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}

