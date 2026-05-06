package com.parkease.booking.dto;

import com.parkease.booking.entity.Booking;
import com.parkease.booking.enums.BookingStatus;
import com.parkease.booking.enums.BookingType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponseDTO {

    private Long bookingId;
    private Long userId;
    private Long lotId;
    private Long spotId;
    private String vehiclePlate;
    private String vehicleType;
    private BookingType bookingType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
    private BookingStatus status;
    private Double pricePerHour;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponseDTO from(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .lotId(booking.getLotId())
                .spotId(booking.getSpotId())
                .vehiclePlate(booking.getVehiclePlate())
                .vehicleType(booking.getVehicleType())
                .bookingType(booking.getBookingType())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .actualCheckInTime(booking.getActualCheckInTime())
                .actualCheckOutTime(booking.getActualCheckOutTime())
                .status(booking.getStatus())
                .pricePerHour(booking.getPricePerHour())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
