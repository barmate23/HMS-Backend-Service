package com.schoolerp.student.controller;


import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.constants.StaffStatus;
import com.schoolerp.student.dto.PageResponse;
import com.schoolerp.student.dto.StaffCreateRequest;
import com.schoolerp.student.dto.StaffResponse;
import com.schoolerp.student.dto.StaffUpdateRequest;
import com.schoolerp.student.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staffservice")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService service;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping("/saveStaff")
    public StandardResponse<?> create(@Valid @RequestBody StaffCreateRequest req) {
        return service.create(req);
    }

    // -------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------
    @GetMapping("/getStaffByFilter")
    public StandardResponse<?> search(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) StaffStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.search(deptId, status, q, page, size);
    }

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    @GetMapping("getStaffById/{id}")
    public StandardResponse<?> get(@PathVariable Long id) {
        return service.get(id);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping("updateStaff/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest req
    ) {
        return service.update(id, req);
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------
    @DeleteMapping("deleteStaff/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
