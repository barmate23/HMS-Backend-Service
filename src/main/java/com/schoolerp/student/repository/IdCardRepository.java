package com.schoolerp.student.repository;

import com.schoolerp.student.entity.IdCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdCardRepository extends JpaRepository<IdCard, Integer> {
    
    List<IdCard> findByStudentId(String studentId);
    
    Optional<IdCard> findByCardNumber(String cardNumber);
    
    List<IdCard> findByStatus(IdCard.CardStatus status);
}