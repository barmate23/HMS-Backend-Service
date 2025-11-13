//package com.schoolerp.student.service;
//
//
//import com.schoolerp.student.dto.PermissionRequest;
//import com.schoolerp.student.dto.PermissionResponse;
//import com.schoolerp.student.entity.Permission;
//import com.schoolerp.student.entity.Role;
//import com.schoolerp.student.repository.ModuleRepository;
//import com.schoolerp.student.repository.PermissionRepository;
//import com.schoolerp.student.repository.RoleRepository;
//import jakarta.ws.rs.NotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service @RequiredArgsConstructor @Transactional
//public class PermissionService {
//
//    private final PermissionRepository repo;
//    private final RoleRepository roleRepo;
//    private final ModuleRepository moduleRepo;
//
//    public Page<PermissionResponse> listByRole(Long roleId, int page, int size) {
//        Page<Permission> p = repo.findByRoleId(roleId, PageRequest.of(page, size, Sort.by("id")));
//        return p.map(this::toResp);
//    }
//
//    public void saveAll(Long roleId, List<PermissionRequest> list) {
//        Role role = roleRepo.findById(roleId)
//                .orElseThrow(() -> new NotFoundException("Role not found"));
//
//        // Delete old permissions
//        repo.deleteByRoleId(roleId);
//
//        for (PermissionRequest req : list) {
//            Module module = moduleRepo.findById(req.moduleId())
//                    .orElseThrow(() -> new NotFoundException("Module not found"));
//            Permission p = Permission.builder()
//                    .role(role)
//                    .module(module)
//                    .canView(req.canView())
//                    .canCreate(req.canCreate())
//                    .canEdit(req.canEdit())
//                    .canDelete(req.canDelete())
//                    .build();
//            repo.save(p);
//        }
//    }
//
//    private PermissionResponse toResp(Permission p) {
//        return new PermissionResponse(
//                p.getId(),
//                p.getModule().getId(),
//                p.getModule().getName(),
//                p.isCanView(),
//                p.isCanCreate(),
//                p.isCanEdit(),
//                p.isCanDelete()
//        );
//    }
//}
