package com.example.kata.service;

import com.example.kata.exception.TimeSlotNotAvailableException;
import com.example.kata.model.DeliveryMode;
import com.example.kata.model.TimeSlot;
import com.example.kata.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private static final Logger logger = LoggerFactory.getLogger(TimeSlotService.class);

    @Cacheable("deliveryModes")
    public List<DeliveryMode> getAllDeliveryModes() {
        logger.info("Fetching all delivery modes");
        return List.of(DeliveryMode.values());
    }

    @Cacheable(value = "timeSlots")
    @Async
    public CompletableFuture<List<TimeSlot>> getAvailableTimeSlots(DeliveryMode mode, LocalDate date) {
        logger.info("Fetching available time slots for mode: {} and date: {}", mode, date);

        return CompletableFuture.supplyAsync(() -> {
            List<TimeSlot> availableTimeSlots = timeSlotRepository.findByDeliveryModeAndStartDateOrderByStartTime(mode, date).stream()
                    .filter(timeSlot -> timeSlot.getCurrentOrders() < timeSlot.getMaxOrders())
                    .toList();
            if (availableTimeSlots.isEmpty()) {
                logger.warn("No available time slots found for mode: {} and date: {}", mode, date);
            }
            return availableTimeSlots;
        });
    }

    @Async
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "timeSlots", allEntries = true)
            }
    )
    public CompletableFuture<TimeSlot> reserveSlot(Long timeSlotId) {
        logger.info("Reserving time slot with ID: {}", timeSlotId);

        Optional<TimeSlot> timeSlotOptional = timeSlotRepository.findById(timeSlotId);

        if (timeSlotOptional.isEmpty()) {
            logger.error("Time slot with ID: {} not found", timeSlotId);
            throw new TimeSlotNotAvailableException("Time slot not found");
        }

        TimeSlot timeSlot = timeSlotOptional.get();
        if (timeSlot.getCurrentOrders() >= timeSlot.getMaxOrders()) {
            logger.error("Time slot with ID: {} is fully booked", timeSlotId);
            throw new TimeSlotNotAvailableException("Time slot is fully booked");
        }

        timeSlot.setCurrentOrders(timeSlot.getCurrentOrders() + 1);
        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        logger.info("Successfully reserved time slot with ID: {}", timeSlotId);
        return CompletableFuture.completedFuture(updatedTimeSlot);
    }
}
