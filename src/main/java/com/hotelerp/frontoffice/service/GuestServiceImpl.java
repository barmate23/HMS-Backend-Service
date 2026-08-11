package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.config.LoginUser;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.dto.GuestResponse;
import com.hotelerp.frontoffice.entity.Guest;
import com.hotelerp.frontoffice.entity.Hotel;
import com.hotelerp.frontoffice.repository.FolioRepository;
import com.hotelerp.frontoffice.repository.GuestRepository;
import com.hotelerp.frontoffice.repository.HotelRepository;
import com.hotelerp.frontoffice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final FolioRepository folioRepository;
    private final HotelRepository hotelRepository;
    private final LoginUser loginUser;

    // ── Create ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> createGuest(GuestRequest request) {
        log.info("Request received to create guest with email: {}", request.getEmail());
        try {
            Long hotelId = loginUser.getHotelId();

            if (guestRepository.existsByEmailAndHotel_IdAndIsDeletedFalse(request.getEmail(), hotelId)) {
                log.warn("Create guest failed: Email {} already exists for hotel {}", request.getEmail(), hotelId);
                return StandardResponse.error("Email already exists", "DUPLICATE_EMAIL", "email", null);
            }

            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found: " + hotelId));

            Guest guest = buildGuest(request, hotel);
            Guest saved = guestRepository.save(guest);

            log.info("Guest created successfully with ID: {}", saved.getId());
            return StandardResponse.success(mapToResponse(saved), "Guest created successfully");

        } catch (Exception e) {
            log.error("Error creating guest: ", e);
            return StandardResponse.error("Failed to create guest", "CREATE_ERROR", null, e.getMessage());
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> updateGuest(Long id, GuestRequest request) {
        log.info("Request received to update guest with ID: {}", id);
        try {
            Optional<Guest> guestOpt = guestRepository.findById(id);
            if (guestOpt.isEmpty() || Boolean.TRUE.equals(guestOpt.get().getIsDeleted())) {
                return StandardResponse.error("Guest not found", "NOT_FOUND", "id", null);
            }

            if (guestRepository.existsByEmailAndIdNotAndIsDeletedFalse(request.getEmail(), id)) {
                return StandardResponse.error("Email already exists", "DUPLICATE_EMAIL", "email", null);
            }

            Guest guest = guestOpt.get();
            applyFields(guest, request);
            guest.setUpdatedAt(LocalDateTime.now());

            Guest updated = guestRepository.save(guest);
            log.info("Guest ID {} updated successfully", id);
            return StandardResponse.success(mapToResponse(updated), "Guest updated successfully");

        } catch (Exception e) {
            log.error("Error updating guest: ", e);
            return StandardResponse.error("Failed to update guest", "UPDATE_ERROR", null, e.getMessage());
        }
    }

    // ── Read By ID ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getGuestById(Long id) {
        log.info("Fetching guest ID: {}", id);
        try {
            Optional<Guest> guestOpt = guestRepository.findById(id);
            if (guestOpt.isEmpty() || Boolean.TRUE.equals(guestOpt.get().getIsDeleted())) {
                return StandardResponse.error("Guest not found", "NOT_FOUND", "id", null);
            }
            return StandardResponse.success(mapToResponse(guestOpt.get()), "Guest fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching guest: ", e);
            return StandardResponse.error("Failed to fetch guest", "FETCH_ERROR", null, e.getMessage());
        }
    }

    // ── Read All ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAllGuests(String search) {
        log.info("Fetching all guests, search={}", search);
        try {
            Long hotelId = loginUser.getHotelId();
            List<Guest> guests = (search != null && !search.isBlank())
                    ? guestRepository.searchGuestsByHotel(search.trim(), hotelId)
                    : guestRepository.findByHotel_IdAndIsDeletedFalse(hotelId);

            List<GuestResponse> responses = guests.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            StandardResponse.ResponseMetadata meta = StandardResponse.ResponseMetadata.builder()
                    .totalRecords((long) responses.size())
                    .operation("GET_ALL_GUESTS")
                    .build();

            return StandardResponse.success(responses, "Guests fetched successfully", meta);
        } catch (Exception e) {
            log.error("Error fetching all guests: ", e);
            return StandardResponse.error("Failed to fetch guests", "FETCH_ERROR", null, e.getMessage());
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> deleteGuest(Long id) {
        log.info("Soft-deleting guest ID: {}", id);
        try {
            Optional<Guest> guestOpt = guestRepository.findById(id);
            if (guestOpt.isEmpty() || Boolean.TRUE.equals(guestOpt.get().getIsDeleted())) {
                return StandardResponse.error("Guest not found", "NOT_FOUND", "id", null);
            }

            Guest guest = guestOpt.get();
            guest.setIsDeleted(true);
            guest.setUpdatedAt(LocalDateTime.now());
            guestRepository.save(guest);

            log.info("Guest ID {} soft-deleted successfully", id);
            return StandardResponse.success("Guest deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting guest: ", e);
            return StandardResponse.error("Failed to delete guest", "DELETE_ERROR", null, e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Build a new Guest entity from a request. */
    private Guest buildGuest(GuestRequest req, Hotel hotel) {
        Guest guest = new Guest();
        applyFields(guest, req);
        guest.setHotel(hotel);
        guest.setIsDeleted(false);
        guest.setIsActive(true);
        return guest;
    }

    /** Apply request fields onto an existing (or new) Guest entity. */
    private void applyFields(Guest guest, GuestRequest req) {
        guest.setTitle(req.getTitle());
        guest.setFirstName(req.getFirstName());
        guest.setLastName(req.getLastName());
        guest.setCountryCode(req.getCountryCode());
        guest.setPhone(req.getPhone());
        guest.setEmail(req.getEmail());
        guest.setAddressLine1(req.getAddressLine1());
        guest.setAddressLine2(req.getAddressLine2());
        guest.setCity(req.getCity());
        guest.setState(req.getState());
        guest.setPostCode(req.getPostCode());
        guest.setCountry(req.getCountry());
        guest.setNationality(req.getNationality());
        guest.setGender(req.getGender());
        guest.setDateOfBirth(req.getDateOfBirth());
        guest.setIdProofType(req.getIdProofType());
        guest.setIdProofNumber(req.getIdProofNumber());
        guest.setGuestNotes(req.getGuestNotes());
        guest.setPreference(req.getPreference());
        guest.setIsVip(req.getIsVip() != null ? req.getIsVip() : false);
    }

    /** Map entity → response DTO. */
    private GuestResponse mapToResponse(Guest g) {
        long stays = reservationRepository.countByGuest_IdAndIsDeletedFalse(g.getId());
        BigDecimal spent = folioRepository.sumTotalChargesByGuestId(g.getId());

        return GuestResponse.builder()
                .id(g.getId())
                .hotelId(g.getHotel() != null ? g.getHotel().getId() : null)
                .hotelName(g.getHotel() != null ? g.getHotel().getName() : null)
                .title(g.getTitle())
                .firstName(g.getFirstName())
                .lastName(g.getLastName())
                .fullName(g.getFirstName() + " " + g.getLastName())
                .countryCode(g.getCountryCode())
                .phone(g.getPhone())
                .email(g.getEmail())
                .addressLine1(g.getAddressLine1())
                .addressLine2(g.getAddressLine2())
                .city(g.getCity())
                .state(g.getState())
                .postCode(g.getPostCode())
                .country(g.getCountry())
                .nationality(g.getNationality())
                .gender(g.getGender())
                .guestNotes(g.getGuestNotes())
                .preference(g.getPreference())
                .isVip(g.getIsVip())
                .numberOfStays((int) stays)
                .totalSpent(spent != null ? spent : BigDecimal.ZERO)
                .build();
    }
}
