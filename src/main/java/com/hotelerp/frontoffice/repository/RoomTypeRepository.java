package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Optional<RoomType> findByNameIgnoreCaseAndIsActiveTrue(String name);
}
