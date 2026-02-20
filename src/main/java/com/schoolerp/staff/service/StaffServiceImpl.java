package com.schoolerp.staff.service;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.dto.*;
import com.schoolerp.staff.entity.*;
import com.schoolerp.staff.repository.*;
import com.schoolerp.staff.util.CodeGenerator;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class StaffServiceImpl implements StaffService {
        private final UserRepository userRepository;
        private final StaffRepository staffRepository;
        private final StaffQualificationRepository qualificationRepository;
        private final PasswordEncoder encoder;
        private final DepartmentRepository deptRepo;
        private final DesignationRepository desigRepo;
        private final CodeGenerator codeGen;

        // -------------------------------------------------------------
        // CREATE
        // -------------------------------------------------------------
        @Transactional
        public StandardResponse create(StaffCreateRequest req) {

                // 1️⃣ Validate Email
                if (userRepository.existsByEmailIgnoreCase(req.email())) {
                        return StandardResponse.error(
                                        "Email already exists",
                                        "DUPLICATE_EMAIL",
                                        "email",
                                        "Another staff already uses this email id");
                }

                // 2️⃣ Validate Department
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
                                                "Invalid department selected");
                        }
                }

                // 3️⃣ Validate Designation
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
                                                "Invalid designation selected");
                        }
                }

                // 4️⃣ Generate Staff Code
                String deptCode = (dept != null) ? dept.getCode() : "GEN";
                String staffCode = codeGen.generate(deptCode);

                // 5️⃣ Create Staff Entity
                Staff staff = Staff.builder()
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
                                .licenseNumber(req.licenseNumber())
                                .staffImage(req.staffImage())
                                .isDeleted(false)

                                // Bank Details
                                .bankName(req.bankName())
                                .accountHolderName(req.accountHolderName())
                                .accountNumber(req.accountNumber())
                                .ifscCode(req.ifscCode())
                                .branchName(req.branchName())
                                .upiId(req.upiId())

                                // Address Details
                                .addressLine1(req.addressLine1())
                                .addressLine2(req.addressLine2())
                                .city(req.city())
                                .state(req.state())
                                .country(req.country())
                                .postalCode(req.postalCode())
                                .build();

                staffRepository.save(staff);

                // 6️⃣ Save Qualifications
                if (req.qualifications() != null && !req.qualifications().isEmpty()) {

                        List<StaffQualification> qualifications = req.qualifications().stream()
                                        .map(q -> StaffQualification.builder()
                                                        .qualification(q.qualification())
                                                        .specialization(q.specialization())
                                                        .university(q.university())
                                                        .passingYear(q.passingYear())
                                                        .grade(q.grade())
                                                        .staff(staff)
                                                        .build())
                                        .toList();

                        qualificationRepository.saveAll(qualifications);
                }

                // 7️⃣ Create User Account
                String password = codeGen.generatePassword();
                String encodedPassword = encoder.encode(password);

                UserEntity user = UserEntity.builder()
                                .email(req.email())
                                .username(req.email())
                                .password(encodedPassword)
                                .isDefaultPasswordGenerated(true)
                                .isDeleted(false)
                                .staff(staff) // 🔥 Important
                                .build();

                userRepository.save(user);

                // 8️⃣ Send Email
                sendCredentialsEmail(req, password);

                return StandardResponse.success(
                                toResp(staff),
                                "Staff created successfully");
        }

        private void sendCredentialsEmail(StaffCreateRequest req, String password) {

                RestTemplate rest = new RestTemplate();

                Map<String, Object> vars = Map.of(
                                "name", req.firstName() + " " + req.lastName(),
                                "username", req.email(),
                                "password", password);

                EmailRequest request = new EmailRequest(
                                req.email(),
                                "Your Login Credentials",
                                "credentials",
                                vars);

                rest.postForObject(
                                "https://emails.helixioninnovations.com/api/email/send",
                                request,
                                String.class);
        }

        // -------------------------------------------------------------
        // SEARCH (Paginated)
        // -------------------------------------------------------------
        public StandardResponse<PageResponse<StaffResponse>> search(
                        Long deptId,
                        String designation, StaffStatus status,
                        String q,
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

                Page<Staff> result = staffRepository.search(deptId, designation, status, q, pageable);

                PageResponse<StaffResponse> pageData = PageResponse.from(result.map(this::toResp));

                StandardResponse.ResponseMetadata meta = StandardResponse.ResponseMetadata.builder()
                                .totalRecords(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .pageSize(size)
                                .currentPage(page)
                                .operation("SEARCH_STAFF")
                                .build();

                return StandardResponse.success(
                                pageData,
                                "Staff list fetched successfully",
                                meta);
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
                                        "Invalid staff id");
                }

                return StandardResponse.success(
                                toDetailsResp(s),
                                "Staff fetched successfully");
        }

        public StandardResponse<?> getAllStaff() {
                List<Staff> staffList = staffRepository.findAll();

                List<StaffAllResponse> staffAllResponseList = new ArrayList<>();
                staffList.forEach(staff -> {
                        StaffAllResponse staffAllResponse = new StaffAllResponse(staff.getId(),
                                        staff.getFirstName() + " " + staff.getLastName(), staff.getDepartment().getId(),
                                        staff.getDepartment().getName());
                        staffAllResponseList.add(staffAllResponse);
                });
                return StandardResponse.success(
                                staffAllResponseList,
                                "get staff list successfully");
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
                                        "Invalid staff id");
                }

                // Email validation
                if (!existing.getEmail().equalsIgnoreCase(req.email()) &&
                                userRepository.existsByEmailIgnoreCase(req.email())) {

                        return StandardResponse.error(
                                        "Email already exists",
                                        "DUPLICATE_EMAIL",
                                        "email",
                                        "Another staff already uses this email");
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
                                                "Invalid department selected");
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
                                                "Invalid designation selected");
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
                existing.setLicenseNumber(req.licenseNumber());
                existing.setStatus(req.status());
                existing.setStaffImage(req.staffImage());

                // Bank Details
                existing.setBankName(req.bankName());
                existing.setAccountHolderName(req.accountHolderName());
                existing.setAccountNumber(req.accountNumber());
                existing.setIfscCode(req.ifscCode());
                existing.setBranchName(req.branchName());
                existing.setUpiId(req.upiId());

                // Address Details
                existing.setAddressLine1(req.addressLine1());
                existing.setAddressLine2(req.addressLine2());
                existing.setCity(req.city());
                existing.setState(req.state());
                existing.setCountry(req.country());
                existing.setPostalCode(req.postalCode());

                staffRepository.save(existing);

                // Update Qualifications (Delete old, Save new)
                List<StaffQualification> existingQuals = qualificationRepository.findByStaff(existing);
                qualificationRepository.deleteAll(existingQuals);

                if (req.qualifications() != null && !req.qualifications().isEmpty()) {
                        List<StaffQualification> newQuals = req.qualifications().stream()
                                        .map(q -> StaffQualification.builder()
                                                        .qualification(q.qualification())
                                                        .specialization(q.specialization())
                                                        .university(q.university())
                                                        .passingYear(q.passingYear())
                                                        .grade(q.grade())
                                                        .staff(existing)
                                                        .build())
                                        .toList();
                        qualificationRepository.saveAll(newQuals);
                }

                return StandardResponse.success(
                                toDetailsResp(existing),
                                "Staff updated successfully");
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
                                        "Invalid staff id");
                }

                s.setDeleted(true);
                staffRepository.save(s);

                return StandardResponse.success("Staff deleted successfully");
        }

        // -------------------------------------------------------------
        // UTILITIES
        // -------------------------------------------------------------
        private Staff find(Long id) {
                return staffRepository.findById(id)
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
                                s.getDesignation() != null ? s.getDesignation().getName() : null,
                                s.getLicenseNumber()!= null ? s.getLicenseNumber() : null,
                                s.getStaffImage());
        }

        private StaffDetailsResponse toDetailsResp(Staff s) {
                List<StaffQualification> quals = qualificationRepository.findByStaff(s);
                List<QualificationRequest> qualificationRequests = quals.stream()
                                .map(q -> new QualificationRequest(
                                                q.getQualification(),
                                                q.getSpecialization(),
                                                q.getUniversity(),
                                                q.getPassingYear(),
                                                q.getGrade()))
                                .toList();

                return new StaffDetailsResponse(
                                s.getId(),
                                s.getStaffCode(),
                                s.getFirstName(),
                                s.getLastName(),
                                s.getEmail(),
                                s.getPhone(),
                                s.getDob(),
                                s.getFatherName(),
                                s.getLicenseNumber(),
                                s.getStatus(),
                                s.getDepartment() != null ? s.getDepartment().getId() : null,
                                s.getDepartment() != null ? s.getDepartment().getName() : null,
                                s.getDesignation() != null ? s.getDesignation().getId() : null,
                                s.getDesignation() != null ? s.getDesignation().getName() : null,
                                s.getStaffImage(),
                                s.getBankName(),
                                s.getAccountHolderName(),
                                s.getAccountNumber(),
                                s.getIfscCode(),
                                s.getBranchName(),
                                s.getUpiId(),
                                s.getAddressLine1(),
                                s.getAddressLine2(),
                                s.getCity(),
                                s.getState(),
                                s.getCountry(),
                                s.getPostalCode(),
                                qualificationRequests);
        }

        public StandardResponse<?> getAllTeachers() {
                List<Staff> staffList = staffRepository.findByIsDeletedAndDesignationNameAndStatus(false, "Teacher",
                                StaffStatus.ACTIVE);

                List<StaffAllResponse> staffAllResponseList = new ArrayList<>();
                staffList.forEach(staff -> {
                        StaffAllResponse staffAllResponse = new StaffAllResponse(staff.getId(),
                                        staff.getFirstName() + " " + staff.getLastName(), staff.getDepartment().getId(),
                                        staff.getDepartment().getName());
                        staffAllResponseList.add(staffAllResponse);
                });
                return StandardResponse.success(
                                staffAllResponseList,
                                "Staff created successfully");
        }
}
