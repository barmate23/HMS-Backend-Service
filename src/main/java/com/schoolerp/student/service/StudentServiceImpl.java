package com.schoolerp.student.service;

import com.schoolerp.student.dto.*;

/**
 * Interface for Student Service operations
 * Defines contract for student management functionality
 */
public interface StudentServiceImpl {
    
    /**
     * Retrieves all students with optional filtering and pagination
     * 
     * @param search Search term for student name or admission number
     * @param status Student status filter (active, inactive, etc.)
     * @param classFilter Class filter
     * @param page Page number for pagination (0-based)
     * @param size Number of records per page
     * @return StudentsListResponseDto containing students list and total count
     */
    StudentsListResponseDto getAllStudents(String search, Integer status, Integer classFilter,
                                          Integer page, Integer size);
    
    /**
     * Retrieves a student by their unique ID
     * 
     * @param id Student unique identifier
     * @return StudentResponseDto containing student details
     * @throws com.schoolerp.student.exception.ResourceNotFoundException if student not found
     */
    StudentResponseDto getStudentById(Integer id);
    
    /**
     * Creates a new student record
     * 
     * @param requestDto Student creation request data
     * @return StudentResponseDto containing created student details
     */
    StudentResponseDto createStudent(StudentRequestDto requestDto);
    
    /**
     * Updates an existing student record
     * 
     * @param id Student unique identifier
     * @param requestDto Student update request data
     * @return StudentResponseDto containing updated student details
     * @throws com.schoolerp.student.exception.ResourceNotFoundException if student not found
     */
    StudentResponseDto updateStudent(Integer id, StudentRequestDto requestDto);
    
    /**
     * Deletes a student record
     * 
     * @param id Student unique identifier
     * @throws com.schoolerp.student.exception.ResourceNotFoundException if student not found
     */
    void deleteStudent(Integer id);
    
    /**
     * Promotes multiple students from one class/section to another
     * 
     * @param requestDto Promotion request containing student IDs and target class/section
     * @throws com.schoolerp.student.exception.ResourceNotFoundException if students not found
     */
    void promoteStudents(PromoteStudentsRequestDto requestDto);
}