package com.schoolerp.staff.controller;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DesignationCreateRequest;
import com.schoolerp.staff.dto.DesignationUpdateRequest;
import com.schoolerp.staff.service.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staffservice/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService service;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping("/saveDesignation")
    public StandardResponse<?> create(@Valid @RequestBody DesignationCreateRequest req) {
        return service.create(req);
    }

    // -------------------------------------------------------------
    // LIST BY DEPARTMENT
    // -------------------------------------------------------------
    @GetMapping("/getDesignationByFilter")
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
    @GetMapping("/getDesignationById/{id}")
    public StandardResponse<?> get(@PathVariable Long id) {
        return service.get(id);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping("/updateDesignation/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody DesignationUpdateRequest req
    ) {
        return service.update(id, req);
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------
    @DeleteMapping("/deleteDesignation/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
