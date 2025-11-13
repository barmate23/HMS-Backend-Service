package com.schoolerp.student.controller;


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
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DesignationResponse create(@Valid @RequestBody DesignationCreateRequest req) {
        return service.create(req);
    }

    @GetMapping
    public PageResponse<DesignationResponse> listByDepartment(
            @RequestParam Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.listByDepartment(departmentId, page, size);
    }

    @GetMapping("/{id}")
    public DesignationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public DesignationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DesignationUpdateRequest req
    ) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
