package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {


    /**
     * Fetch all rooms for a hotel that have NO active (non-cancelled) booking
     * overlapping the requested date range — i.e., truly available rooms.
     */
    @Query("SELECT r FROM Room r " +
           "WHERE r.floor.id = :floorId " +
           "AND r.isActive = true " +
           "AND r.status = com.hotelerp.frontoffice.entity.Room.RoomStatus.VACANT " +
           "AND r.id NOT IN (" +
           "  SELECT b.room.id FROM Booking b " +
           "  WHERE b.isDeleted = false " +
           "  AND b.bookingStatus NOT IN (com.hotelerp.frontoffice.entity.Booking.BookingStatus.CANCELLED) " +
           "  AND b.checkInDate < :checkOut " +
           "  AND b.checkOutDate > :checkIn" +
           ")")
    List<Room> findAvailableRooms(
            @Param("floorId")  Long floorId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
