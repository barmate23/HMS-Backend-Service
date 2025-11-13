package com.schoolerp.student.service;


import com.schoolerp.student.dto.DepartmentCreateRequest;
import com.schoolerp.student.dto.DepartmentResponse;
import com.schoolerp.student.dto.DepartmentUpdateRequest;
import com.schoolerp.student.entity.Department;
import com.schoolerp.student.repository.DepartmentRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentResponse create(DepartmentCreateRequest req) {

        if (repository.existsByNameIgnoreCase(req.name()))
            throw new BadRequestException("Department name already exists");

        if (repository.existsByCodeIgnoreCase(req.code()))
            throw new BadRequestException("Department code already exists");

        Department d = Department.builder()
                .name(req.name())
                .code(req.code())
                .description(req.description())
                .hodId(req.hodId())
                .build();

        d = repository.save(d);

        return toResp(d);
    }

    public PageResponse<DepartmentResponse> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Department> res = repository.search(q, pageable);
        return PageResponse.from(res.map(this::toResp));
    }

    public DepartmentResponse get(Long id) {
        return toResp(find(id));
    }

    public DepartmentResponse update(Long id, DepartmentUpdateRequest req) {
        Department d = find(id);

        if (!d.getName().equalsIgnoreCase(req.name()) &&
                repository.existsByNameIgnoreCase(req.name()))
            throw new BadRequestException("Department name already exists");

        if (!d.getCode().equalsIgnoreCase(req.code()) &&
                repository.existsByCodeIgnoreCase(req.code()))
            throw new BadRequestException("Department code already exists");

        d.setName(req.name());
        d.setCode(req.code());
        d.setDescription(req.description());
        d.setHodId(req.hodId());

        return toResp(repository.save(d));
    }

    public void delete(Long id) {
        Department d = find(id);
        d.setIsDelete(true);
        repository.save(d);
    }

    private Department find(Long id) {
        return repository.findById(id)
                .filter(x -> !x.getIsDelete())
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private DepartmentResponse toResp(Department d) {
        return new DepartmentResponse(
                d.getId(),
                d.getName(),
                d.getCode(),
                d.getDescription(),
                d.getHodId()
        );
    }
}
