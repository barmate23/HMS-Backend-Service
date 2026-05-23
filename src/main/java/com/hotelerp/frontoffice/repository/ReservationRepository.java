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

    List<Reservation> findByIsDeletedFalse();

    List<Reservation> findByGuest_IdAndIsDeletedFalse(Long guestId);

    Optional<Reservation> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.isDeleted = false AND r.hotel.id = :hotelId " +
           "AND r.checkInDate <= :checkOut AND r.checkOutDate >= :checkIn")
    List<Reservation> findOverlappingReservations(
            @Param("hotelId")  Long hotelId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.isDeleted = false AND (" +
           "LOWER(r.guest.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(r.guest.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(r.guest.email)     LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(r.guest.phone)     LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Reservation> searchReservations(@Param("q") String query);
}
