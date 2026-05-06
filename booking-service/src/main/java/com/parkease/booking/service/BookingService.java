package com.parkease.booking.service;

import com.parkease.booking.dto.BookingRequestDTO;
import com.parkease.booking.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    Booking createBooking(BookingRequestDTO dto);

    Booking getBookingById(Long id);

    List<Booking> getBookingHistory(Long userId);

    List<Booking> getBookingsByLot(Long lotId);

    Booking checkIn(Long bookingId);

    Booking checkOut(Long bookingId);

    Booking cancelBooking(Long bookingId);

    Booking extendBooking(Long bookingId, LocalDateTime newEndTime);

    Double calculateAmount(LocalDateTime start, LocalDateTime end, Double pricePerHour);
}
