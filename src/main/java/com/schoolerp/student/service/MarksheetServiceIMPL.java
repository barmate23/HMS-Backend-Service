package com.schoolerp.student.service;

import com.schoolerp.student.dto.MarksheetRequestDto;
import com.schoolerp.student.entity.Marksheet;

import java.util.List;

/**
 * Interface for Marksheet Service operations
 * Defines contract for student marksheet management functionality
 */
public interface MarksheetServiceIMPL {
    
    /**
     * Retrieves all marksheets in the system
     * 
     * @return List of Marksheet entities
     */
    List<Marksheet> getAllMarksheets();
    
    /**
     * Creates a new marksheet for a student
     * 
     * @param requestDto Marksheet creation request data
     * @return Marksheet entity containing created marksheet details
     */
    Marksheet createMarksheet(MarksheetRequestDto requestDto);
    
    /**
     * Retrieves all marksheets for a specific student
     * 
     * @param studentId Student unique identifier
     * @return List of Marksheet entities for the student
     */
    List<Marksheet> getMarksheetsByStudentId(String studentId);
}