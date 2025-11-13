package com.schoolerp.student.controller;


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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse create(@Valid @RequestBody StaffCreateRequest req) {
        return service.create(req);
    }

    @GetMapping
    public PageResponse<StaffResponse> search(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) StaffStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.search(deptId, status, q, page, size);
    }

    @GetMapping("/{id}")
    public StaffResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public StaffResponse update(@PathVariable Long id, @Valid @RequestBody StaffUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
