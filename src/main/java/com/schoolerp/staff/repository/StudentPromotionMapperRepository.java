package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.StudentPromotionMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPromotionMapperRepository extends JpaRepository<StudentPromotionMapper, Integer> {
}

