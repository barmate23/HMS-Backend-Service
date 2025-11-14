package com.schoolerp.student.controller;


import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.DesignationCreateRequest;
import com.schoolerp.student.dto.DesignationResponse;
import com.schoolerp.student.dto.DesignationUpdateRequest;
import com.schoolerp.student.dto.PageResponse;
import com.schoolerp.student.service.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staffservice/v1/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService service;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping
    public StandardResponse<?> create(@Valid @RequestBody DesignationCreateRequest req) {
        return service.create(req);
    }

    // -------------------------------------------------------------
    // LIST BY DEPARTMENT
    // -------------------------------------------------------------
    @GetMapping
    public StandardResponse<?> listByDepartment(
            @RequestParam Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.listByDepartment(departmentId, page, size);
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
            @Valid @RequestBody DesignationUpdateRequest req
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
