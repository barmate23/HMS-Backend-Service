package com.schoolerp.staff.service;

import com.schoolerp.staff.common.StandardResponse;
import com.schoolerp.staff.dto.*;
import com.schoolerp.staff.entity.*;
import com.schoolerp.staff.repository.*;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public interface PermissionService {


    /**
     * List permissions of a specific role
     */
    public StandardResponse<Page<PermissionResponse>> listByRole(Long roleId, int page, int size);

    /**
     * Save or update all permissions of a role
     */
    public StandardResponse<Void> saveAll(Long roleId, List<PermissionRequest> list);

    /**
     * Get all modules with submodules
     */
    public StandardResponse<List<ModuleWithSubmodulesResponse>> getAllModulesWithSubmodules();

    public StandardResponse getUserPermission();



}