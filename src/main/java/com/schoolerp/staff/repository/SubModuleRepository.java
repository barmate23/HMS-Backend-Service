package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.SubModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubModuleRepository extends JpaRepository<SubModule, Integer> {
    List<SubModule> findByModulesId(Integer moduleId);
}