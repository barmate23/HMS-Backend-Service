package com.hotelerp.frontoffice.controller;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.dto.*;
import com.hotelerp.frontoffice.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Dedicated REST controller for handling check-in, check-out, listing, and folio actions.
 */
@RestController
@RequiredArgsConstructor
public class CheckInOutController {

    private final ReservationService reservationService;

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_ARRIVALS)
    public ResponseEntity<StandardResponse<?>> getArrivals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean checkout) {
        return ResponseEntity.ok(reservationService.getArrivals(date, search, checkout));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_CHECKIN_DETAILS)
    public ResponseEntity<StandardResponse<?>> getCheckInDetails(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reservationService.getCheckInDetails(bookingId));
    }

    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.COMPLETE_CHECKIN)
    public ResponseEntity<StandardResponse<?>> completeCheckIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(reservationService.completeCheckIn(request));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_FOLIO)
    public ResponseEntity<StandardResponse<?>> getFolio(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reservationService.getFolio(bookingId));
    }

    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.COMPLETE_CHECKOUT)
    public ResponseEntity<StandardResponse<?>> completeCheckOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(reservationService.completeCheckOut(request));
    }

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.GET_ROOM_AUDITS)
    public ResponseEntity<StandardResponse<?>> getRoomAudits(@PathVariable Long roomId) {
        return ResponseEntity.ok(reservationService.getRoomAudits(roomId));
    }
}
