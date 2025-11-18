package com.schoolerp.student.controller;


import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.ModuleWithSubmodulesResponse;
import com.schoolerp.student.dto.PermissionRequest;
import com.schoolerp.student.dto.PermissionResponse;
import com.schoolerp.student.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staffservice/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService service;

    /**
     * Get all permissions for a given role
     */
    @GetMapping("/getAllPermissions")
    public StandardResponse<?> listByRole(
            @RequestParam Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.listByRole(roleId, page, size);
    }

    /**
     * Save/Update all permissions for a role
     */
    @PostMapping("/savePermisions/{roleId}")
    public StandardResponse<Void> saveAll(@PathVariable Long roleId, @RequestBody List<PermissionRequest> list) {
        return service.saveAll(roleId, list);
    }

    /**
     * Get all modules with their submodules
     */
    @GetMapping("/getModuleList")
    public StandardResponse<List<ModuleWithSubmodulesResponse>> getModulesWithSubModules() {
        return service.getAllModulesWithSubmodules();
    }
}
