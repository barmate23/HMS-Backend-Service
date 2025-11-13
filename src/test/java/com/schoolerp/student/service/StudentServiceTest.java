//package com.schoolerp.student.service;
//
//import com.schoolerp.student.dto.StudentRequestDto;
//import com.schoolerp.student.dto.StudentResponseDto;
//import com.schoolerp.student.dto.StudentsListResponseDto;
//import com.schoolerp.student.entity.Student;
//import com.schoolerp.student.exception.ResourceNotFoundException;
//import com.schoolerp.student.mapper.StudentMapper;
//import com.schoolerp.student.repository.StudentRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class StudentServiceTest {
//
//    @Mock
//    private StudentRepository studentRepository;
//
//    @Mock
//    private StudentMapper studentMapper;
//
//    @InjectMocks
//    private StudentService studentService;
//
//    private Student testStudent;
//    private StudentRequestDto testRequestDto;
//    private StudentResponseDto testResponseDto;
//
//    @BeforeEach
//    void setUp() {
//        testStudent = Student.builder()
//            .id(1)
//            .admissionNo("AD-2024-1001")
//            .admissionDate(LocalDate.of(2024, 4, 15))
//            .firstName("John")
//            .lastName("Doe")
//            .dateOfBirth(LocalDate.of(2010, 1, 1))
//            .gender("Male")
//            .mobileNumber("9876543210")
//            .emailId("john.doe@email.com")
//            .classApplyingFor("Class 8")
//            .section("A")
//            .academicYear("2024-25")
//            .status(Student.StudentStatus.ACTIVE)
//            .feesStatus(Student.FeesStatus.UNPAID)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//        testRequestDto = new StudentRequestDto();
//        testRequestDto.setFirstName("John");
//        testRequestDto.setLastName("Doe");
//        testRequestDto.setMobileNumber("9876543210");
//        testRequestDto.setEmailId("john.doe@email.com");
//        testRequestDto.setClassApplyingFor("Class 8");
//        testRequestDto.setSection("A");
//
//        testResponseDto = new StudentResponseDto();
//        testResponseDto.setId("test-id-123");
//        testResponseDto.setFirstName("John");
//        testResponseDto.setLastName("Doe");
//        testResponseDto.setAdmissionNo("AD-2024-1001");
//        testResponseDto.setStatus("active");
//    }
//
//    @Test
//    void getAllStudents_ShouldReturnStudentsList() {
//        // Arrange
//        List<Student> students = List.of(testStudent);
//        Page<Student> studentsPage = new PageImpl<>(students);
//
//        when(studentRepository.findStudentsWithFilters(any(), any(), any(), any(Pageable.class)))
//            .thenReturn(studentsPage);
//        when(studentMapper.toDto(any(Student.class))).thenReturn(testResponseDto);
//
//        // Act
//        StudentsListResponseDto result = studentService.getAllStudents(null, null, null, 0, 20);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.getTotal());
//        assertEquals(1, result.getStudents().size());
//        assertEquals("John", result.getStudents().get(0).getFirstName());
//    }
//
//    @Test
//    void getStudentById_ShouldReturnStudent_WhenStudentExists() {
//        // Arrange
//        when(studentRepository.findById("test-id-123")).thenReturn(Optional.of(testStudent));
//        when(studentMapper.toDto(testStudent)).thenReturn(testResponseDto);
//
//        // Act
//        StudentResponseDto result = studentService.getStudentById("test-id-123");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("test-id-123", result.getId());
//        assertEquals("John", result.getFirstName());
//    }
//
//    @Test
//    void getStudentById_ShouldThrowException_WhenStudentNotFound() {
//        // Arrange
//        when(studentRepository.findById("non-existent-id")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(ResourceNotFoundException.class,
//            () -> studentService.getStudentById("non-existent-id"));
//    }
//
//    @Test
//    void createStudent_ShouldCreateAndReturnStudent() {
//        // Arrange
//        when(studentMapper.toEntity(testRequestDto)).thenReturn(testStudent);
//        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
//        when(studentMapper.toDto(testStudent)).thenReturn(testResponseDto);
//
//        // Act
//        StudentResponseDto result = studentService.createStudent(testRequestDto);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("John", result.getFirstName());
//        verify(studentRepository).save(any(Student.class));
//    }
//
//    @Test
//    void updateStudent_ShouldUpdateAndReturnStudent() {
//        // Arrange
//        when(studentRepository.findById("test-id-123")).thenReturn(Optional.of(testStudent));
//        when(studentRepository.save(testStudent)).thenReturn(testStudent);
//        when(studentMapper.toDto(testStudent)).thenReturn(testResponseDto);
//
//        // Act
//        StudentResponseDto result = studentService.updateStudent("test-id-123", testRequestDto);
//
//        // Assert
//        assertNotNull(result);
//        verify(studentMapper).updateEntityFromDto(testRequestDto, testStudent);
//        verify(studentRepository).save(testStudent);
//    }
//
//    @Test
//    void deleteStudent_ShouldDeleteStudent_WhenStudentExists() {
//        // Arrange
//        when(studentRepository.findById("test-id-123")).thenReturn(Optional.of(testStudent));
//
//        // Act
//        studentService.deleteStudent("test-id-123");
//
//        // Assert
//        verify(studentRepository).delete(testStudent);
//    }
//
//    @Test
//    void deleteStudent_ShouldThrowException_WhenStudentNotFound() {
//        // Arrange
//        when(studentRepository.findById("non-existent-id")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(ResourceNotFoundException.class,
//            () -> studentService.deleteStudent("non-existent-id"));
//    }
//}