package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.RoomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Long> {
    List<RoomPhoto> findByRoom_Id(Long roomId);
    List<RoomPhoto> findByRoom_IdIn(List<Long> roomIds);
}
