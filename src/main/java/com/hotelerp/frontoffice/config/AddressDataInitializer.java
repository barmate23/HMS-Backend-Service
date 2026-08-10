package com.hotelerp.frontoffice.config;

import com.hotelerp.frontoffice.entity.City;
import com.hotelerp.frontoffice.entity.Country;
import com.hotelerp.frontoffice.entity.State;
import com.hotelerp.frontoffice.repository.CityRepository;
import com.hotelerp.frontoffice.repository.CountryRepository;
import com.hotelerp.frontoffice.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressDataInitializer implements ApplicationRunner {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            initAddressData();
        } catch (Exception e) {
            log.error("Failed to initialize address data for India: ", e);
        }
    }

    private void initAddressData() {
        Optional<Country> existingIndia = countryRepository.findByCodeIgnoreCaseAndIsDeletedFalse("IN");
        Country india;
        if (existingIndia.isPresent()) {
            india = existingIndia.get();
            log.info("India country data already exists (id={})", india.getId());
        } else {
            india = Country.builder()
                    .name("India")
                    .code("IN")
                    .phoneCode("+91")
                    .isDeleted(false)
                    .build();
            india = countryRepository.save(india);
            log.info("Initialized Country: India (id={})", india.getId());
        }

        Map<String, String[]> stateCityMap = new LinkedHashMap<>();
        stateCityMap.put("Maharashtra", new String[]{"Mumbai", "Pune", "Nagpur", "Nashik", "Thane", "Aurangabad", "Solapur", "Amravati"});
        stateCityMap.put("Delhi", new String[]{"New Delhi", "Delhi", "North Delhi", "South Delhi"});
        stateCityMap.put("Karnataka", new String[]{"Bengaluru", "Mysuru", "Mangaluru", "Hubballi", "Belagavi"});
        stateCityMap.put("Tamil Nadu", new String[]{"Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem"});
        stateCityMap.put("Telangana", new String[]{"Hyderabad", "Warangal", "Nizamabad", "Karimnagar"});
        stateCityMap.put("Gujarat", new String[]{"Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar"});
        stateCityMap.put("Rajasthan", new String[]{"Jaipur", "Udaipur", "Jodhpur", "Kota", "Ajmer"});
        stateCityMap.put("West Bengal", new String[]{"Kolkata", "Howrah", "Durgapur", "Asansol", "Siliguri"});
        stateCityMap.put("Uttar Pradesh", new String[]{"Lucknow", "Noida", "Kanpur", "Agra", "Varanasi", "Prayagraj", "Ghaziabad"});
        stateCityMap.put("Kerala", new String[]{"Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam"});
        stateCityMap.put("Punjab", new String[]{"Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Mohali"});
        stateCityMap.put("Haryana", new String[]{"Gurugram", "Faridabad", "Panipat", "Ambala", "Karnal"});
        stateCityMap.put("Madhya Pradesh", new String[]{"Bhopal", "Indore", "Gwalior", "Jabalpur", "Ujjain"});
        stateCityMap.put("Bihar", new String[]{"Patna", "Gaya", "Bhagalpur", "Muzaffarpur"});
        stateCityMap.put("Odisha", new String[]{"Bhubaneswar", "Cuttack", "Rourkela", "Puri"});
        stateCityMap.put("Andhra Pradesh", new String[]{"Visakhapatnam", "Vijayawada", "Guntur", "Tirupati"});
        stateCityMap.put("Goa", new String[]{"Panaji", "Margao", "Vasco da Gama"});
        stateCityMap.put("Assam", new String[]{"Guwahati", "Silchar", "Dibrugarh"});

        Map<String, String> stateCodeMap = new HashMap<>();
        stateCodeMap.put("Maharashtra", "MH");
        stateCodeMap.put("Delhi", "DL");
        stateCodeMap.put("Karnataka", "KA");
        stateCodeMap.put("Tamil Nadu", "TN");
        stateCodeMap.put("Telangana", "TG");
        stateCodeMap.put("Gujarat", "GJ");
        stateCodeMap.put("Rajasthan", "RJ");
        stateCodeMap.put("West Bengal", "WB");
        stateCodeMap.put("Uttar Pradesh", "UP");
        stateCodeMap.put("Kerala", "KL");
        stateCodeMap.put("Punjab", "PB");
        stateCodeMap.put("Haryana", "HR");
        stateCodeMap.put("Madhya Pradesh", "MP");
        stateCodeMap.put("Bihar", "BR");
        stateCodeMap.put("Odisha", "OR");
        stateCodeMap.put("Andhra Pradesh", "AP");
        stateCodeMap.put("Goa", "GA");
        stateCodeMap.put("Assam", "AS");

        for (Map.Entry<String, String[]> entry : stateCityMap.entrySet()) {
            String stateName = entry.getKey();
            String[] cities = entry.getValue();
            String stateCode = stateCodeMap.getOrDefault(stateName, stateName.substring(0, Math.min(2, stateName.length())).toUpperCase());

            final Country indiaCountry = india;
            State state = stateRepository.findByNameIgnoreCaseAndCountry_IdAndIsDeletedFalse(stateName, indiaCountry.getId())
                    .orElseGet(() -> stateRepository.save(State.builder()
                            .name(stateName)
                            .code(stateCode)
                            .country(indiaCountry)
                            .isDeleted(false)
                            .build()));

            for (String cityName : cities) {
                final State currentState = state;
                cityRepository.findByNameIgnoreCaseAndState_IdAndIsDeletedFalse(cityName, currentState.getId())
                        .orElseGet(() -> cityRepository.save(City.builder()
                                .name(cityName)
                                .state(currentState)
                                .isDeleted(false)
                                .build()));
            }
        }

        log.info("Address data initialization for India complete");
    }
}
