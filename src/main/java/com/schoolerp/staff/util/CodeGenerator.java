package com.schoolerp.staff.util;

import com.schoolerp.staff.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeGenerator {

    private final UserRepository repo;
    private final AtomicInteger counter = new AtomicInteger(1);

    public CodeGenerator(UserRepository repo) {
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

    public static String generatePassword() {
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
