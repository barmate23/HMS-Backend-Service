package com.schoolerp.student.service;

import com.schoolerp.staff.dto.permission.*;
import com.schoolerp.staff.exception.NotFoundException;
import com.schoolerp.staff.model.*;
import com.schoolerp.staff.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class PermissionService {

    private final PermissionRepository repo;
    private final RoleRepository roleRepo;
    private final ModuleRepository moduleRepo;

    public Page<PermissionResponse> listByRole(Long roleId, int page, int size) {
        Page<Permission> p = repo.findByRoleId(roleId, PageRequest.of(page, size, Sort.by("id")));
        return p.map(this::toResp);
    }

    public void saveAll(Long roleId, List<PermissionRequest> list) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        // Delete old permissions
        repo.deleteByRoleId(roleId);

        for (PermissionRequest req : list) {
            Module module = moduleRepo.findById(req.moduleId())
                    .orElseThrow(() -> new NotFoundException("Module not found"));
            Permission p = Permission.builder()
                    .role(role)
                    .module(module)
                    .canView(req.canView())
                    .canCreate(req.canCreate())
                    .canEdit(req.canEdit())
                    .canDelete(req.canDelete())
                    .build();
            repo.save(p);
        }
    }

    private PermissionResponse toResp(Permission p) {
        return new PermissionResponse(
                p.getId(),
                p.getModule().getId(),
                p.getModule().getName(),
                p.isCanView(),
                p.isCanCreate(),
                p.isCanEdit(),
                p.isCanDelete()
        );
    }
}
