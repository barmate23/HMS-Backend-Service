package com.schoolerp.student.mapper;

import com.schoolerp.student.dto.FeeStructureResponse;
import com.schoolerp.student.dto.StudentRequestDto;
import com.schoolerp.student.dto.StudentResponseDto;
import com.schoolerp.student.entity.FeeStructure;
import com.schoolerp.student.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequestDto dto) {
        if (dto == null) return null;
        Student student = new Student();
        student.setId(null); // auto-generated
        student.setAdmissionNo(dto.getAdmissionNo());
        student.setAdmissionDate(dto.getAdmissionDate());
        student.setFirstName(dto.getFirstName());
        student.setMiddleName(dto.getMiddleName());
        student.setLastName(dto.getLastName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setBloodGroup(dto.getBloodGroup());
        student.setAadharNumber(dto.getAadharNumber());
        student.setCaste(dto.getCaste());
        student.setReligion(dto.getReligion());
        student.setNationality(dto.getNationality());
        student.setMotherTongue(dto.getMotherTongue());
        student.setCurrentAddress(dto.getCurrentAddress());
        student.setPermanentAddress(dto.getPermanentAddress());
        student.setCity(dto.getCity());
        student.setState(dto.getState());
        student.setPincode(dto.getPincode());
        student.setMobileNumber(dto.getMobileNumber());
        student.setAlternateContact(dto.getAlternateContact());
        student.setEmailId(dto.getEmailId());
        student.setFatherName(dto.getFatherName());
        student.setMotherName(dto.getMotherName());
        student.setGuardianName(dto.getGuardianName());
        student.setFatherOccupation(dto.getFatherOccupation());
        student.setMotherOccupation(dto.getMotherOccupation());
        student.setAnnualIncome(dto.getAnnualIncome());
        student.setClassApplyingFor(dto.getClassApplyingFor());
        student.setSection(dto.getSection());
        student.setAcademicYear(dto.getAcademicYear());
        student.setPreviousSchoolName(dto.getPreviousSchoolName());
        student.setTransferCertificateNo(dto.getTransferCertificateNo());
        student.setPreviousClassPassed(dto.getPreviousClassPassed());
        student.setBoard(dto.getBoard());
        student.setMediumOfInstruction(dto.getMediumOfInstruction());
//        student.setDocuments(dto.getDocuments());
        student.setStatus(dto.getStatus());
//        student.setFeesStatus(mapFeesStatus(dto.getFeesStatus()));
        return student;
    }

    public StudentResponseDto toDto(Student entity) {
        if (entity == null) return null;
        StudentResponseDto dto = new StudentResponseDto();
        dto.setId(entity.getId());
        dto.setAdmissionNo(entity.getAdmissionNo());
        dto.setAdmissionDate(entity.getAdmissionDate());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setBloodGroup(entity.getBloodGroup());
        dto.setAadharNumber(entity.getAadharNumber());
        dto.setCaste(entity.getCaste());
        dto.setReligion(entity.getReligion());
        dto.setNationality(entity.getNationality());
        dto.setMotherTongue(entity.getMotherTongue());
        dto.setCurrentAddress(entity.getCurrentAddress());
        dto.setPermanentAddress(entity.getPermanentAddress());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPincode(entity.getPincode());
        dto.setMobileNumber(entity.getMobileNumber());
        dto.setAlternateContact(entity.getAlternateContact());
        dto.setEmailId(entity.getEmailId());
        dto.setFatherName(entity.getFatherName());
        dto.setMotherName(entity.getMotherName());
        dto.setGuardianName(entity.getGuardianName());
        dto.setFatherOccupation(entity.getFatherOccupation());
        dto.setMotherOccupation(entity.getMotherOccupation());
        dto.setAnnualIncome(entity.getAnnualIncome());
        dto.setClassApplyingFor(entity.getClassApplyingFor());
        dto.setSection(entity.getSection());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setPreviousSchoolName(entity.getPreviousSchoolName());
        dto.setTransferCertificateNo(entity.getTransferCertificateNo());
        dto.setPreviousClassPassed(entity.getPreviousClassPassed());
        dto.setBoard(entity.getBoard());
        dto.setMediumOfInstruction(entity.getMediumOfInstruction());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : null);
        dto.setFeesStatus(entity.getFeesStatus() != null ? entity.getFeesStatus().name().toLowerCase() : null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public void updateEntityFromDto(StudentRequestDto dto, Student entity) {
        if (dto == null || entity == null) return;
//        entity.setAdmissionNo(dto.getAdmissionNo());
//        entity.setAdmissionDate(dto.getAdmissionDate());
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setGender(dto.getGender());
        entity.setBloodGroup(dto.getBloodGroup());
        entity.setAadharNumber(dto.getAadharNumber());
        entity.setCaste(dto.getCaste());
        entity.setReligion(dto.getReligion());
        entity.setNationality(dto.getNationality());
        entity.setMotherTongue(dto.getMotherTongue());
        entity.setCurrentAddress(dto.getCurrentAddress());
        entity.setPermanentAddress(dto.getPermanentAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPincode(dto.getPincode());
        entity.setMobileNumber(dto.getMobileNumber());
        entity.setAlternateContact(dto.getAlternateContact());
        entity.setEmailId(dto.getEmailId());
        entity.setFatherName(dto.getFatherName());
        entity.setMotherName(dto.getMotherName());
        entity.setGuardianName(dto.getGuardianName());
        entity.setFatherOccupation(dto.getFatherOccupation());
        entity.setMotherOccupation(dto.getMotherOccupation());
        entity.setAnnualIncome(dto.getAnnualIncome());
        entity.setClassApplyingFor(dto.getClassApplyingFor());
        entity.setSection(dto.getSection());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setPreviousSchoolName(dto.getPreviousSchoolName());
        entity.setTransferCertificateNo(dto.getTransferCertificateNo());
        entity.setPreviousClassPassed(dto.getPreviousClassPassed());
        entity.setBoard(dto.getBoard());
        entity.setMediumOfInstruction(dto.getMediumOfInstruction());
//        entity.setDocuments(dto.getDocuments());
        entity.setStatus(dto.getStatus());
//        entity.setFeesStatus(mapFeesStatus(dto.getFeesStatus()));
        // Do not update id, createdAt, updatedAt (audited automatically)
    }


    private Student.FeesStatus mapFeesStatus(String feesStatus) {
        if (feesStatus == null || feesStatus.isEmpty()) return Student.FeesStatus.UNPAID;
        try {
            return Student.FeesStatus.valueOf(feesStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Student.FeesStatus.UNPAID;
        }
    }


    public static FeeStructureResponse toFeeStructureResponse(FeeStructure feeStructure) {
        if (feeStructure == null) return null;

        FeeStructureResponse response = new FeeStructureResponse();

        response.setId(feeStructure.getId());

        // Class
        if (feeStructure.getClassId() != null) {
            response.setClassId((long) feeStructure.getClassId().getId());
            response.setClassName(feeStructure.getClassId().getData());
        }

        // Section
        if (feeStructure.getSectionId() != null) {
            response.setSectionId((long) feeStructure.getSectionId().getId());
            response.setSectionName(feeStructure.getSectionId().getData());
        }

        // Academic Year
        if (feeStructure.getAcademicYear() != null) {
            response.setAcademicYearId((long) feeStructure.getAcademicYear().getId());
            response.setAcademicYearName(feeStructure.getAcademicYear().getData());
        }

        // Payment Frequency
        if (feeStructure.getPaymentFrequency() != null) {
            response.setPaymentFrequencyId((long) feeStructure.getPaymentFrequency().getId());
            response.setPaymentFrequencyName(feeStructure.getPaymentFrequency().getData());
        }

        // Fee amounts
        response.setFeeStructureName(feeStructure.getFeeStructureName());
        response.setTuitionFee(feeStructure.getTuitionFee());
        response.setAdmissionFee(feeStructure.getAdmissionFee());
        response.setTransportFee(feeStructure.getTransportFee());
        response.setLibraryFee(feeStructure.getLibraryFee());
        response.setExamFee(feeStructure.getExamFee());
        response.setSportsFee(feeStructure.getSportsFee());
        response.setLabFee(feeStructure.getLabFee());
        response.setDevelopmentFee(feeStructure.getDevelopmentFee());
        response.setTotalFee(feeStructure.getTotalFee());
        response.setMaxDiscount(feeStructure.getMaxDiscount());
        response.setLateFeePenalty(feeStructure.getLateFeePenalty());

        // Dates
        response.setEffectiveFrom(feeStructure.getEffectiveFrom());
        response.setDueDate(feeStructure.getDueDate());

        // Audit
        response.setIsDeleted(feeStructure.getIsDeleted());
        response.setCreatedBy(feeStructure.getCreatedBy());
        response.setCreatedAt(feeStructure.getCreatedAt());
        response.setUpdatedBy(feeStructure.getUpdatedBy());
        response.setUpdatedAt(feeStructure.getUpdatedAt());

        return response;
    }
}

