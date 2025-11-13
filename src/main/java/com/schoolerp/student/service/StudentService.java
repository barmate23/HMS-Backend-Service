package com.schoolerp.student.service;

import com.schoolerp.student.common.LogContext;
import com.schoolerp.student.constants.ServiceConstants;
import com.schoolerp.student.dto.*;
import com.schoolerp.student.entity.*;
import com.schoolerp.student.exception.CustomException;
import com.schoolerp.student.exception.ResourceNotFoundException;
import com.schoolerp.student.mapper.StudentMapper;
import com.schoolerp.student.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
class StudentService implements StudentServiceImpl {
    
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentPromotionMapperRepository studentPromotionMapperRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final StudentDocumentRepository studentDocumentRepository;


    StudentService(StudentRepository studentRepository, StudentMapper studentMapper, FeeStructureRepository feeStructureRepository, StudentPromotionMapperRepository studentPromotionMapperRepository, CommonMasterRepository commonMasterRepository, StudentDocumentRepository studentDocumentRepository) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.feeStructureRepository = feeStructureRepository;
        this.studentPromotionMapperRepository = studentPromotionMapperRepository;
        this.commonMasterRepository = commonMasterRepository;
        this.studentDocumentRepository = studentDocumentRepository;
    }

    @Override
    public StudentsListResponseDto getAllStudents(String search, Integer status, Integer classFilter,
                                                  Integer page, Integer size) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting getAllStudents with search: {}, status: {}, class: {}, page: {}, size: {}", 
                 logId, search, status, classFilter, page, size);
        
        try {
            
            Pageable pageable = PageRequest.of(
                page != null ? page : 0, 
                size != null ? size : 20, 
                Sort.by("createdAt").descending()
            );
            
            Page<Student> studentsPage = studentRepository.findStudentsWithFilters(
                search, status, classFilter, pageable
            );
            
            List<StudentResponseDto> studentDtos = studentsPage.getContent()
                .stream()
                .map(studentMapper::toDto)
                .toList();
            
            StudentsListResponseDto result = new StudentsListResponseDto(studentDtos, studentsPage.getTotalElements());
            
            log.info("logId: {} - Successfully retrieved {} students out of {} total", 
                     logId, studentDtos.size(), studentsPage.getTotalElements());
            
            return result;
            
        } catch (Exception e) {
            log.error("logId: {} - Error retrieving students: {}", logId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public StudentResponseDto getStudentById(Integer id) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting getStudentById for id: {}", logId, id);

        try {
            Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> {
                        log.warn("logId: {} - Student not found with id: {}", logId, id);
                        return new ResourceNotFoundException("Student not found with id: " + id);
                    });

            // Map student to DTO
            StudentResponseDto result = studentMapper.toDto(student);

            // 🔹 Attach student documents
            List<StudentDocument> documents = studentDocumentRepository.findByStudentIdAndIsDeletedFalse(id);
            List<StudentDocumentDto> documentDtos = documents.stream()
                    .map(doc -> new StudentDocumentDto(
                            doc.getId(),
                            doc.getDocumentType(),
                            doc.getDocumentName(),
                            doc.getS3Url()
                    ))
                    .toList();
            result.setStudentDocuments(documentDtos);

            // 🔹 Fetch and attach FeeStructure
            if (student.getClassApplyingFor() != null && student.getAcademicYear() != null) {
                Optional<FeeStructure> feeStructureOpt = feeStructureRepository.findByClassId_IdAndAcademicYear_IdAndIsDeletedFalse(
                        student.getClassApplyingFor(),
                        student.getAcademicYear() // assuming it's an object; adjust if it's just ID
                );

                feeStructureOpt.ifPresent(feeStructure -> {
                    FeeStructureResponse feeStructureDto = StudentMapper.toFeeStructureResponse(feeStructure);
                    result.setFeeStructureResponse(feeStructureDto);
                });
            }

            log.info("logId: {} - Successfully retrieved student: {} {}", logId, student.getFirstName(), student.getLastName());

            return result;

        } catch (Exception e) {
            log.error("logId: {} - Error retrieving student with id {}: {}", logId, id, e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public StudentResponseDto createStudent(StudentRequestDto requestDto) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting createStudent for: {} {}", 
                 logId, requestDto.getFirstName(), requestDto.getLastName());
        
        try {
            // Generate admission number if not provided
            if (requestDto.getAdmissionNo() == null || requestDto.getAdmissionNo().isEmpty()) {
                requestDto.setAdmissionNo(generateAdmissionNumber());
                log.debug("logId: {} - Generated admission number: {}", logId, requestDto.getAdmissionNo());
            } else if (studentRepository.existsByAdmissionNo(requestDto.getAdmissionNo())) {
                log.warn("logId: {} - Admission number already exists: {}", logId, requestDto.getAdmissionNo());
                throw new CustomException(
                    "Admission number already exists: " + requestDto.getAdmissionNo(), ServiceConstants.ERROR_CODE, ServiceConstants.DUPLICATE_ADMISSION_NO
                );
            }

            // Handle same address logic
            if (requestDto.getSameAsCurrentAddress() != null && requestDto.getSameAsCurrentAddress()) {
                requestDto.setPermanentAddress(requestDto.getCurrentAddress());
                log.debug("logId: {} - Using current address as permanent address", logId);
            }
            
            Student student = studentMapper.toEntity(requestDto);
            
            Student savedStudent = studentRepository.save(student);
            
            log.info("logId: {} - Successfully created student with ID: {} and admission number: {}", 
                     logId, savedStudent.getId(), savedStudent.getAdmissionNo());
            
            return studentMapper.toDto(savedStudent);
            
        } catch (Exception e) {
            log.error("logId: {} - Error creating student {} {}: {}", 
                      logId, requestDto.getFirstName(), requestDto.getLastName(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public StudentResponseDto updateStudent(Integer id, StudentRequestDto requestDto) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting updateStudent for id: {}", logId, id);
        
        try {
            Student existingStudent = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("logId: {} - Student not found for update with id: {}", logId, id);
                    return new ResourceNotFoundException("Student not found with id: " + id);
                });
            
            log.debug("logId: {} - Found existing student: {} {}", 
                      logId, existingStudent.getFirstName(), existingStudent.getLastName());
            
            // Handle same address logic
            if (requestDto.getSameAsCurrentAddress() != null && requestDto.getSameAsCurrentAddress()) {
                requestDto.setPermanentAddress(requestDto.getCurrentAddress());
                log.debug("logId: {} - Using current address as permanent address for update", logId);
            }
            
            studentMapper.updateEntityFromDto(requestDto, existingStudent);
            Student updatedStudent = studentRepository.save(existingStudent);
            
            log.info("logId: {} - Successfully updated student with ID: {} - {} {}", 
                     logId, updatedStudent.getId(), updatedStudent.getFirstName(), updatedStudent.getLastName());
            
            return studentMapper.toDto(updatedStudent);
            
        } catch (Exception e) {
            log.error("logId: {} - Error updating student with id {}: {}", logId, id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void deleteStudent(Integer id) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting deleteStudent for id: {}", logId, id);
        
        try {
            Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("logId: {} - Student not found for deletion with id: {}", logId, id);
                    return new ResourceNotFoundException("Student not found with id: " + id);
                });
            
            log.debug("logId: {} - Found student for deletion: {} {}", 
                      logId, student.getFirstName(), student.getLastName());
            
            student.setDeleted(true);
            studentRepository.save(student);

            log.info("logId: {} - Successfully soft deleted student with ID: {} - {} {}",
                     logId, id, student.getFirstName(), student.getLastName());
            
        } catch (Exception e) {
            log.error("logId: {} - Error deleting student with id {}: {}", logId, id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void promoteStudents(PromoteStudentsRequestDto requestDto) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - Starting promoteStudents from {}-{} to {}-{} for {} students", 
                 logId, requestDto.getFromClass(), requestDto.getFromSection(),
                 requestDto.getToClass(), requestDto.getToSection(), requestDto.getStudentIds().size());
        try {
            List<Student> studentsToPromote = studentRepository.findAllByIdInAndIsDeletedFalse(requestDto.getStudentIds());
            if (studentsToPromote.isEmpty()) {
                log.warn("logId: {} - No students found with provided IDs: {}", logId, requestDto.getStudentIds());
                throw new ResourceNotFoundException("No students found with provided IDs");
            }
            log.debug("logId: {} - Found {} students for promotion", logId, studentsToPromote.size());
            for (Student student : studentsToPromote) {
                log.debug("logId: {} - Promoting student: {} {} from {}-{} to {}-{}", 
                          logId, student.getFirstName(), student.getLastName(),
                          student.getClassApplyingFor(), student.getSection(),
                          requestDto.getToClass(), requestDto.getToSection());
                // Save promotion details in mapper table
                StudentPromotionMapper promotion = new StudentPromotionMapper();
                promotion.setStudentId(student.getId().longValue());
                promotion.setFromClass(student.getClassApplyingFor());
                promotion.setFromSection(student.getSection());
                promotion.setToClass(requestDto.getToClass());
                promotion.setToSection(requestDto.getToSection());
                promotion.setAcademicYear(requestDto.getAcademicYear());
                promotion.setPromotionDate(LocalDateTime.now());
                // Fetch orgId (assume from student or requestDto, update as needed)
                Integer statusId = null;
                if (requestDto.isMarkAsAlumni()) {
                    statusId = commonMasterRepository.findByCommonMasterKeyAndDataAndAndStatus("STATUS","ALUMNI",true)
                        .map(CommonMaster::getId)
                        .orElseThrow(() -> new CustomException("ALUMNI status not found in CommonMaster", ServiceConstants.ERROR_CODE, "ALUMNI_STATUS_NOT_FOUND"));
                } else {
                    statusId = commonMasterRepository.findByCommonMasterKeyAndDataAndAndStatus("STATUS",requestDto.getStatus() ,true)
                        .map(CommonMaster::getId)
                        .orElseThrow(() -> new CustomException("PROMOTED status not found in CommonMaster", ServiceConstants.ERROR_CODE, "PROMOTED_STATUS_NOT_FOUND"));
                }
                promotion.setStatus(statusId);
                studentPromotionMapperRepository.save(promotion);
                // Update student details
                student.setClassApplyingFor(requestDto.getToClass());
                student.setSection(requestDto.getToSection());
                student.setAcademicYear(requestDto.getAcademicYear());
                student.setStatus(statusId);
                if (requestDto.isMarkAsAlumni()) {
                    log.debug("logId: {} - Marked student {} {} as alumni",
                              logId, student.getFirstName(), student.getLastName());
                }
            }
            studentRepository.saveAll(studentsToPromote);
            log.info("logId: {} - Successfully promoted {} students from {}-{} to {}-{}",
                     logId, studentsToPromote.size(), 
                     requestDto.getFromClass(), requestDto.getFromSection(),
                     requestDto.getToClass(), requestDto.getToSection());
            // TODO: Send notifications if requestDto.isSendNotification() is true
            if (requestDto.isSendNotification()) {
                log.info("logId: {} - Notification sending requested but not yet implemented", logId);
            }
        } catch (Exception e) {
            log.error("logId: {} - Error promoting students: {}", logId, e.getMessage(), e);
            throw e;
        }
    }
    
    private String generateAdmissionNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String randomSuffix = String.valueOf((int) (Math.random() * 9000) + 1000);
        return "AD-" + year + "-" + randomSuffix;
    }
}