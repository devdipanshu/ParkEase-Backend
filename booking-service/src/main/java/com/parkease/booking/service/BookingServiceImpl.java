package com.parkease.booking.service;

import com.parkease.booking.client.AnalyticsClient;
import com.parkease.booking.client.NotificationClient;
import com.parkease.booking.client.ParkingLotClient;
import com.parkease.booking.client.SpotClient;
import com.parkease.booking.dto.BookingRequestDTO;
import com.parkease.booking.dto.external.LotDTO;
import com.parkease.booking.dto.external.NotificationRequest;
import com.parkease.booking.dto.external.OccupancyLogRequest;
import com.parkease.booking.dto.external.SpotDTO;
import com.parkease.booking.entity.Booking;
import com.parkease.booking.enums.BookingStatus;
import com.parkease.booking.exception.ResourceNotFoundException;
import com.parkease.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SpotClient spotClient;
    private final ParkingLotClient parkingLotClient;
    private final NotificationClient notificationClient;
    private final AnalyticsClient analyticsClient;

    @Override
    public Booking createBooking(BookingRequestDTO dto) {
        SpotDTO spot = spotClient.getSpot(dto.getSpotId());
        if (!"AVAILABLE".equals(spot.getStatus())) {
            throw new IllegalStateException("Spot " + dto.getSpotId() + " is not available (current status: " + spot.getStatus() + ")");
        }

        spotClient.reserveSpot(dto.getSpotId());

        try {
            parkingLotClient.decrementAvailable(dto.getLotId());
        } catch (Exception e) {
            log.error("Failed to decrement lot {}, rolling back spot {} reservation", dto.getLotId(), dto.getSpotId());
            spotClient.releaseSpot(dto.getSpotId());
            throw e;
        }

        // Fetch updated lot data for analytics — non-critical, failure is tolerated
        LotDTO lot = null;
        try {
            lot = parkingLotClient.getLot(dto.getLotId());
        } catch (Exception e) {
            log.warn("Could not fetch lot {} for analytics logging: {}", dto.getLotId(), e.getMessage());
        }

        Booking booking = Booking.builder()
                .userId(dto.getUserId())
                .lotId(dto.getLotId())
                .spotId(dto.getSpotId())
                .vehiclePlate(dto.getVehiclePlate().toUpperCase())
                .vehicleType(dto.getVehicleType())
                .bookingType(dto.getBookingType())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.RESERVED)
                .pricePerHour(spot.getPricePerHour())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created for user {} on spot {}", saved.getBookingId(), dto.getUserId(), dto.getSpotId());

        if (lot != null) {
            logOccupancy(dto.getLotId(), dto.getSpotId(), lot, dto.getVehicleType());
        }

        try {
            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipientId(dto.getUserId())
                    .type("BOOKING_CONFIRMED")
                    .channel("APP")
                    .title("Booking Confirmed")
                    .message("Your booking #" + saved.getBookingId() + " is confirmed for spot " + dto.getSpotId())
                    .build());
        } catch (Exception e) {
            log.warn("Notification failed for booking {}: {}", saved.getBookingId(), e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingHistory(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByLot(Long lotId) {
        return bookingRepository.findByLotId(lotId);
    }

    @Override
    public Booking checkIn(Long bookingId) {
        Booking booking = findOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Check-in requires RESERVED status (current: " + booking.getStatus() + ")");
        }

        spotClient.checkInSpot(booking.getSpotId());

        booking.setStatus(BookingStatus.ACTIVE);
        booking.setActualCheckInTime(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} checked in", bookingId);

        logOccupancySafe(booking.getLotId(), booking.getSpotId(), booking.getVehicleType());

        try {
            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipientId(booking.getUserId())
                    .type("CHECKIN")
                    .channel("APP")
                    .title("Check-In Successful")
                    .message("You have checked in for booking #" + bookingId)
                    .build());
        } catch (Exception e) {
            log.warn("Notification failed for booking {}: {}", bookingId, e.getMessage());
        }

        return saved;
    }

    @Override
    public Booking checkOut(Long bookingId) {
        Booking booking = findOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Check-out requires ACTIVE status (current: " + booking.getStatus() + ")");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        double totalAmount = calculateAmount(booking.getActualCheckInTime(), checkOutTime, booking.getPricePerHour());

        spotClient.releaseSpot(booking.getSpotId());
        parkingLotClient.incrementAvailable(booking.getLotId());

        booking.setActualCheckOutTime(checkOutTime);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.COMPLETED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} checked out, total amount: {}", bookingId, totalAmount);

        logOccupancySafe(booking.getLotId(), booking.getSpotId(), booking.getVehicleType());

        try {
            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipientId(booking.getUserId())
                    .type("CHECKOUT")
                    .channel("APP")
                    .title("Check-Out Successful")
                    .message("Booking #" + bookingId + " completed. Amount charged: ₹" + totalAmount)
                    .build());
        } catch (Exception e) {
            log.warn("Notification failed for booking {}: {}", bookingId, e.getMessage());
        }

        return saved;
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = findOrThrow(bookingId);
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a " + booking.getStatus() + " booking");
        }

        spotClient.releaseSpot(booking.getSpotId());
        parkingLotClient.incrementAvailable(booking.getLotId());

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled", bookingId);

        logOccupancySafe(booking.getLotId(), booking.getSpotId(), booking.getVehicleType());

        try {
            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipientId(booking.getUserId())
                    .type("BOOKING_CANCELLED")
                    .channel("APP")
                    .title("Booking Cancelled")
                    .message("Booking #" + bookingId + " has been cancelled")
                    .build());
        } catch (Exception e) {
            log.warn("Notification failed for booking {}: {}", bookingId, e.getMessage());
        }

        return saved;
    }

    @Override
    public Booking extendBooking(Long bookingId, LocalDateTime newEndTime) {
        Booking booking = findOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.RESERVED && booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only RESERVED or ACTIVE bookings can be extended (current: " + booking.getStatus() + ")");
        }
        if (!newEndTime.isAfter(booking.getEndTime())) {
            throw new IllegalStateException("New end time must be after the current end time: " + booking.getEndTime());
        }
        booking.setEndTime(newEndTime);
        log.info("Booking {} extended to {}", bookingId, newEndTime);
        return bookingRepository.save(booking);
    }

    @Override
    public Double calculateAmount(LocalDateTime start, LocalDateTime end, Double pricePerHour) {
        long minutes = Duration.between(start, end).toMinutes();
        double hours = Math.ceil(minutes / 60.0);
        if (hours < 1.0) hours = 1.0;
        return hours * pricePerHour;
    }

    private void logOccupancy(Long lotId, Long spotId, LotDTO lot, String vehicleType) {
        try {
            int total = lot.getTotalSpots() != null ? lot.getTotalSpots() : 0;
            int available = lot.getAvailableSpots() != null ? lot.getAvailableSpots() : 0;
            double rate = total > 0 ? ((double)(total - available) / total) * 100.0 : 0.0;

            analyticsClient.logOccupancy(OccupancyLogRequest.builder()
                    .lotId(lotId)
                    .spotId(spotId)
                    .occupancyRate(rate)
                    .availableSpots(available)
                    .totalSpots(total)
                    .vehicleType(vehicleType)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Analytics logging failed for lot {}: {}", lotId, e.getMessage());
        }
    }

    private void logOccupancySafe(Long lotId, Long spotId, String vehicleType) {
        try {
            LotDTO lot = parkingLotClient.getLot(lotId);
            logOccupancy(lotId, spotId, lot, vehicleType);
        } catch (Exception e) {
            log.warn("Analytics logging failed (lot fetch) for lot {}: {}", lotId, e.getMessage());
        }
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }
}
