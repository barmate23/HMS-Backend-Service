package com.schoolerp.staff.controller;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.RoleCreateRequest;
import com.schoolerp.staff.dto.RoleResponse;
import com.schoolerp.staff.dto.RoleUpdateRequest;
import com.schoolerp.staff.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
