package com.schoolerp.staff.service;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.RoleCreateRequest;
import com.schoolerp.staff.dto.RoleResponse;
import com.schoolerp.staff.dto.RoleUpdateRequest;
import com.schoolerp.staff.dto.StaffResponse;
import com.schoolerp.staff.entity.Role;
import com.schoolerp.staff.entity.RoleStaffMapper;
import com.schoolerp.staff.entity.Staff;
import com.schoolerp.staff.entity.UserEntity;
import com.schoolerp.staff.repository.RoleRepository;
import com.schoolerp.staff.repository.RoleStaffMapperRepository;
import com.schoolerp.staff.repository.StaffRepository;
import com.schoolerp.staff.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repo;
    private final StaffRepository staffRepo;
    private final UserRepository userRepository;
    private final RoleStaffMapperRepository roleStaffMapperRepository;
    private UserEntity userEntity;

    public StandardResponse create(RoleCreateRequest req) {

        if (repo.existsByNameIgnoreCase(req.name())) {
            return StandardResponse.error(
                    "Role name already exists",
                    "VALIDATION_ERROR",
                    "name",
                    "A role with this name already exists");
        }

        Role r = Role.builder()
                .name(req.name())
                .description(req.description())
                .code(req.code())
                .isDeleted(false)
                .build();

        List<RoleStaffMapper> roleStaffMapperList = new ArrayList<>();
        if (req.staffIds() != null) {
            for (Integer id : req.staffIds()) {
                Staff staff = staffRepo.findById(id.longValue())
                        .filter(s -> !s.isDeleted())
                        .orElse(null);

                if (staff != null) {
                    UserEntity userEntity = userRepository.findByStaffId(staff.getId());
                    if (userEntity != null) {
                        RoleStaffMapper roleStaffMapper = new RoleStaffMapper();
                        roleStaffMapper.setRole(r);
                        roleStaffMapper.setStaff(userEntity);
                        roleStaffMapper.setDeleted(false);
                        roleStaffMapperList.add(roleStaffMapper);
                    }
                }
            }
        }

        r = repo.save(r);
        roleStaffMapperRepository.saveAll(roleStaffMapperList);

        return StandardResponse.success(
                toResp(r),
                "Role created successfully");
    }

    public StandardResponse<Page<RoleResponse>> list(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Role> p = repo.findAll(pageable);

        Page<RoleResponse> mapped = p.map(this::toResp);

        StandardResponse.ResponseMetadata meta = StandardResponse.ResponseMetadata.builder()
                .totalRecords(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .operation("Role List")
                .build();

        return StandardResponse.success(mapped, "Roles fetched successfully", meta);
    }

    public StandardResponse get(Long id) {
        Role r;
        try {
            r = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Role not found",
                    "ROLE_NOT_FOUND",
                    "id",
                    "The provided roleId does not exist"
            );
        }
        return StandardResponse.success(toResp(r), "Role details fetched");
    }

    public StandardResponse update(Long id, RoleUpdateRequest req) {
        Role role;
        try {
            role = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Role not found",
                    "ROLE_NOT_FOUND",
                    "id",
                    "The provided roleId does not exist"
            );
        }

        if (!role.getName().equalsIgnoreCase(req.name()) &&
                repo.existsByNameIgnoreCase(req.name())) {

            return StandardResponse.error(
                    "Role name already exists",
                    "VALIDATION_ERROR",
                    "name",
                    "A role with this name already exists");
        }

        role.setName(req.name());
        role.setCode(req.code());
        role.setDescription(req.description());

        role = repo.save(role);

        List<RoleStaffMapper> roleStaffMapperList = roleStaffMapperRepository.findByIsDeletedAndRoleId(false,
                role.getId());

        roleStaffMapperRepository.deleteAll(roleStaffMapperList);
        if (req.staffIds() != null) {
            for (Integer staffId : req.staffIds()) {
                Staff staff = staffRepo.findById(staffId.longValue())
                        .filter(s -> !s.isDeleted())
                        .orElse(null);
                UserEntity userEntity = staff != null ? userRepository.findByStaffId(staff.getId()) : null;

                if (staff != null && userEntity != null) {
                    RoleStaffMapper roleStaffMapper = new RoleStaffMapper();
                    roleStaffMapper.setRole(role);
                    roleStaffMapper.setStaff(userEntity);
                    roleStaffMapper.setDeleted(false);
                    roleStaffMapperRepository.save(roleStaffMapper);
                }
            }
        }
        return StandardResponse.success(
                toResp(role),
                "Role updated successfully");
    }

    public StandardResponse<Void> delete(Long id) {
        Role r;
        try {
            r = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Role not found",
                    "ROLE_NOT_FOUND",
                    "id",
                    "The provided roleId does not exist"
            );
        }
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
                toStaffResponse(r.getId()));
    }

    private List<StaffResponse> toStaffResponse(Integer id) {
        List<StaffResponse> staffResponseList = new ArrayList<>();
        List<RoleStaffMapper> roleStaffMapperList = roleStaffMapperRepository.findByIsDeletedAndRoleId(false, id);
        for (RoleStaffMapper roleStaffMapper : roleStaffMapperList) {
            staffResponseList.add(toStfResp(roleStaffMapper.getStaff()));
        }
        return staffResponseList;
    }

    private StaffResponse toStfResp(UserEntity s) {
        if (s == null || s.getStaff() == null) {
            return null;
        }
        Staff staff = s.getStaff();
        return new StaffResponse(
                s.getId(),
                staff.getId(),
                staff.getStaffCode(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getEmail(),
                staff.getPhone(),
                staff.getDob(),
                staff.getFatherName(),
                staff.getStatus(),
                staff.getDepartment() != null ? staff.getDepartment().getId() : null,
                staff.getDepartment() != null ? staff.getDepartment().getName() : null,
                staff.getDesignation() != null ? staff.getDesignation().getId() : null,
                staff.getDesignation() != null ? staff.getDesignation().getName() : null,
                null,
                null,
                staff.getStaffImage());
    }
}
