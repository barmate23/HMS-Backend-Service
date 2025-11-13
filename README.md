# Student Management Microservice

A comprehensive Spring Boot microservice for managing student data, ID cards, marksheets, and documents in the School ERP system.

## Features

### Core Student Management
- Complete student CRUD operations with comprehensive data model
- Advanced search and filtering capabilities (by name, status, class)
- Pagination support for large datasets
- Student promotion across classes and academic years
- Automatic admission number generation

### ID Card Management
- Issue new ID cards with auto-generated card numbers
- QR code generation for security
- Track validity periods and status
- Photo URL management

### Marksheet Management
- Create and manage student marksheets
- Automatic grade calculation based on percentage
- Support for different exam types and academic years
- Rank tracking within class/section

### Document Management
- Upload and manage various student documents
- Auto-generated verification codes
- Document categorization and status tracking
- Issue date and validity tracking

## API Endpoints

### Student Operations
- `GET /api/students` - List all students with filtering
  - Query params: `search`, `status`, `class`, `page`, `size`
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create new student
- `PUT /api/students/{id}` - Update existing student
- `DELETE /api/students/{id}` - Delete student
- `POST /api/students/promote` - Promote multiple students

### ID Card Operations
- `GET /api/students/id-cards` - List all ID cards
- `POST /api/students/id-cards` - Issue new ID card

### Marksheet Operations
- `GET /api/students/marksheets` - List all marksheets
- `POST /api/students/marksheets` - Create marksheet

### Document Operations
- `GET /api/students/documents` - List all documents
- `POST /api/students/documents` - Upload document

## Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Bean Validation
- **Mapping**: MapStruct
- **Build Tool**: Maven
- **Documentation**: Spring Boot Actuator
- **Architecture**: Interface-Implementation Design Pattern
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Architecture Pattern

### Interface-Implementation Design
The microservice follows enterprise-grade **Interface-Implementation Design Pattern** for:
- **Better Testability**: Easy mocking of service interfaces
- **Maintainability**: Clear separation of contracts and implementations  
- **Flexibility**: Easy to swap implementations (caching, different storage)
- **SOLID Principles**: Follows Dependency Inversion Principle

### Service Layer Structure
```
Controllers → Service Interfaces → Service Implementations → Repositories
```

**Service Interfaces:**
- `IStudentService` - Student management contract
- `IIdCardService` - ID card operations contract  
- `IMarksheetService` - Academic records contract
- `IStudentDocumentService` - Document management contract

**Service Implementations:**
- `StudentService implements IStudentService`
- `IdCardService implements IIdCardService`
- `MarksheetService implements IMarksheetService`
- `StudentDocumentService implements IStudentDocumentService`

## Data Model

### Student Entity
Complete student information including:
- Personal details (name, DOB, contact info)
- Academic information (class, section, academic year)
- Family details (parents, guardian information)
- Address information with same-as-current-address support
- Status tracking and fee status
- Document attachments

### Related Entities
- **IdCard**: Student ID card management with QR codes
- **Marksheet**: Academic performance tracking
- **StudentDocument**: Document management with verification

## Configuration

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/school_erp_students
    username: school_user
    password: school_password
```

### Profile-based Configuration
- `dev`: Development environment with detailed logging
- `prod`: Production environment with optimized settings
- Default: Standard configuration

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL database
- Maven 3.6+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd student-service
   ```

2. **Setup Database**
   ```sql
   CREATE DATABASE school_erp_students;
   CREATE USER school_user WITH PASSWORD 'school_password';
   GRANT ALL PRIVILEGES ON DATABASE school_erp_students TO school_user;
   ```

3. **Run with Maven**
   ```bash
   # Development mode
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Production mode
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

4. **Build JAR**
   ```bash
   mvn clean package
   java -jar target/student-service-1.0.0.jar
   ```

### API Documentation
Access the application health endpoint at: `http://localhost:8080/actuator/health`

### Sample API Calls

#### Create Student
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "2010-01-15",
    "mobileNumber": "9876543210",
    "emailId": "john.doe@email.com",
    "classApplyingFor": "Class 8",
    "section": "A"
  }'
```

#### Get Students with Filters
```bash
curl "http://localhost:8080/api/students?search=john&status=active&class=Class%208&page=0&size=10"
```

## Field Names Compatibility
This microservice maintains exact field name compatibility with the existing frontend system. All API responses match the expected JSON structure exactly as specified in the requirements.

## Error Handling
Comprehensive error handling with:
- Custom exception classes
- Global exception handler
- Detailed validation error responses
- Appropriate HTTP status codes

## Logging
Structured logging with different levels for development and production environments. Log files are automatically created in the `logs/` directory.

## Security Considerations
- Input validation on all endpoints
- SQL injection prevention through JPA
- Proper error message handling to avoid information leakage
- Database connection pooling for performance

## Monitoring
Built-in health checks and metrics through Spring Boot Actuator endpoints for production monitoring and alerting.