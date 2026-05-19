package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "bills", indexes = {
    @Index(name = "idx_booking_id", columnList = "bookingId"),
    @Index(name = "idx_guest_id", columnList = "guestId"),
    @Index(name = "idx_bill_status", columnList = "billStatus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingId", nullable = false, unique = true)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guestId", nullable = false)
    private Guest guest;
    
    @Column(name = "roomCharges", nullable = false, 
            precision = 10, scale = 2)
    private BigDecimal roomCharges;
    
    @Builder.Default
    @Column(name = "additionalCharges", precision = 10, scale = 2)
    private BigDecimal additionalCharges = BigDecimal.ZERO;
    
    @Column(name = "taxAmount", nullable = false, 
            precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "totalAmount", nullable = false, 
            precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "billStatus", nullable = false, length = 20)
    private BillStatus billStatus = BillStatus.DRAFT;
    
    @Column(name = "billDate", nullable = false)
    private LocalDate billDate;
    
    @Column(name = "paymentDueDate")
    private LocalDate paymentDueDate;
    
    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Relationships
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, 
               fetch = FetchType.LAZY)
    private java.util.List<Payment> payments;
    
    public enum BillStatus {
        DRAFT, ISSUED, PARTIALLY_PAID, PAID, CANCELLED
    }
}
      