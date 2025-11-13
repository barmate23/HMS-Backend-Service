package com.schoolerp.student.repository;

import com.schoolerp.student.entity.CommonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommonMasterRepository extends JpaRepository<CommonMaster, Integer> {
    Optional<CommonMaster> findByCommonMasterKeyAndDataAndAndStatus(String commonMasterKey,String data ,Boolean status);

}
