package com.schoolerp.staff.controller;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.dto.StaffCreateRequest;
import com.schoolerp.staff.dto.StaffUpdateRequest;
import com.schoolerp.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/getAllStaff")
    public StandardResponse<?> getAllStaff(

    ) {
        return service.getAllStaff();
    }


    @GetMapping("/getAllTeachers")
    public StandardResponse<?> getAllTeachers(

    ) {
        return service.getAllTeachers();
    }
    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    @GetMapping("/getStaffById/{id}")
    public StandardResponse<?> get(@PathVariable Long id) {
        return service.get(id);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping("/updateStaff/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest req
    ) {
        return service.update(id, req);
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------
    @DeleteMapping("/deleteStaff/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
