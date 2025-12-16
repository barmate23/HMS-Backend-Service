package com.schoolerp.staff.controller;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DepartmentCreateRequest;
import com.schoolerp.staff.dto.DepartmentUpdateRequest;
import com.schoolerp.staff.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staffservice/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping("/saveDepartment")
    public StandardResponse<?> create(@Valid @RequestBody DepartmentCreateRequest req) {
        return service.create(req);
    }

    // -------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------
    @GetMapping("/getDepartmentByFilter")
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
    @GetMapping("/getDeptById/{id}")
    public StandardResponse<?> get(@PathVariable Long id) {
        return service.get(id);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping("/updateDepartment/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest req
    ) {
        return service.update(id, req);
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------
    @DeleteMapping("/deleteDepartment/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}

