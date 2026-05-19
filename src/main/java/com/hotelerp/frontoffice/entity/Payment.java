package com.hotelerp.frontoffice.entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_bill_id", columnList = "billId"),
    @Index(name = "idx_payment_date", columnList = "paymentDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billId", nullable = false)
    private Bill bill;
    
    @Column(name = "amount", nullable = false, 
            precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMode", nullable = false, length = 20)
    private PaymentMode paymentMode;
    
    @Column(name = "paymentDate", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "transactionId", unique = true, length = 100)
    private String transactionId;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum PaymentMode {
        CASH, CREDIT_CARD, DEBIT_CARD, NET_BANKING, 
        UPI, CHEQUE, DEMAND_DRAFT
    }
    
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
      