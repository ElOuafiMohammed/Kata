package com.example.kata.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DeliveryMode deliveryMode;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int maxOrders = 3; // Default max orders

    private int currentOrders = 0;

}
