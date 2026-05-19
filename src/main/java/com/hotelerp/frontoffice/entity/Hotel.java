    package com.hotelerp.frontoffice.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.time.LocalTime;

    @Entity
    @Table(name = "hotels", indexes = {
        @Index(name = "idx_city", columnList = "city"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_active", columnList = "isActive")
    })
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Hotel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "name", nullable = false, unique = true, length = 255)
        private String name;

        @Column(name = "email", nullable = false, unique = true, length = 100)
        private String email;

        @Column(name = "phone", nullable = false, length = 20)
        private String phone;

        @Column(name = "address", nullable = false, columnDefinition = "TEXT")
        private String address;

        @Column(name = "city", nullable = false, length = 100)
        private String city;

        @Column(name = "state", nullable = false, length = 100)
        private String state;

        @Column(name = "country", nullable = false, length = 100)
        private String country;

        @Column(name = "zipCode", nullable = false, length = 10)
        private String zipCode;

        @Column(name = "totalRooms", nullable = false)
        private Integer totalRooms;

        @Column(name = "currency", nullable = false, length = 3)
        private String currency = "USD";

        @Column(name = "createdAt", nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updatedAt")
        private LocalDateTime updatedAt = LocalDateTime.now();

        @Column(name = "isActive")
        private Boolean isActive = true;


    }