package com.hotelerp.frontoffice.repository;

import com.hotelerp.common.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByBooking_Reservation_Id(Long reservationId);

    java.util.Optional<Bill> findByBooking_Id(Long bookingId);

    /**
     * Sum all successful payment amounts for a given reservation
     * (across all booking bills linked to that reservation).
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p " +
           "WHERE p.bill.booking.reservation.id = :reservationId " +
           "AND p.paymentStatus = com.hotelerp.common.entity.Payment.PaymentStatus.SUCCESS")
    BigDecimal sumPaidAmountByReservation(@Param("reservationId") Long reservationId);
}
