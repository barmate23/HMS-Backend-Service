package com.schoolerp.student.repository;

import com.schoolerp.student.entity.StudentPromotionMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPromotionMapperRepository extends JpaRepository<StudentPromotionMapper, Integer> {
}

