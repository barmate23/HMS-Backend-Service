package com.hotelerp.frontoffice.controller;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dedicated REST controller for address dropdowns (countries, states, cities).
 */
@RestController
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping(ServiceConstants.ADDRESS_BASE_URL + ServiceConstants.GET_COUNTRIES)
    public ResponseEntity<StandardResponse<?>> getCountries() {
        return ResponseEntity.ok(addressService.getCountries());
    }

    @GetMapping(ServiceConstants.ADDRESS_BASE_URL + ServiceConstants.GET_STATES)
    public ResponseEntity<StandardResponse<?>> getStates(
            @RequestParam(required = false) Long countryId) {
        return ResponseEntity.ok(addressService.getStates(countryId));
    }

    @GetMapping(ServiceConstants.ADDRESS_BASE_URL + ServiceConstants.GET_CITIES)
    public ResponseEntity<StandardResponse<?>> getCities(
            @RequestParam(required = false) Long stateId) {
        return ResponseEntity.ok(addressService.getCities(stateId));
    }
}
