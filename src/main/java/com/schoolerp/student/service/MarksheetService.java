package com.schoolerp.student.service;

import com.schoolerp.student.dto.MarksheetRequestDto;
import com.schoolerp.student.entity.Marksheet;
import com.schoolerp.student.repository.MarksheetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class MarksheetService implements MarksheetServiceIMPL {

    @Autowired
    private final MarksheetRepository marksheetRepository;

    public MarksheetService(MarksheetRepository marksheetRepository) {
        this.marksheetRepository = marksheetRepository;
    }

    @Override
    public List<Marksheet> getAllMarksheets() {
        return marksheetRepository.findAll();
    }
    
    @Override
    public Marksheet createMarksheet(MarksheetRequestDto requestDto) {
        // Calculate percentage if totalMarks and maxTotalMarks are provided
        Double percentage = requestDto.getPercentage();
        if ((percentage == null || percentage == 0.0) && 
            requestDto.getTotalMarks() != null && requestDto.getMaxTotalMarks() != null && 
            requestDto.getMaxTotalMarks() > 0) {
            percentage = (requestDto.getTotalMarks().doubleValue() / requestDto.getMaxTotalMarks()) * 100;
        }
        
        // Calculate grade based on percentage
        String grade = requestDto.getGrade();
        if (grade == null || grade.isEmpty()) {
            grade = calculateGrade(percentage != null ? percentage : 0.0);
        }
        
        Marksheet marksheet = Marksheet.builder()
            .studentId(requestDto.getStudentId())
            .examType(requestDto.getExamType())
            .academicYear(requestDto.getAcademicYear())
            .className(requestDto.getClassName())
            .section(requestDto.getSection())
            .subjects(requestDto.getSubjects())
            .totalMarks(requestDto.getTotalMarks())
            .maxTotalMarks(requestDto.getMaxTotalMarks())
            .percentage(percentage)
            .grade(grade)
            .rank(requestDto.getRank())
            .result(requestDto.getResult())
            .status(Marksheet.MarksheetStatus.valueOf(requestDto.getStatus().toUpperCase()))
            .publishDate(requestDto.getPublishDate())
            .build();
        
        Marksheet savedMarksheet = marksheetRepository.save(marksheet);
        
        log.info("Created marksheet for student: {} for {} exam", 
                 requestDto.getStudentId(), requestDto.getExamType());
        
        return savedMarksheet;
    }
    
    @Override
    public List<Marksheet> getMarksheetsByStudentId(String studentId) {
        return marksheetRepository.findByStudentId(studentId);
    }
    
    private String calculateGrade(Double percentage) {
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B+";
        else if (percentage >= 60) return "B";
        else if (percentage >= 50) return "C+";
        else if (percentage >= 40) return "C";
        else if (percentage >= 33) return "D";
        else return "F";
    }
}