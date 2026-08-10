package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByCountry_IdAndIsDeletedFalseOrderByNameAsc(Long countryId);
    List<State> findByIsDeletedFalseOrderByNameAsc();
    Optional<State> findByNameIgnoreCaseAndCountry_IdAndIsDeletedFalse(String name, Long countryId);
}
