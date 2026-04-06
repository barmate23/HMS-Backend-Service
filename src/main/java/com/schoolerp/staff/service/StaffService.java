package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface StaffService {

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    public StandardResponse create(StaffCreateRequest req);

    // -------------------------------------------------------------
    // SEARCH (Paginated)
    // -------------------------------------------------------------
    public StandardResponse<PageResponse<StaffResponse>> search(
            Long deptId,
            String designation, StaffStatus status,
            String q,
            int page,
            int size
    );

    // -------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------
    public StandardResponse get(Long id);

    public StandardResponse<?> getAllStaff();

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    public StandardResponse update(Long id, StaffUpdateRequest req);

    // -------------------------------------------------------------
    // DELETE (Soft Delete)
    // -------------------------------------------------------------
    public StandardResponse<Void> delete(Long id);


    StandardResponse<?> getAllTeachers();

    StandardResponse<?> createUserForStaff(Long staffId);
}
