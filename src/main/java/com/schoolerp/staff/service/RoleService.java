package com.schoolerp.staff.service;


import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.RoleCreateRequest;
import com.schoolerp.staff.dto.RoleResponse;
import com.schoolerp.staff.dto.RoleUpdateRequest;
import com.schoolerp.staff.dto.StaffResponse;
import com.schoolerp.staff.entity.Role;
import com.schoolerp.staff.entity.RoleStaffMapper;
import com.schoolerp.staff.entity.UserEntity;
import com.schoolerp.staff.repository.RoleRepository;
import com.schoolerp.staff.repository.RoleStaffMapperRepository;
import com.schoolerp.staff.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public interface RoleService {


    public StandardResponse create(RoleCreateRequest req);

    public StandardResponse<Page<RoleResponse>> list(int page, int size);

    public StandardResponse get(Long id);

    public StandardResponse update(Long id, RoleUpdateRequest req);

    public StandardResponse<Void> delete(Long id);


}
