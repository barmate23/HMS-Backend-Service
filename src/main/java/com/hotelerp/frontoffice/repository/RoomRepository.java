package com.hotelerp.frontoffice.repository;

import com.hotelerp.common.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {


    /**
     * Fetch all rooms that have NO active (non-cancelled) booking
     * overlapping the requested date range — i.e., truly available rooms.
     */
    @Query("SELECT r FROM Room r " +
           "WHERE r.isActive = true " +
           "AND r.status.code = 'VACANT' " +
           "AND r.id NOT IN (" +
           "  SELECT b.room.id FROM Booking b " +
           "  JOIN b.reservation res " +
           "  WHERE b.isDeleted = false " +
           "  AND res.isDeleted = false " +
           "  AND res.reservationStatus NOT IN (com.hotelerp.common.entity.Reservation.ReservationStatus.CANCELLED, " +
           "                                   com.hotelerp.common.entity.Reservation.ReservationStatus.NO_SHOW) " +
           "  AND res.checkInDate < :checkOut " +
           "  AND res.checkOutDate > :checkIn" +
           ")")
    List<Room> findAvailableRooms(
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
