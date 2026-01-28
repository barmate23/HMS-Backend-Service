package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DesignationCreateRequest;
import com.schoolerp.staff.dto.DesignationResponse;
import com.schoolerp.staff.dto.DesignationUpdateRequest;
import com.schoolerp.staff.dto.PageResponse;
import com.schoolerp.staff.entity.Department;
import com.schoolerp.staff.entity.Designation;
import com.schoolerp.staff.repository.DepartmentRepository;
import com.schoolerp.staff.repository.DesignationRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface DesignationService {


    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(DesignationCreateRequest req);

    // -------------------------------------------------------------
    // LIST BY DEPARTMENT (Pagination)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<DesignationResponse>> listByDepartment(Long departmentId, int page, int size);

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse<DesignationResponse> get(Long id);

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, DesignationUpdateRequest req);

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id);

}
