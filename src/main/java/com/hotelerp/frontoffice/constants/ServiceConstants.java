package com.hotelerp.frontoffice.constants;

public class ServiceConstants {

    // ── Guest ──────────────────────────────────────────────────────────────
    public static final String GUEST_BASE_URL = "/api/frontOfficeService/v1/guests";
    public static final String CREATE_GUEST = "/createGuest";
    public static final String UPDATE_GUEST = "/updateGuest/{id}";
    public static final String GET_GUEST_BY_ID = "/getGuestById/{id}";
    public static final String GET_ALL_GUESTS = "/getAllGuests";
    public static final String DELETE_GUEST = "/deleteGuest/{id}";

    // ── Reservation ────────────────────────────────────────────────────────
    public static final String RESERVATION_BASE_URL = "/api/frontOfficeService/v1/frontOffice";
    public static final String CREATE_RESERVATION = "/createReservation";
    public static final String GET_RESERVATION_BY_ID = "/getReservationById/{id}";
    public static final String GET_ALL_RESERVATIONS = "/getAllReservations";
    public static final String GET_RESERVATIONS_BY_GUEST = "/getByGuest/{guestId}";
    public static final String CANCEL_RESERVATION = "/cancelReservation/{id}";
    public static final String DELETE_RESERVATION = "/deleteReservation/{id}";
    public static final String GET_ARRIVALS = "/arrivals";
    public static final String GET_CHECKIN_DETAILS = "/checkin-details/{bookingId}";
    public static final String COMPLETE_CHECKIN = "/checkin";
    public static final String GET_FOLIO = "/folio/{bookingId}";
    public static final String COMPLETE_CHECKOUT = "/checkout";
    public static final String GET_GANTT_CHART = "/getGanttChartData";
    public static final String UPDATE_RESERVATION = "/updateReservation/{id}";

    // ── Room Availability ──────────────────────────────────────────────────
    public static final String ROOM_BASE_URL = "/api/frontOfficeService/v1/rooms";
    public static final String GET_AVAILABLE_ROOMS = "/available";

    // Legacy alias kept so existing GuestController compile unchanged
    public static final String BASE_URL = GUEST_BASE_URL;
    public static final String GET_ROOM_AUDITS = "/getRooAudit";
}
