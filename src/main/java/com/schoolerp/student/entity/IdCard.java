package com.schoolerp.student.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "id_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IdCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(nullable = false)
    private String studentId;
    
    @Column(unique = true, nullable = false)
    private String cardNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validTo;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;
    
    private String photoUrl;
    
    private String qrCode;
    
    private String issueReason;
    
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;
    
    public enum CardStatus {
        ACTIVE, INACTIVE, EXPIRED, LOST, DAMAGED
    }
}