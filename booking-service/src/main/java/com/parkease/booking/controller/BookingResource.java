package com.parkease.booking.controller;

import com.parkease.booking.dto.BookingRequestDTO;
import com.parkease.booking.dto.BookingResponseDTO;
import com.parkease.booking.dto.ExtendBookingRequest;
import com.parkease.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingResource {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BookingResponseDTO.from(bookingService.createBooking(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponseDTO.from(bookingService.getBookingById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingHistory(userId).stream()
                .map(BookingResponseDTO::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(bookingService.getBookingsByLot(lotId).stream()
                .map(BookingResponseDTO::from)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<BookingResponseDTO> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponseDTO.from(bookingService.checkIn(id)));
    }

    @PutMapping("/{id}/checkout")
    public ResponseEntity<BookingResponseDTO> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponseDTO.from(bookingService.checkOut(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponseDTO.from(bookingService.cancelBooking(id)));
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<BookingResponseDTO> extendBooking(@PathVariable Long id,
                                                             @Valid @RequestBody ExtendBookingRequest request) {
        return ResponseEntity.ok(BookingResponseDTO.from(
                bookingService.extendBooking(id, request.getNewEndTime())));
    }
}
