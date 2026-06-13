package com.hotelerp.frontoffice.repository;

import com.hotelerp.common.entity.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {
    List<RatePlan> findByIsActiveTrueOrderByDisplayOrderAsc();
}
