package com.example.kata.assembler;

import com.example.kata.controller.TimeSlotController;
import com.example.kata.model.TimeSlot;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TimeSlotModelAssembler implements RepresentationModelAssembler<TimeSlot, EntityModel<TimeSlot>> {

    @Override
    public EntityModel<TimeSlot> toModel(TimeSlot timeSlot) {
        return EntityModel.of(
                timeSlot,
                linkTo(methodOn(TimeSlotController.class).reserveSlot(timeSlot.getId())).withRel("reserve"),
                linkTo(methodOn(TimeSlotController.class).getAvailableTimeSlots(timeSlot.getDeliveryMode(), null)).withRel("timeslots")
        );
    }
}
