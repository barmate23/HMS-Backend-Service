package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.FolioPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolioPostingRepository extends JpaRepository<FolioPosting, Long> {

    List<FolioPosting> findByFolio_IdAndIsDeletedFalse(Long folioId);

    /**
     * Finds the reservation-specific folio posting by folio ID and source name.
     * Used to update paidAmount on the "Reservation" posting when payment is received.
     * Uses findTop to safely return at most one record (there should only be one per source per folio).
     */
    Optional<FolioPosting> findTopByFolio_IdAndSourceAndIsDeletedFalse(Long folioId, String source);
}
