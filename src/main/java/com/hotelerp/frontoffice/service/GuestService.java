package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.GuestRequest;

public interface GuestService {
    StandardResponse<?> createGuest(GuestRequest request);
    StandardResponse<?> updateGuest(Long id, GuestRequest request);
    StandardResponse<?> getGuestById(Long id);
    StandardResponse<?> getAllGuests(String search);
    StandardResponse<?> deleteGuest(Long id);
}
