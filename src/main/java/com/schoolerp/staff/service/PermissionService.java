package com.schoolerp.staff.service;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.ModuleWithSubmodulesResponse;
import com.schoolerp.staff.dto.PermissionRequest;
import com.schoolerp.staff.dto.PermissionResponse;
import com.schoolerp.staff.dto.SubModuleResponse;
import com.schoolerp.staff.entity.Modules;
import com.schoolerp.staff.entity.Permission;
import com.schoolerp.staff.entity.Role;
import com.schoolerp.staff.entity.SubModule;
import com.schoolerp.staff.repository.ModulesRepository;
import com.schoolerp.staff.repository.PermissionRepository;
import com.schoolerp.staff.repository.RoleRepository;
import com.schoolerp.staff.repository.SubModuleRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ModulesRepository moduleRepo;
    private final SubModuleRepository subModuleRepo;

    /**
     * List permissions of a specific role
     */
    public StandardResponse<Page<PermissionResponse>> listByRole(Long roleId, int page, int size) {
        Page<Permission> p = permissionRepository.findByRoleId(
                roleId, PageRequest.of(page, size, Sort.by("id"))
        );

        Page<PermissionResponse> mapped = p.map(this::toResp);

        return StandardResponse.success(
                mapped,
                "Permissions fetched successfully",
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(p.getTotalElements())
                        .currentPage(page)
                        .pageSize(size)
                        .totalPages(p.getTotalPages())
                        .operation("LIST_ROLE_PERMISSIONS")
                        .build()
        );
    }

    /**
     * Save or update all permissions of a role
     */
    public StandardResponse<Void> saveAll(Long roleId, List<PermissionRequest> list) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        // Delete previous permissions
        permissionRepository.deleteByRoleId(roleId);

        for (PermissionRequest req : list) {
            SubModule subModule = subModuleRepo.findById(req.subModuleId())
                    .orElseThrow(() -> new NotFoundException("Submodule not found"));

            Permission p = Permission.builder()
                    .role(role)
                    .subModule(subModule)
                    .canView(req.canView())
                    .canCreate(req.canCreate())
                    .canEdit(req.canEdit())
                    .canDelete(req.canDelete())
                    .build();

            permissionRepository.save(p);
        }

        return StandardResponse.success("Permissions updated successfully");
    }

    /**
     * Get all modules with submodules
     */
    public StandardResponse<List<ModuleWithSubmodulesResponse>> getAllModulesWithSubmodules() {

        List<Modules> modules = moduleRepo.findAll();

        List<ModuleWithSubmodulesResponse> result = modules.stream()
                .map(module -> {
                    List<SubModule> subModules =
                            subModuleRepo.findByModulesId(module.getId());

                    List<SubModuleResponse> subModuleResponses = subModules.stream()
                            .map(sm -> new SubModuleResponse(
                                    sm.getId(),
                                    sm.getSubModuleCode(),
                                    sm.getSubModuleName(),
                                    sm.getDescription()
                            ))
                            .toList();

                    return new ModuleWithSubmodulesResponse(
                            module.getId(),
                            module.getKeyName(),
                            module.getName(),
                            module.getDescription(),
                            subModuleResponses
                    );
                })
                .toList();

        return StandardResponse.success(
                result,
                "Modules and submodules fetched successfully"
        );
    }

    private PermissionResponse toResp(Permission p) {
        return new PermissionResponse(
                p.getId(),
                p.getSubModule().getId(),
                p.getSubModule().getSubModuleName(),
                p.isCanView(),
                p.isCanCreate(),
                p.isCanEdit(),
                p.isCanDelete()
        );
    }
}