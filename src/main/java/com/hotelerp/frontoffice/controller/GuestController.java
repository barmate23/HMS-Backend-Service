package com.hotelerp.frontoffice.controller;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Guest management APIs.
 *
 * Base URL: /api/v1/guests
 *
 * POST   /createGuest         – Create a new guest
 * PUT    /updateGuest/{id}    – Update guest details
 * GET    /getGuestById/{id}   – Fetch single guest
 * GET    /getAllGuests         – List all guests (optional ?search=)
 * DELETE /deleteGuest/{id}    – Soft-delete a guest
 */
@RestController
@RequestMapping(ServiceConstants.GUEST_BASE_URL)
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping(ServiceConstants.CREATE_GUEST)
    public ResponseEntity<StandardResponse<?>> createGuest(
            @Valid @RequestBody GuestRequest request) {
        return ResponseEntity.ok(guestService.createGuest(request));
    }

    @PutMapping(ServiceConstants.UPDATE_GUEST)
    public ResponseEntity<StandardResponse<?>> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestRequest request) {
        return ResponseEntity.ok(guestService.updateGuest(id, request));
    }

    @GetMapping(ServiceConstants.GET_GUEST_BY_ID)
    public ResponseEntity<StandardResponse<?>> getGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.getGuestById(id));
    }

    @GetMapping(ServiceConstants.GET_ALL_GUESTS)
    public ResponseEntity<StandardResponse<?>> getAllGuests(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(guestService.getAllGuests(search));
    }

    @DeleteMapping(ServiceConstants.DELETE_GUEST)
    public ResponseEntity<StandardResponse<?>> deleteGuest(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.deleteGuest(id));
    }
}
