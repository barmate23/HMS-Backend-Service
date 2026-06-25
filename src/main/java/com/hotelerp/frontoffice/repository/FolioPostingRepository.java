package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.FolioPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolioPostingRepository extends JpaRepository<FolioPosting, Long> {
    List<FolioPosting> findByFolio_IdAndIsDeletedFalse(Long folioId);
}
