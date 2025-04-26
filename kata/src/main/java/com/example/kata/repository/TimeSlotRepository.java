package com.example.kata.repository;


import com.example.kata.model.DeliveryMode;
import com.example.kata.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    @Query("SELECT t FROM TimeSlot t WHERE t.deliveryMode = :mode AND CAST(t.startTime AS date) = :date ORDER BY t.startTime ASC")
    List<TimeSlot> findByDeliveryModeAndStartDateOrderByStartTime(
            @Param("mode") DeliveryMode mode,
            @Param("date") LocalDate date
    );
}