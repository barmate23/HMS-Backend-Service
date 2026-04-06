package com.schoolerp.staff.service;

import com.schoolerp.staff.common.StandardResponse;

import com.schoolerp.staff.config.UserContext;
import com.schoolerp.staff.dto.*;
import com.schoolerp.staff.entity.*;
import com.schoolerp.staff.repository.*;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService{

    private final RoleStaffMapperRepository roleStaffMapperRepository;
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
        List<Permission> permissionList = permissionRepository.findByRoleId(roleId);
        Map<Integer, Permission> permissionMap = new HashMap<>();
        if (permissionList != null) {
            permissionMap = permissionList.stream().collect(Collectors.toMap(k -> k.getSubModule().getId(), v -> v));

        }
        for (PermissionRequest req : list) {
            SubModule subModule = subModuleRepo.findById(req.subModuleId())
                    .orElseThrow(() -> new NotFoundException("Submodule not found"));
            Permission p = permissionMap.get(subModule.getId());
            if (p == null) {
                p = Permission.builder()
                        .role(role)
                        .subModule(subModule)
                        .canView(req.canView())
                        .canCreate(req.canCreate())
                        .canEdit(req.canEdit())
                        .canDelete(req.canDelete())
                        .build();
            } else {
                p.setCanCreate(req.canCreate());
                p.setCanDelete(req.canDelete());
                p.setCanEdit(req.canEdit());
                p.setCanView(req.canView());
            }


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

    public StandardResponse<List<UserPermissionResponse>> getUserPermission() {
        String loggedInUser = UserContext.getUser();
        System.out.println("User = " + loggedInUser);
        RoleStaffMapper roleStaffMapper = roleStaffMapperRepository.findByIsDeletedAndStaffEmail(false, loggedInUser);
        List<UserPermissionResponse> permissionResponseList = new ArrayList<>();

       String designation = roleStaffMapper.getStaff().getStaff() != null ? roleStaffMapper.getStaff().getStaff().getDesignation().getName() : "System Administrator";
        List<Permission> permissionList = permissionRepository
                .findByRoleId(Long.parseLong(Integer.toString(roleStaffMapper.getRole().getId())))
                .stream()
                .filter(p -> p.isCanView() || p.isCanCreate() || p.isCanEdit() || p.isCanDelete())
                .toList();

        Map<Integer, List<Permission>> modulePermissionMapper = permissionList.stream().collect(Collectors.groupingBy(k -> k.getSubModule().getModules().getId()));

        for (Map.Entry<Integer, List<Permission>> modulePermissionMap : modulePermissionMapper.entrySet()) {
            List<Permission> permissions = modulePermissionMap.getValue();
            List<UserSubModuleResponse> userSubModuleResponseList =
                    permissions.stream()
                            .map(permission -> new UserSubModuleResponse(
                                    permission.getSubModule().getId(),
                                    permission.getRole().getName(),
                                    designation,
                                    permission.getSubModule().getSubModuleCode(),
                                    permission.getSubModule().getSubModuleName(),
                                    new PermissionUserResponse(
                                            permission.isCanView(),
                                            permission.isCanCreate(),
                                            permission.isCanEdit(),
                                            permission.isCanDelete()
                                    )
                            ))
                            .toList();
            UserPermissionResponse userPermissionResponse = new UserPermissionResponse(permissions.get(0).getId(), permissions.get(0).getSubModule().getModules().getName(), permissions.get(0).getSubModule().getModules().getKeyName(), userSubModuleResponseList);
            permissionResponseList.add(userPermissionResponse);
        }
        return StandardResponse.success(
                permissionResponseList,
                "Permissions fetched successfully"
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