package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByIsDeletedFalseOrderByNameAsc();
    Optional<Country> findByNameIgnoreCaseAndIsDeletedFalse(String name);
    Optional<Country> findByCodeIgnoreCaseAndIsDeletedFalse(String code);
}
