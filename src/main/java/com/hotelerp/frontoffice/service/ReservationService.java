package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.CheckInRequest;
import com.hotelerp.frontoffice.dto.CheckOutRequest;
import com.hotelerp.frontoffice.dto.ReservationRequest;

import java.time.LocalDate;

import com.hotelerp.frontoffice.entity.Reservation;

import java.time.LocalDate;

public interface ReservationService {

    /** Create a reservation + one booking row per selected room */
    StandardResponse<?> createReservation(ReservationRequest request);

    /** Update a reservation and all associated bookings */
    StandardResponse<?> updateReservation(Long id, ReservationRequest request);

    /** Fetch full detail for a single reservation (for detail/edit view) */
    StandardResponse<?> getReservationById(Long id);

    /** Listing: slim response with only fields visible on the list screen (with filters and paging) */
    StandardResponse<?> getAllReservations(String searchText, Reservation.ReservationStatus status, 
                                           LocalDate fromDate, LocalDate toDate, int page, int size);

    /** Listing: slim reservations for a specific guest */
    StandardResponse<?> getReservationsByGuest(Long guestId);

    /** Soft-cancel a reservation and all its room bookings */
    StandardResponse<?> cancelReservation(Long id);

    /** Soft-delete a reservation */
    StandardResponse<?> deleteReservation(Long id);

    /** Return available rooms for a hotel on the given date range */
    StandardResponse<?> getAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    /** 1> Get arrivals or departures list (with stats) based on date, search and paging */
    StandardResponse<?> getArrivals(LocalDate date, String searchText, boolean checkout, int page, int size);

    /** 2> Get check-in popup details */
    StandardResponse<?> getCheckInDetails(Long bookingId);

    /** 3> Complete check-in and save payment transaction */
    StandardResponse<?> completeCheckIn(CheckInRequest request);

    /** 4> View folio */
    StandardResponse<?> getFolio(Long bookingId);

    /** Save checkout process */
    StandardResponse<?> completeCheckOut(CheckOutRequest request);

    /** Get Room operation audit logs */
    StandardResponse<?> getRoomAudits(Long roomId);
    
    /** Get all bookings in a date range for Gantt chart view */
    StandardResponse<?> getGanttChartData(LocalDate startDate, LocalDate endDate);
}

