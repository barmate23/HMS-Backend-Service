package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DepartmentCreateRequest;
import com.schoolerp.staff.dto.DepartmentResponse;
import com.schoolerp.staff.dto.DepartmentUpdateRequest;
import com.schoolerp.staff.dto.PageResponse;
import com.schoolerp.staff.entity.Department;
import com.schoolerp.staff.entity.Designation;
import com.schoolerp.staff.repository.DepartmentRepository;
import com.schoolerp.staff.repository.DesignationRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;
    private final DesignationRepository designationRepository;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(DepartmentCreateRequest req) {

        if (repository.existsByNameIgnoreCase(req.name())) {
            return StandardResponse.error(
                    "Department name already exists",
                    "DUPLICATE_NAME",
                    "name",
                    "Department with same name is already present"
            );
        }

        if (repository.existsByCodeIgnoreCase(req.code())) {
            return StandardResponse.error(
                    "Department code already exists",
                    "DUPLICATE_CODE",
                    "code",
                    "Department with same code is already present"
            );
        }

        Department d = Department.builder()
                .name(req.name())
                .code(req.code())
                .description(req.description())
                .hodId(req.hodId())
                .isDelete(false)
                .build();

        d = repository.save(d);

        return StandardResponse.success(
                toResp(d),
                "Department created successfully"
        );
    }

    // -------------------------------------------------------------
    // SEARCH (Paginated)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<DepartmentResponse>> search(String q, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Department> res = repository.search(q, pageable);

        PageResponse<DepartmentResponse> pageResponse =
                PageResponse.from(res.map(this::toResp));

        StandardResponse.ResponseMetadata metadata =
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(res.getTotalElements())
                        .totalPages(res.getTotalPages())
                        .currentPage(page)
                        .pageSize(size)
                        .executionTimeMs(null)
                        .operation("SEARCH_DEPARTMENTS")
                        .build();

        return StandardResponse.success(
                pageResponse,
                "Departments fetched successfully",
                metadata
        );
    }

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse<DepartmentResponse> get(Long id) {
        Department d = find(id);
        return StandardResponse.success(
                toResp(d),
                "Department fetched successfully"
        );
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, DepartmentUpdateRequest req) {

        Department d = find(id);

        if (!d.getName().equalsIgnoreCase(req.name()) &&
                repository.existsByNameIgnoreCase(req.name())) {

            return StandardResponse.error(
                    "Department name already exists",
                    "DUPLICATE_NAME",
                    "name",
                    "Another department already uses this name"
            );
        }

        if (!d.getCode().equalsIgnoreCase(req.code()) &&
                repository.existsByCodeIgnoreCase(req.code())) {

            return StandardResponse.error(
                    "Department code already exists",
                    "DUPLICATE_CODE",
                    "code",
                    "Another department already uses this code"
            );
        }

        d.setName(req.name());
        d.setCode(req.code());
        d.setDescription(req.description());
        d.setHodId(req.hodId());

        d = repository.save(d);

        return StandardResponse.success(
                toResp(d),
                "Department updated successfully"
        );
    }

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id) {
        Department d = find(id);
        d.setIsDelete(true);
        repository.save(d);

        return StandardResponse.success("Department deleted successfully");
    }

    // -------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------
    private Department find(Long id) {
        return repository.findById(id)
                .filter(x -> !x.getIsDelete())
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private DepartmentResponse toResp(Department d) {
        List<Designation> designationList = designationRepository.findByDepartmentIdAndIsDeleted(d.getId(), false);
        return new DepartmentResponse(
                d.getId(),
                d.getName(),
                d.getCode(),
                d.getDescription(),
                d.getHodId(),
                designationList.size(),
                "Amol",
                "Hod",
                "This is base 64 URL"
        );
    }
}
