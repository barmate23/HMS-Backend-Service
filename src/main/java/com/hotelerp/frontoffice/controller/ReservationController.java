package com.hotelerp.frontoffice.controller;

import com.hotelerp.common.entity.Reservation.ReservationStatus;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.dto.*;
import com.hotelerp.common.entity.Reservation;
import com.hotelerp.frontoffice.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for Reservation and Room-availability APIs.
 *
 * Base URL: /api/v1/reservations
 *
 * POST   /createReservation           – Create reservation + bookings
 * GET    /getReservationById/{id}     – Get single reservation
 * GET    /getAllReservations           – List all (optional ?search=)
 * GET    /getByGuest/{guestId}        – Guest's reservations
 * PUT    /cancelReservation/{id}      – Cancel reservation + bookings
 * DELETE /deleteReservation/{id}      – Soft-delete reservation
 *
 * Base URL: /api/v1/rooms
 * GET    /available                   – Available rooms (?hotelId=&checkIn=&checkOut=)
 */
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // ── Reservation Endpoints ──────────────────────────────────────────────

    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CREATE_RESERVATION)
    public ResponseEntity<StandardResponse<?>> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @PutMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.UPDATE_RESERVATION)
    public ResponseEntity<StandardResponse<?>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_RESERVATION_BY_ID)
    public ResponseEntity<StandardResponse<?>> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_ALL_RESERVATIONS)
    public ResponseEntity<StandardResponse<?>> getAllReservations(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reservationService.getAllReservations(searchText, status, fromDate, toDate, page, size));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_RESERVATIONS_BY_GUEST)
    public ResponseEntity<StandardResponse<?>> getReservationsByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(reservationService.getReservationsByGuest(guestId));
    }

    @PutMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CANCEL_RESERVATION)
    public ResponseEntity<StandardResponse<?>> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @DeleteMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.DELETE_RESERVATION)
    public ResponseEntity<StandardResponse<?>> deleteReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.deleteReservation(id));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_GANTT_CHART)
    public ResponseEntity<StandardResponse<?>> getGanttChartData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reservationService.getGanttChartData(startDate, endDate));
    }

    // ── Room Availability Endpoint ─────────────────────────────────────────

    @GetMapping(ServiceConstants.ROOM_BASE_URL + ServiceConstants.GET_AVAILABLE_ROOMS)
    public ResponseEntity<StandardResponse<?>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(reservationService.getAvailableRooms(checkIn, checkOut));
    }
}
