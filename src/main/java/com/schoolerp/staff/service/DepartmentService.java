package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.DepartmentCreateRequest;
import com.schoolerp.staff.dto.DepartmentResponse;
import com.schoolerp.staff.dto.DepartmentUpdateRequest;
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

import java.util.List;

@Service
@Transactional
public interface DepartmentService {

  // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(DepartmentCreateRequest req);

    // -------------------------------------------------------------
    // SEARCH (Paginated)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<DepartmentResponse>> search(String q, int page, int size);

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse<DepartmentResponse> get(Long id);

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, DepartmentUpdateRequest req);

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id);


}
