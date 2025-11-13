package com.schoolerp.student.service;

import com.schoolerp.staff.common.PageResponse;
import com.schoolerp.staff.dto.staff.*;
import com.schoolerp.staff.enums.StaffStatus;
import com.schoolerp.staff.exception.BadRequestException;
import com.schoolerp.staff.exception.NotFoundException;
import com.schoolerp.staff.model.*;
import com.schoolerp.staff.repository.*;
import com.schoolerp.staff.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository repo;
    private final DepartmentRepository deptRepo;
    private final DesignationRepository desigRepo;
    private final CodeGenerator codeGen;

    public StaffResponse create(StaffCreateRequest req) {

        if (repo.existsByEmailIgnoreCase(req.email()))
            throw new BadRequestException("Email already exists");

        Department dept = null;
        if (req.departmentId() != null) {
            dept = deptRepo.findById(req.departmentId())
                    .filter(d -> !d.isDeleted())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
        }

        Designation desig = null;
        if (req.designationId() != null) {
            desig = desigRepo.findById(req.designationId())
                    .filter(d -> !d.isDeleted())
                    .orElseThrow(() -> new NotFoundException("Designation not found"));
        }

        String deptCode = (dept != null) ? dept.getCode() : "GEN";
        String staffCode = codeGen.generate(deptCode);

        Staff s = Staff.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .dob(req.dob())
                .department(dept)
                .designation(desig)
                .fatherName(req.fatherName())
                .status(req.status() != null ? req.status() : StaffStatus.ACTIVE)
                .staffCode(staffCode)
                .build();

        return toResp(repo.save(s));
    }

    public PageResponse<StaffResponse> search(Long deptId, StaffStatus status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<Staff> result = repo.search(deptId, status, q, pageable);
        return PageResponse.from(result.map(this::toResp));
    }

    public StaffResponse get(Long id) {
        return toResp(find(id));
    }

    public StaffResponse update(Long id, StaffUpdateRequest req) {
        Staff s = find(id);

        if (!s.getEmail().equalsIgnoreCase(req.email()) &&
                repo.existsByEmailIgnoreCase(req.email()))
            throw new BadRequestException("Email already exists");

        Department dept = null;
        if (req.departmentId() != null) {
            dept = deptRepo.findById(req.departmentId())
                    .filter(d -> !d.isDeleted())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
        }

        Designation desig = null;
        if (req.designationId() != null) {
            desig = desigRepo.findById(req.designationId())
                    .filter(d -> !d.isDeleted())
                    .orElseThrow(() -> new NotFoundException("Designation not found"));
        }

        s.setFirstName(req.firstName());
        s.setLastName(req.lastName());
        s.setEmail(req.email());
        s.setPhone(req.phone());
        s.setDob(req.dob());
        s.setDepartment(dept);
        s.setDesignation(desig);
        s.setFatherName(req.fatherName());
        s.setStatus(req.status());

        return toResp(repo.save(s));
    }

    public void delete(Long id) {
        Staff s = find(id);
        s.setDeleted(true);
        repo.save(s);
    }

    private Staff find(Long id) {
        return repo.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Staff not found"));
    }

    private StaffResponse toResp(Staff s) {
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
                s.getDesignation() != null ? s.getDesignation().getName() : null
        );
    }
}
