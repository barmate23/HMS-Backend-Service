package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;

public interface AddressService {
    StandardResponse<?> getCountries();
    StandardResponse<?> getStates(Long countryId);
    StandardResponse<?> getCities(Long stateId);
}
