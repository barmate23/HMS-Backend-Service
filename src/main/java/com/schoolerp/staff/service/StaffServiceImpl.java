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
    private final RouteRepository routeRepository;
    private final StaffQualificationRepository qualificationRepository;
    private final PasswordEncoder encoder;
    private final DepartmentRepository deptRepo;
    private final DesignationRepository desigRepo;
    private final CodeGenerator codeGen;
    private final org.springframework.context.ApplicationContext applicationContext;

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
        // Use mobile number as password if available, otherwise generate password
        String password = (req.phone() != null && !req.phone().trim().isEmpty()) 
                ? req.phone() 
                : codeGen.generatePassword();
        
        String encodedPassword = encoder.encode(password);

        UserEntity user = UserEntity.builder()
                .email(req.email())
                .username(req.email())
                .designation(desig.getName())
                .password(encodedPassword)
                .isDefaultPasswordGenerated(true)
                .isDeleted(false)
                .isStaff(true)
                .staff(staff) // 🔥 Important
                .build();

        userRepository.save(user);

        // 8️⃣ Send Email
        sendCredentialsEmail(req, password);

        return StandardResponse.success(
                toResp(staff),
                "Staff created successfully with user account (Password: " + password + ")");
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
                "https://gateway.sarvosmi.io/api/email/send",
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
                    staff.getFirstName() + " " + staff.getLastName(), 
                    staff.getDepartment() != null ? staff.getDepartment().getId() : null,
                    staff.getDepartment() != null ? staff.getDepartment().getName() : null);
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
        String vehicleNumber = null;
        if (s.getDesignation().getName().equalsIgnoreCase("driver")) {
            Route route = routeRepository.findByDriverId(s.getId());
            vehicleNumber = route != null ? route.getVehicle().getVehicleNumber() : null;
        }
        return new StaffResponse(
                s.getId(),
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
                s.getLicenseNumber() != null ? s.getLicenseNumber() : null,
                vehicleNumber,
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
                    staff.getFirstName() + " " + staff.getLastName(), 
                    staff.getDepartment() != null ? staff.getDepartment().getId() : null,
                    staff.getDepartment() != null ? staff.getDepartment().getName() : null);
            staffAllResponseList.add(staffAllResponse);
        });
        return StandardResponse.success(
                staffAllResponseList,
                "Teachers list fetched successfully");
    }

    @Override
    public StandardResponse<?> createUserForStaff(Long staffId) {
        Staff staff;
        try {
            staff = find(staffId);
        } catch (NotFoundException ex) {
            return StandardResponse.error(
                    "Staff not found",
                    "STAFF_NOT_FOUND",
                    "id",
                    "Invalid staff id");
        }

        UserEntity user = userRepository.findByStaffId(staff.getId());
        
        // Use mobile number as password
        String encodedPassword = encoder.encode(staff.getPhone());

        if (user != null) {
            // Update existing user's password
            user.setPassword(encodedPassword);
            user.setDefaultPasswordGenerated(false);
            userRepository.save(user);
            return StandardResponse.success(
                    null,
                    "User password updated successfully to mobile number");
        }

        // Create new user if not exists
        user = UserEntity.builder()
                .email(staff.getEmail())
                .username(staff.getEmail())
                .password(encodedPassword)
                .designation(staff.getDesignation().getName())
                .isDefaultPasswordGenerated(true)
                .isDeleted(false)
                .isStaff(true)
                .staff(staff)
                .build();

        userRepository.save(user);

        return StandardResponse.success(
                null,
                "User created successfully for staff using mobile number as password");
    }

    @Override
    public byte[] downloadStaffExcelTemplate() {
        List<String> departmentNames = deptRepo.findAll().stream()
                .filter(d -> !d.getIsDelete())
                .map(Department::getName)
                .toList();

        List<String> designationNames = desigRepo.findAll().stream()
                .filter(d -> !d.isDeleted())
                .map(Designation::getName)
                .toList();

        return com.schoolerp.staff.util.StaffExcelHelper.generateStaffExcelTemplate(departmentNames, designationNames);
    }

    @Override
    public StandardResponse<?> uploadStaffExcel(org.springframework.web.multipart.MultipartFile file) {
        try {
            List<StaffExcelDto> dtoList = com.schoolerp.staff.util.StaffExcelHelper.parseExcelFile(file.getInputStream());

            java.util.concurrent.CompletableFuture.runAsync(() -> {
                StaffService selfProxy = applicationContext.getBean(StaffService.class);

                for (StaffExcelDto dto : dtoList) {
                    try {
                        Long deptId = null;
                        Department deptEntity = null;
                        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank()) {
                            String dName = dto.getDepartmentName().trim();
                            java.util.Optional<Department> deptOpt = deptRepo.findByNameIgnoreCase(dName);
                            if (deptOpt.isPresent() && !deptOpt.get().getIsDelete()) {
                                deptEntity = deptOpt.get();
                            } else {
                                String safeCode = dName.replaceAll("\\s+", "").toUpperCase();
                                safeCode = safeCode.substring(0, Math.min(safeCode.length(), 4)) + (int) (Math.random() * 10000);
                                Department newDept = Department.builder()
                                        .name(dName)
                                        .code(safeCode)
                                        .isDelete(false)
                                        .build();
                                deptEntity = deptRepo.save(newDept);
                            }
                            deptId = deptEntity.getId().longValue();
                        }

                        Long desigId = null;
                        if (dto.getDesignationName() != null && !dto.getDesignationName().isBlank() && deptEntity != null) {
                            String dName = dto.getDesignationName().trim();
                            java.util.Optional<Designation> desigOpt = desigRepo.findByNameIgnoreCaseAndDepartmentId(dName, deptEntity.getId());
                            if (desigOpt.isPresent() && !desigOpt.get().isDeleted()) {
                                desigId = desigOpt.get().getId().longValue();
                            } else {
                                Designation newDesig = Designation.builder()
                                        .name(dName)
                                        .department(deptEntity)
                                        .isDeleted(false)
                                        .teaching(false)
                                        .build();
                                newDesig = desigRepo.save(newDesig);
                                desigId = newDesig.getId().longValue();
                            }
                        }

                        java.time.LocalDate parsedDob = null;
                        if (dto.getDob() != null && !dto.getDob().isBlank()) {
                            try {
                                parsedDob = java.time.LocalDate.parse(dto.getDob());
                            } catch (Exception ignored) { }
                        }

                        StaffStatus parsedStatus = StaffStatus.ACTIVE;
                        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                            try {
                                parsedStatus = StaffStatus.valueOf(dto.getStatus().toUpperCase());
                            } catch (Exception ignored) { }
                        }

                        StaffCreateRequest request = new StaffCreateRequest(
                                dto.getFirstName(),
                                dto.getLastName(),
                                dto.getEmail(),
                                dto.getPhone(),
                                parsedDob,
                                deptId,
                                desigId,
                                dto.getFatherName(),
                                dto.getLicenseNumber(),
                                parsedStatus,
                                dto.getEmail(), // username
                                null, // staffImage
                                dto.getBankName(), dto.getAccountHolderName(), dto.getAccountNumber(), dto.getIfscCode(), dto.getBranchName(), dto.getUpiId(), // bank details
                                dto.getAddressLine1(), dto.getAddressLine2(), dto.getCity(), dto.getState(), dto.getCountry(), dto.getPostalCode(), // address details
                                null // qualifications
                        );

                        selfProxy.create(request);
                    } catch (Exception ex) {
                        System.err.println("Failed to process staff excel record for email: " + dto.getEmail() + " - " + ex.getMessage());
                    }
                }
            });

            return StandardResponse.success("Staff bulk upload started. Processing in background.");
        } catch (Exception e) {
            return StandardResponse.error("Excel processing failed", "EXCEL_ERROR", "file", e.getMessage());
        }
    }
}
