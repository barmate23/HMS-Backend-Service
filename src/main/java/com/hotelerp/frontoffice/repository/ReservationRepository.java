package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByGuest_IdAndIsDeletedFalse(Long guestId);

    Optional<Reservation> findByIdAndIsDeletedFalse(Long id);

    /**
     * Fetch all active reservations overlapping a specific date range.
     * Used for Gantt chart view.
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.isDeleted = false " +
           "AND r.reservationStatus NOT IN (com.hotelerp.frontoffice.entity.Reservation.ReservationStatus.CANCELLED, " +
           "                               com.hotelerp.frontoffice.entity.Reservation.ReservationStatus.NO_SHOW) " +
           "AND r.checkInDate < :endDate " +
           "AND r.checkOutDate > :startDate")
    List<Reservation> findReservationsInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);
}
