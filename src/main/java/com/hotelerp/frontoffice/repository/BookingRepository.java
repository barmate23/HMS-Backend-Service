package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByReservation_IdAndIsDeletedFalse(Long reservationId);

    List<Booking> findByIsDeletedFalse();

    /**
     * Check if a specific room is already booked (non-cancelled) for an overlapping date range.
     * Used to prevent double-booking.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.isDeleted = false " +
           "AND (b.bookingStatus IS NULL OR b.bookingStatus.code NOT IN ('CANCELLED')) " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn")
    boolean isRoomBooked(
            @Param("roomId")   Long roomId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Same as above but excluding a specific booking (useful for updates).
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.id <> :excludeId " +
           "AND b.isDeleted = false " +
           "AND (b.bookingStatus IS NULL OR b.bookingStatus.code NOT IN ('CANCELLED')) " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn")
    boolean isRoomBookedExcluding(
            @Param("roomId")    Long roomId,
            @Param("excludeId") Long excludeId,
            @Param("checkIn")   LocalDate checkIn,
            @Param("checkOut")  LocalDate checkOut);

    /**
     * Check if a room is booked by OTHER reservations.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.reservation.id <> :resId " +
           "AND b.isDeleted = false " +
           "AND (b.bookingStatus IS NULL OR b.bookingStatus.code NOT IN ('CANCELLED')) " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn")
    boolean isRoomBookedExcludingReservation(
            @Param("roomId")    Long roomId,
            @Param("resId")     Long resId,
            @Param("checkIn")   LocalDate checkIn,
            @Param("checkOut")  LocalDate checkOut);

    /**
     * Fetch all active bookings overlapping a specific date range.
     * Used for Gantt chart view.
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.isDeleted = false " +
           "AND (b.bookingStatus IS NULL OR b.bookingStatus.code NOT IN ('CANCELLED')) " +
           "AND b.checkInDate < :endDate " +
           "AND b.checkOutDate > :startDate")
    List<Booking> findBookingsInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);
}
