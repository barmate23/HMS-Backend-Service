package com.schoolerp.student.util;

import com.schoolerp.staff.repository.StaffRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeGenerator {

    private final StaffRepository repo;
    private final AtomicInteger counter = new AtomicInteger(1);

    public CodeGenerator(StaffRepository repo) {
        this.repo = repo;
    }

    public synchronized String generate(String deptCode) {
        String year = String.valueOf(Year.now().getValue());
        String code;
        do {
            code = String.format("%s-%s-%04d", deptCode, year, counter.getAndIncrement());
        } while (repo.existsByStaffCode(code));
        return code;
    }
}
