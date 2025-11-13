package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "common_master")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private String commonMasterKey;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    private Boolean status;


}

