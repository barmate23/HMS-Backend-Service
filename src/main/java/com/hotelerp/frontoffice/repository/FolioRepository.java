package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Folio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, Long> {
    Optional<Folio> findByReservation_IdAndIsDeletedFalse(Long reservationId);

    @Query("SELECT COALESCE(SUM(f.totalCharges), 0) FROM Folio f " +
           "WHERE f.reservation.guest.id = :guestId AND f.isDeleted = false")
    BigDecimal sumTotalChargesByGuestId(@Param("guestId") Long guestId);
}

