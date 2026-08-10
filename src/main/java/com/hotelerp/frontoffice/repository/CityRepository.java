package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByState_IdAndIsDeletedFalseOrderByNameAsc(Long stateId);
    List<City> findByIsDeletedFalseOrderByNameAsc();
    Optional<City> findByNameIgnoreCaseAndState_IdAndIsDeletedFalse(String name, Long stateId);
}
