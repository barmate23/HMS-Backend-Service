package com.schoolerp.student.service;

import com.schoolerp.staff.dto.role.*;
import com.schoolerp.staff.exception.*;
import com.schoolerp.staff.model.Role;
import com.schoolerp.staff.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class RoleService {

    private final RoleRepository repo;

    public RoleResponse create(RoleCreateRequest req) {
        if (repo.existsByNameIgnoreCase(req.name()))
            throw new BadRequestException("Role name already exists");
        Role r = repo.save(Role.builder().name(req.name()).description(req.description()).build());
        return toResp(r);
    }

    public Page<RoleResponse> list(int page, int size) {
        Page<Role> p = repo.findAll(PageRequest.of(page, size, Sort.by("name")));
        return p.map(this::toResp);
    }

    public RoleResponse get(Long id) {
        return toResp(find(id));
    }

    public RoleResponse update(Long id, RoleUpdateRequest req) {
        Role r = find(id);
        if (!r.getName().equalsIgnoreCase(req.name()) && repo.existsByNameIgnoreCase(req.name()))
            throw new BadRequestException("Role name already exists");
        r.setName(req.name());
        r.setDescription(req.description());
        return toResp(repo.save(r));
    }

    public void delete(Long id) {
        Role r = find(id);
        r.setDeleted(true);
        repo.save(r);
    }

    private Role find(Long id) {
        return repo.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private RoleResponse toResp(Role r) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription());
    }
}
