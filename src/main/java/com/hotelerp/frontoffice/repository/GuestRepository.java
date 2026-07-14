package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByIsDeletedFalse();
    boolean existsByEmailAndIsDeletedFalse(String email);
    Optional<Guest> findByFirstNameAndLastNameAndIsDeletedFalse(String firstName,String lastName);
    boolean existsByEmailAndIdNotAndIsDeletedFalse(String email, Long id);

    @Query("SELECT g FROM Guest g WHERE g.isDeleted = false AND (" +
           "LOWER(g.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Guest> searchGuests(@Param("search") String search);

    Optional<Guest> findByEmailAndIsDeletedFalse(String email);
}
