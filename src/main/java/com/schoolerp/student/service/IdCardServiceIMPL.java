package com.schoolerp.student.service;

import com.schoolerp.student.dto.IdCardRequestDto;
import com.schoolerp.student.entity.IdCard;

import java.util.List;

/**
 * Interface for ID Card Service operations
 * Defines contract for student ID card management functionality
 */
public interface IdCardServiceIMPL {
    
    /**
     * Retrieves all ID cards in the system
     * 
     * @return List of IdCard entities
     */
    List<IdCard> getAllIdCards();
    
    /**
     * Creates a new ID card for a student
     * 
     * @param requestDto ID card creation request data
     * @return IdCard entity containing created ID card details
     */
    IdCard createIdCard(IdCardRequestDto requestDto);
    
    /**
     * Retrieves all ID cards for a specific student
     * 
     * @param studentId Student unique identifier
     * @return List of IdCard entities for the student
     */
    List<IdCard> getIdCardsByStudentId(String studentId);
}