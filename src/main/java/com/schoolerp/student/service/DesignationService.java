package com.schoolerp.student.service;

import com.schoolerp.staff.common.PageResponse;
import com.schoolerp.staff.dto.designation.*;
import com.schoolerp.staff.exception.NotFoundException;
import com.schoolerp.staff.model.Department;
import com.schoolerp.staff.model.Designation;
import com.schoolerp.staff.repository.DepartmentRepository;
import com.schoolerp.staff.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationService {

    private final DesignationRepository repository;
    private final DepartmentRepository departmentRepository;

    public DesignationResponse create(DesignationCreateRequest req) {

        Department department = departmentRepository.findById(req.departmentId())
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        Designation designation = Designation.builder()
                .name(req.name())
                .description(req.description())
                .teaching(req.teaching())
                .department(department)
                .build();

        designation = repository.save(designation);
        return toResp(designation);
    }

    public PageResponse<DesignationResponse> listByDepartment(Long departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Designation> result = repository.findByDepartmentIdAndDeletedFalse(departmentId, pageable);
        return PageResponse.from(result.map(this::toResp));
    }

    public DesignationResponse get(Long id) {
        return toResp(find(id));
    }

    public DesignationResponse update(Long id, DesignationUpdateRequest req) {
        Designation existing = find(id);

        Department department = departmentRepository.findById(req.departmentId())
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        existing.setName(req.name());
        existing.setDescription(req.description());
        existing.setTeaching(req.teaching());
        existing.setDepartment(department);

        return toResp(repository.save(existing));
    }

    public void delete(Long id) {
        Designation d = find(id);
        d.setDeleted(true);
        repository.save(d);
    }

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

