package com.schoolerp.student.service;

import com.schoolerp.student.dto.IdCardRequestDto;
import com.schoolerp.student.entity.IdCard;
import com.schoolerp.student.repository.IdCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class IdCardService implements IdCardServiceIMPL {

    @Autowired
    private final IdCardRepository idCardRepository;

    public IdCardService(IdCardRepository idCardRepository) {
        this.idCardRepository = idCardRepository;
    }

    @Override
    public List<IdCard> getAllIdCards() {
        return idCardRepository.findAll();
    }
    
    @Override
    public IdCard createIdCard(IdCardRequestDto requestDto) {
        // Generate card number if not provided
        if (requestDto.getCardNumber() == null || requestDto.getCardNumber().isEmpty()) {
            requestDto.setCardNumber(generateCardNumber());
        }
        
        // Generate QR code if not provided
        if (requestDto.getQrCode() == null || requestDto.getQrCode().isEmpty()) {
            requestDto.setQrCode(generateQRCode(requestDto.getCardNumber()));
        }
        
        IdCard idCard = IdCard.builder()
            .studentId(requestDto.getStudentId())
            .cardNumber(requestDto.getCardNumber())
            .issueDate(requestDto.getIssueDate() != null ? requestDto.getIssueDate() : LocalDate.now())
            .validFrom(requestDto.getValidFrom() != null ? requestDto.getValidFrom() : LocalDate.now())
            .validTo(requestDto.getValidTo() != null ? requestDto.getValidTo() : LocalDate.now().plusYears(1))
            .status(IdCard.CardStatus.valueOf(requestDto.getStatus().toUpperCase()))
            .photoUrl(requestDto.getPhotoUrl())
            .qrCode(requestDto.getQrCode())
            .issueReason(requestDto.getIssueReason())
            .build();
        
        IdCard savedIdCard = idCardRepository.save(idCard);
        
        log.info("Created ID card for student: {} with card number: {}", 
                 requestDto.getStudentId(), savedIdCard.getCardNumber());
        
        return savedIdCard;
    }
    
    @Override
    public List<IdCard> getIdCardsByStudentId(String studentId) {
        return idCardRepository.findByStudentId(studentId);
    }
    
    private String generateCardNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String randomSuffix = String.valueOf((int) (Math.random() * 90000) + 10000);
        return "ID-" + year + "-" + randomSuffix;
    }
    
    private String generateQRCode(String cardNumber) {
        // Simple QR code data - in production, this would be more sophisticated
        return "STUDENT_CARD:" + cardNumber + ":" + UUID.randomUUID().toString().substring(0, 8);
    }
}