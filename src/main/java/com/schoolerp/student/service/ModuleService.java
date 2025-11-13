package com.schoolerp.student.service;

import com.schoolerp.staff.dto.module.*;
import com.schoolerp.staff.exception.*;
import com.schoolerp.staff.model.Module;
import com.schoolerp.staff.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class ModuleService {

    private final ModuleRepository repo;

    public ModuleResponse create(ModuleCreateRequest req) {
        if (repo.existsByKeyNameIgnoreCase(req.keyName()))
            throw new BadRequestException("Module key already exists");
        Module m = repo.save(Module.builder()
                .keyName(req.keyName().toUpperCase())
                .name(req.name())
                .description(req.description())
                .build());
        return toResp(m);
    }

    public Page<ModuleResponse> list(int page, int size) {
        Page<Module> p = repo.findAll(PageRequest.of(page, size, Sort.by("name")));
        return p.map(this::toResp);
    }

    public ModuleResponse update(Long id, ModuleUpdateRequest req) {
        Module m = repo.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
        m.setKeyName(req.keyName().toUpperCase());
        m.setName(req.name());
        m.setDescription(req.description());
        return toResp(repo.save(m));
    }

    public void delete(Long id) {
        Module m = repo.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
        m.setDeleted(true);
        repo.save(m);
    }

    private ModuleResponse toResp(Module m) {
        return new ModuleResponse(m.getId(), m.getKeyName(), m.getName(), m.getDescription());
    }
}
