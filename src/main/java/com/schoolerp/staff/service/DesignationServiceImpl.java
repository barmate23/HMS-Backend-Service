package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DesignationCreateRequest;
import com.schoolerp.staff.dto.DesignationResponse;
import com.schoolerp.staff.dto.DesignationUpdateRequest;
import com.schoolerp.staff.dto.PageResponse;
import com.schoolerp.staff.entity.Department;
import com.schoolerp.staff.entity.Designation;
import com.schoolerp.staff.repository.DepartmentRepository;
import com.schoolerp.staff.repository.DesignationRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationServiceImpl implements DesignationService{

    private final DesignationRepository repository;
    private final DepartmentRepository departmentRepository;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(DesignationCreateRequest req) {

        Department department = departmentRepository.findById(req.departmentId())
                .filter(d -> !d.getIsDelete())
                .orElse(null);

        if (department == null) {
            return StandardResponse.error(
                    "Department not found",
                    "DEPARTMENT_NOT_FOUND",
                    "departmentId",
                    "The provided departmentId does not exist"
            );
        }

        Designation designation = Designation.builder()
                .name(req.name())
                .description(req.description())
                .teaching(req.teaching())
                .department(department)
                .isDeleted(false)
                .build();

        designation = repository.save(designation);

        return StandardResponse.success(
                toResp(designation),
                "Designation created successfully"
        );
    }

    // -------------------------------------------------------------
    // LIST BY DEPARTMENT (Pagination)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<DesignationResponse>> listByDepartment(Long departmentId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Designation> result = repository.findByDepartmentIdAndIsDeletedFalse(departmentId, pageable);

        PageResponse<DesignationResponse> pageResponse = PageResponse.from(result.map(this::toResp));

        StandardResponse.ResponseMetadata meta =
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .currentPage(page)
                        .pageSize(size)
                        .operation("LIST_DESIGNATIONS_BY_DEPARTMENT")
                        .build();

        return StandardResponse.success(
                pageResponse,
                "Designations fetched successfully",
                meta
        );
    }

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse<DesignationResponse> get(Long id) {
        Designation d = find(id);
        return StandardResponse.success(
                toResp(d),
                "Designation fetched successfully"
        );
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, DesignationUpdateRequest req) {

        Designation existing = find(id);

        Department department = departmentRepository.findById(req.departmentId())
                .filter(x -> !x.getIsDelete())
                .orElse(null);

        if (department == null) {
            return StandardResponse.error(
                    "Department not found",
                    "DEPARTMENT_NOT_FOUND",
                    "departmentId",
                    "Invalid department selected"
            );
        }

        existing.setName(req.name());
        existing.setDescription(req.description());
        existing.setTeaching(req.teaching());
        existing.setDepartment(department);

        repository.save(existing);

        return StandardResponse.success(
                toResp(existing),
                "Designation updated successfully"
        );
    }

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id) {
        Designation d = find(id);
        d.setDeleted(true);
        repository.save(d);

        return StandardResponse.success("Designation deleted successfully");
    }

    // -------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------
    private Designation find(Long id) {
        return repository.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Designation not found"));
    }

    private DesignationResponse toResp(Designation d) {
        return new DesignationResponse(
                d.getId(),
                d.getName(),
                d.getDescription(),
                d.isTeaching(),
                d.getDepartment() != null ? d.getDepartment().getId() : null,
                d.getDepartment() != null ? d.getDepartment().getName() : null
        );
    }
}
