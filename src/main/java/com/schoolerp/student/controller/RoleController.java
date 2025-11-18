package com.schoolerp.student.controller;

import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.RoleCreateRequest;
import com.schoolerp.student.dto.RoleResponse;
import com.schoolerp.student.dto.RoleUpdateRequest;
import com.schoolerp.student.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staffservice/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @PostMapping("/saveRole")
    public StandardResponse<RoleResponse> create(@Valid @RequestBody RoleCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/getRoles")
    public StandardResponse<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(page, size);
    }

    @GetMapping("/getRoleById/{id}")
    public StandardResponse<RoleResponse> get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/updateRole/{id}")
    public StandardResponse<RoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest req
    ) {
        return service.update(id, req);
    }

    @DeleteMapping("/deleteRole/{id}")
    public StandardResponse<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
