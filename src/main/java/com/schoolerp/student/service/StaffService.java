package com.schoolerp.student.service;


import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.constants.StaffStatus;
import com.schoolerp.student.dto.PageResponse;
import com.schoolerp.student.dto.StaffCreateRequest;
import com.schoolerp.student.dto.StaffResponse;
import com.schoolerp.student.dto.StaffUpdateRequest;
import com.schoolerp.student.entity.Department;
import com.schoolerp.student.entity.Designation;
import com.schoolerp.student.entity.Staff;
import com.schoolerp.student.repository.DepartmentRepository;
import com.schoolerp.student.repository.DesignationRepository;
import com.schoolerp.student.repository.StaffRepository;
import com.schoolerp.student.util.CodeGenerator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
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

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(StaffCreateRequest req) {

        // Validate duplicate email
        if (repo.existsByEmailIgnoreCase(req.email())) {
            return StandardResponse.error(
                    "Email already exists",
                    "DUPLICATE_EMAIL",
                    "email",
                    "Another staff already uses this email id"
            );
        }

        // Validate Department
        Department dept = null;
        if (req.departmentId() != null) {
            dept = deptRepo.findById(req.departmentId())
                    .filter(d -> !d.getIsDelete())
                    .orElse(null);

            if (dept == null) {
                return StandardResponse.error(
                        "Department not found",
                        "DEPARTMENT_NOT_FOUND",
                        "departmentId",
                        "Invalid department selected"
                );
            }
        }

        // Validate Designation
        Designation desig = null;
        if (req.designationId() != null) {
            desig = desigRepo.findById(req.designationId())
                    .filter(d -> !d.isDeleted())
                    .orElse(null);

            if (desig == null) {
                return StandardResponse.error(
                        "Designation not found",
                        "DESIGNATION_NOT_FOUND",
                        "designationId",
                        "Invalid designation selected"
                );
            }
        }

        // Generate staff code
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
                .isDeleted(false)
                .build();

        repo.save(s);

        return StandardResponse.success(
                toResp(s),
                "Staff created successfully"
        );
    }

    // -------------------------------------------------------------
    // SEARCH (Paginated)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<StaffResponse>> search(
            Long deptId,
            StaffStatus status,
            String q,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

        Page<Staff> result = repo.search(deptId, status, q, pageable);

        PageResponse<StaffResponse> pageData = PageResponse.from(result.map(this::toResp));

        StandardResponse.ResponseMetadata meta =
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .pageSize(size)
                        .currentPage(page)
                        .operation("SEARCH_STAFF")
                        .build();

        return StandardResponse.success(
                pageData,
                "Staff list fetched successfully",
                meta
        );
    }

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse get(Long id) {

        Staff s;
        try {
            s = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Staff not found",
                    "STAFF_NOT_FOUND",
                    "id",
                    "Invalid staff id"
            );
        }

        return StandardResponse.success(
                toResp(s),
                "Staff fetched successfully"
        );
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, StaffUpdateRequest req) {

        Staff existing;
        try {
            existing = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Staff not found",
                    "STAFF_NOT_FOUND",
                    "id",
                    "Invalid staff id"
            );
        }

        // Email validation
        if (!existing.getEmail().equalsIgnoreCase(req.email()) &&
                repo.existsByEmailIgnoreCase(req.email())) {

            return StandardResponse.error(
                    "Email already exists",
                    "DUPLICATE_EMAIL",
                    "email",
                    "Another staff already uses this email"
            );
        }

        // Validate Department
        Department dept = null;
        if (req.departmentId() != null) {
            dept = deptRepo.findById(req.departmentId())
                    .filter(d -> !d.getIsDelete())
                    .orElse(null);

            if (dept == null) {
                return StandardResponse.error(
                        "Department not found",
                        "DEPARTMENT_NOT_FOUND",
                        "departmentId",
                        "Invalid department selected"
                );
            }
        }

        // Validate Designation
        Designation desig = null;
        if (req.designationId() != null) {
            desig = desigRepo.findById(req.designationId())
                    .filter(d -> !d.isDeleted())
                    .orElse(null);

            if (desig == null) {
                return StandardResponse.error(
                        "Designation not found",
                        "DESIGNATION_NOT_FOUND",
                        "designationId",
                        "Invalid designation selected"
                );
            }
        }

        existing.setFirstName(req.firstName());
        existing.setLastName(req.lastName());
        existing.setEmail(req.email());
        existing.setPhone(req.phone());
        existing.setDob(req.dob());
        existing.setDepartment(dept);
        existing.setDesignation(desig);
        existing.setFatherName(req.fatherName());
        existing.setStatus(req.status());

        repo.save(existing);

        return StandardResponse.success(
                toResp(existing),
                "Staff updated successfully"
        );
    }

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id) {
        Staff s;
        try {
            s = find(id);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Staff not found",
                    "STAFF_NOT_FOUND",
                    "id",
                    "Invalid staff id"
            );
        }

        s.setDeleted(true);
        repo.save(s);

        return StandardResponse.success("Staff deleted successfully");
    }

    // -------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------
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
