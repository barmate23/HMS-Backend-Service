package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.dto.*;
import com.schoolerp.staff.entity.Department;
import com.schoolerp.staff.entity.Designation;
import com.schoolerp.staff.entity.UserEntity;
import com.schoolerp.staff.repository.DepartmentRepository;
import com.schoolerp.staff.repository.DesignationRepository;
import com.schoolerp.staff.repository.StaffRepository;
import com.schoolerp.staff.util.CodeGenerator;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {
    private final StaffRepository staffRepository;
    private final DepartmentRepository deptRepo;
    private PasswordEncoder  passwordEncoder;
    private final DesignationRepository desigRepo;
    private final CodeGenerator codeGen;

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(StaffCreateRequest req) {

        // Validate duplicate email
        if (staffRepository.existsByEmailIgnoreCase(req.email())) {
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
        String password = codeGen.generatePassword();
        String encodedPassword =  passwordEncoder.encode(password);
        RestTemplate rest = new RestTemplate();

        Map<String, Object> vars = Map.of(
                "name", req.firstName() + " " + req.lastName(),
                "username", req.username(),
                "password", encodedPassword
        );

        EmailRequest request = new EmailRequest(
                req.email(),
                "Your Login Credentials",
                "credentials",
                vars
        );

        rest.postForObject(
                "http://email-service:8092/api/email/send",
                request,
                String.class
        );

        UserEntity s = UserEntity.builder()
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
                .staffImage(req.staffImage())
                .username(req.username())
                .password(encodedPassword)
                .isDefaultPasswordGenerated(true)
                .isDeleted(false)
                .build();

        staffRepository.save(s);

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

        Page<UserEntity> result = staffRepository.search(deptId, status, q, pageable);

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

        UserEntity s;
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

    public StandardResponse<?> getAllStaff() {
        List<UserEntity> staffList = staffRepository.findAll();

        List<StaffAllResponse> staffAllResponseList = new ArrayList<>();
        staffList.forEach(staff -> {
            StaffAllResponse staffAllResponse = new StaffAllResponse(staff.getId(), staff.getFirstName() + " " + staff.getLastName());
            staffAllResponseList.add(staffAllResponse);
        });
        return StandardResponse.success(
                staffAllResponseList,
                "Staff created successfully"
        );
    }
    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, StaffUpdateRequest req) {

        UserEntity existing;
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
                staffRepository.existsByEmailIgnoreCase(req.email())) {

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
        existing.setStaffImage(req.staffImage());
        existing.setStatus(req.status());

        staffRepository.save(existing);

        return StandardResponse.success(
                toResp(existing),
                "Staff updated successfully"
        );
    }

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id) {
        UserEntity s;
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
        staffRepository.save(s);

        return StandardResponse.success("Staff deleted successfully");
    }

    // -------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------
    private UserEntity find(Long id) {
        return staffRepository.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Staff not found"));
    }

    private StaffResponse toResp(UserEntity s) {
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
