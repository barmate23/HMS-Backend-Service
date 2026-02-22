package com.schoolerp.staff.controller;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.ModuleWithSubmodulesResponse;
import com.schoolerp.staff.dto.PermissionRequest;
import com.schoolerp.staff.dto.UserPermissionResponse;
import com.schoolerp.staff.service.PermissionService;
import lombok.RequiredArgsConstructor;
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
    @PostMapping("/savePermissions/{roleId}")
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

    /**
     * Get all modules with their submodules
     */
    @GetMapping("/getUserPermission")
    public StandardResponse<List<UserPermissionResponse>> getUserPermission() {
        return service.getUserPermission();
    }
}