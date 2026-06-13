package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.CommonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonMasterRepository extends JpaRepository<CommonMaster, Long> {
    List<CommonMaster> findByCategoryAndIsActiveTrue(String category);
}
