package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.AccompanyingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccompanyingGuestRepository extends JpaRepository<AccompanyingGuest, Long> {

    List<AccompanyingGuest> findByReservation_IdAndIsDeletedFalse(Long reservationId);

    void deleteByReservation_Id(Long reservationId);
}
