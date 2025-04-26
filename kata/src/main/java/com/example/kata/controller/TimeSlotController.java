package com.example.kata.controller;

import com.example.kata.model.DeliveryMode;
import com.example.kata.model.TimeSlot;
import com.example.kata.service.TimeSlotService;
import com.example.kata.assembler.TimeSlotModelAssembler;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;
    private final TimeSlotModelAssembler timeSlotModelAssembler;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/timeslots")
    public CollectionModel<EntityModel<TimeSlot>> getAvailableTimeSlots(
            @Parameter(description = "Delivery mode (DRIVE, DELIVERY, DELIVERY_TODAY, DELIVERY_ASAP)", required = true)
            @RequestParam DeliveryMode deliveryMode,

            @Parameter(description = "Date in ISO format (YYYY-MM-DD). Defaults to today")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<TimeSlot> slots = timeSlotService.getAvailableTimeSlots(deliveryMode, queryDate).join();

        List<EntityModel<TimeSlot>> slotModels = slots.stream()
                .map(timeSlotModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(slotModels);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/reserve")
    public EntityModel<TimeSlot> reserveSlot(@PathVariable Long id) {
        TimeSlot reservedTimeSlot = timeSlotService.reserveSlot(id).join();
        return timeSlotModelAssembler.toModel(reservedTimeSlot);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/modes")
    public List<DeliveryMode> getDeliveryModes() {
        return timeSlotService.getAllDeliveryModes();
    }
}
