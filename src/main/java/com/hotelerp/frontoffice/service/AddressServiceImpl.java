package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.CityResponse;
import com.hotelerp.frontoffice.dto.CountryResponse;
import com.hotelerp.frontoffice.dto.StateResponse;
import com.hotelerp.frontoffice.entity.City;
import com.hotelerp.frontoffice.entity.Country;
import com.hotelerp.frontoffice.entity.State;
import com.hotelerp.frontoffice.repository.CityRepository;
import com.hotelerp.frontoffice.repository.CountryRepository;
import com.hotelerp.frontoffice.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getCountries() {
        log.info("Fetching all active countries");
        try {
            List<Country> countries = countryRepository.findByIsDeletedFalseOrderByNameAsc();
            List<CountryResponse> response = countries.stream()
                    .map(c -> CountryResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .code(c.getCode())
                            .phoneCode(c.getPhoneCode())
                            .build())
                    .collect(Collectors.toList());
            return StandardResponse.success(response, "Countries fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching countries: ", e);
            return StandardResponse.error("Failed to fetch countries", "FETCH_COUNTRIES_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getStates(Long countryId) {
        log.info("Fetching states for countryId={}", countryId);
        try {
            List<State> states;
            if (countryId != null) {
                states = stateRepository.findByCountry_IdAndIsDeletedFalseOrderByNameAsc(countryId);
            } else {
                states = stateRepository.findByIsDeletedFalseOrderByNameAsc();
            }
            List<StateResponse> response = states.stream()
                    .map(s -> StateResponse.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .code(s.getCode())
                            .countryId(s.getCountry().getId())
                            .build())
                    .collect(Collectors.toList());
            return StandardResponse.success(response, "States fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching states: ", e);
            return StandardResponse.error("Failed to fetch states", "FETCH_STATES_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getCities(Long stateId) {
        log.info("Fetching cities for stateId={}", stateId);
        try {
            List<City> cities;
            if (stateId != null) {
                cities = cityRepository.findByState_IdAndIsDeletedFalseOrderByNameAsc(stateId);
            } else {
                cities = cityRepository.findByIsDeletedFalseOrderByNameAsc();
            }
            List<CityResponse> response = cities.stream()
                    .map(c -> CityResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .stateId(c.getState().getId())
                            .build())
                    .collect(Collectors.toList());
            return StandardResponse.success(response, "Cities fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching cities: ", e);
            return StandardResponse.error("Failed to fetch cities", "FETCH_CITIES_ERROR", null, e.getMessage());
        }
    }
}
