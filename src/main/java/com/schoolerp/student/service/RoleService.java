package com.schoolerp.student.service;


import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.RoleCreateRequest;
import com.schoolerp.student.dto.RoleResponse;
import com.schoolerp.student.dto.RoleUpdateRequest;
import com.schoolerp.student.entity.Role;
import com.schoolerp.student.repository.RoleRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository repo;

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

        r = repo.save(r);

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
        Role r = find(id);

        if (!r.getName().equalsIgnoreCase(req.name()) &&
                repo.existsByNameIgnoreCase(req.name())) {

            return StandardResponse.error(
                    "Role name already exists",
                    "VALIDATION_ERROR",
                    "name",
                    "A role with this name already exists"
            );
        }

        r.setName(req.name());
        r.setCode(req.code());
        r.setDescription(req.description());

        r = repo.save(r);

        return StandardResponse.success(
                toResp(r),
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
                r.getDescription()
        );
    }
}
