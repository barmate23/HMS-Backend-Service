package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.RoleCreateRequest;
import com.schoolerp.staff.dto.RoleResponse;
import com.schoolerp.staff.dto.RoleUpdateRequest;
import com.schoolerp.staff.dto.StaffResponse;
import com.schoolerp.staff.entity.Role;
import com.schoolerp.staff.entity.RoleStaffMapper;
import com.schoolerp.staff.entity.UserEntity;
import com.schoolerp.staff.repository.RoleRepository;
import com.schoolerp.staff.repository.RoleStaffMapperRepository;
import com.schoolerp.staff.repository.StaffRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository repo;
 private final StaffRepository staffRepo;
 private final RoleStaffMapperRepository roleStaffMapperRepository;

    public StandardResponse create(RoleCreateRequest req) {

        if (repo.existsByNameIgnoreCase(req.name())) {
            return StandardResponse.error(
                    "Role name already exists",
                    "VALIDATION_ERROR",
                    "name",
                    "A role with this name already exists"
            );
        }

        Role r = Role.builder()
                .name(req.name())
                .description(req.description())
                .code(req.code())
                .isDeleted(false)
                .build();

        List<RoleStaffMapper> roleStaffMapperList = new ArrayList<>();
        for (Integer id : req.StaffIds()) {
            RoleStaffMapper roleStaffMapper = new RoleStaffMapper();
            roleStaffMapper.setRole(r);
            UserEntity staff = staffRepo.findById(id);

            roleStaffMapper.setStaff(staff);
            roleStaffMapper.setDeleted(false);
            roleStaffMapperList.add(roleStaffMapper);
        }

        r = repo.save(r);
        roleStaffMapperRepository.saveAll(roleStaffMapperList);

        return StandardResponse.success(
                toResp(r),
                "Role created successfully"
        );
    }

    public StandardResponse<Page<RoleResponse>> list(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Role> p = repo.findAll(pageable);

        Page<RoleResponse> mapped = p.map(this::toResp);

        StandardResponse.ResponseMetadata meta =
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(p.getTotalElements())
                        .totalPages(p.getTotalPages())
                        .currentPage(page)
                        .pageSize(size)
                        .operation("Role List")
                        .build();

        return StandardResponse.success(mapped, "Roles fetched successfully", meta);
    }

    public StandardResponse get(Long id) {
        Role r = find(id);
        return StandardResponse.success(toResp(r), "Role details fetched");
    }

    public StandardResponse update(Long id, RoleUpdateRequest req) {
        Role role = find(id);

        if (!role.getName().equalsIgnoreCase(req.name()) &&
                repo.existsByNameIgnoreCase(req.name())) {

            return StandardResponse.error(
                    "Role name already exists",
                    "VALIDATION_ERROR",
                    "name",
                    "A role with this name already exists"
            );
        }

        role.setName(req.name());
        role.setCode(req.code());
        role.setDescription(req.description());

        role = repo.save(role);

        List<RoleStaffMapper> roleStaffMapperList = roleStaffMapperRepository.findByIsDeletedAndRoleId(false, role.getId());

        roleStaffMapperRepository.deleteAll(roleStaffMapperList);
        for (Integer staffId : req.StaffIds()) {
            RoleStaffMapper roleStaffMapper = new RoleStaffMapper();
            roleStaffMapper.setRole(role);
            roleStaffMapper.setStaff(staffRepo.findById(Long.valueOf(staffId)).get());
            roleStaffMapper.setDeleted(false);
            roleStaffMapperRepository.save(roleStaffMapper);
        }
        return StandardResponse.success(
                toResp(role),
                "Role updated successfully"
        );
    }

    public StandardResponse<Void> delete(Long id) {
        Role r = find(id);
        r.setDeleted(true);
        repo.save(r);

        return StandardResponse.success("Role deleted successfully");
    }

    private Role find(Long id) {
        return repo.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private RoleResponse toResp(Role r) {
        return new RoleResponse(
                r.getId(),
                r.getName(),
                r.getCode(),
                r.getDescription(),
                toStaffResponse(r.getId())
        );
    }

    private List<StaffResponse> toStaffResponse(Integer id) {
        List<StaffResponse> staffResponseList = new ArrayList<>();
        List<RoleStaffMapper> roleStaffMapperList =  roleStaffMapperRepository.findByIsDeletedAndRoleId(false, id);
        for(RoleStaffMapper roleStaffMapper : roleStaffMapperList) {
            staffResponseList.add(toStfResp(roleStaffMapper.getStaff()));
        }
        return staffResponseList;
    }

    private StaffResponse toStfResp(UserEntity s) {
        return new StaffResponse(
                s.getId(),
                s.getStaffCode(),
                s.getFirstName(),
                s.getLastName(),
                s.getEmail(),
                s.getPhone(),
                s.getDob(),
                s.getFatherName(),
                s.getStatus(),
                s.getDepartment() != null ? s.getDepartment().getId() : null,
                s.getDepartment() != null ? s.getDepartment().getName() : null,
                s.getDesignation() != null ? s.getDesignation().getId() : null,
                s.getDesignation() != null ? s.getDesignation().getName() : null,
                s.getStaffImage()
        );
    }
}
