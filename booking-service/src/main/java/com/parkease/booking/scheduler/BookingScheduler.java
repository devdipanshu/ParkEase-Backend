package com.parkease.booking.scheduler;

import com.parkease.booking.entity.Booking;
import com.parkease.booking.repository.BookingRepository;
import com.parkease.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    // Runs every 5 minutes; auto-cancels RESERVED bookings past their start time + 15 min grace
    @Scheduled(fixedRate = 300000)
    public void autoCancelExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Booking> expired = bookingRepository.findExpiredReservations(cutoff);

        if (expired.isEmpty()) {
            return;
        }

        log.info("Found {} expired reservations to auto-cancel", expired.size());
        for (Booking booking : expired) {
            try {
                bookingService.cancelBooking(booking.getBookingId());
                log.info("Auto-cancelled booking {}", booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel booking {}: {}", booking.getBookingId(), e.getMessage());
            }
        }
    }
}
