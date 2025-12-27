package com.parking.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id") // External key to Client
    private Client client; // For binding to Abonent

    @Column(name = "is_allowed")
    private Boolean isAllowed = true; // Entrance is allowed/not

}