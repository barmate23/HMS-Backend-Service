package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.RoomAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomAuditRepository extends JpaRepository<RoomAudit, Long> {
    List<RoomAudit> findByRoom_Id(Long roomId);
    List<RoomAudit> findByBooking_Id(Long bookingId);
}
