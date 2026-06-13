package com.hotelerp.frontoffice.repository;

import com.hotelerp.common.entity.CommonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommonMasterRepository extends JpaRepository<CommonMaster, Long> {
    Optional<CommonMaster> findByCategoryAndCode(String category, String code);
}
