package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Folio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, Long> {
    Optional<Folio> findByReservation_IdAndIsDeletedFalse(Long reservationId);
}
