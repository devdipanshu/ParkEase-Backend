package com.parkease.booking;

import com.parkease.booking.client.AnalyticsClient;
import com.parkease.booking.client.NotificationClient;
import com.parkease.booking.client.ParkingLotClient;
import com.parkease.booking.client.SpotClient;
import com.parkease.booking.dto.external.SpotDTO;
import com.parkease.booking.entity.Booking;
import com.parkease.booking.enums.BookingStatus;
import com.parkease.booking.enums.BookingType;
import com.parkease.booking.exception.ResourceNotFoundException;
import com.parkease.booking.repository.BookingRepository;
import com.parkease.booking.service.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {


    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SpotClient spotClient;
    @Mock
    private ParkingLotClient parkingLotClient;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private AnalyticsClient analyticsClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking reservedBooking;
    private Booking activeBooking;

    @BeforeEach
    void setUp() {
        reservedBooking = Booking.builder()
                .bookingId(1L)
                .userId(100L)
                .lotId(10L)
                .spotId(5L)
                .vehiclePlate("MH12AB1234")
                .vehicleType("CAR")
                .bookingType(BookingType.PRE)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.RESERVED)
                .pricePerHour(50.0)
                .build();

        activeBooking = Booking.builder()
                .bookingId(2L)
                .userId(100L)
                .lotId(10L)
                .spotId(5L)
                .vehiclePlate("MH12AB1234")
                .vehicleType("CAR")
                .status(BookingStatus.ACTIVE)
                .pricePerHour(50.0)
                .actualCheckInTime(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void getBookingById_existingId_returnsBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));

        Booking result = bookingService.getBookingById(1L);

        assertThat(result.getBookingId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_unknownId_throwsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBookingHistory_returnsUserBookings() {
        when(bookingRepository.findByUserId(100L)).thenReturn(List.of(reservedBooking));

        List<Booking> result = bookingService.getBookingHistory(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void checkIn_reservedBooking_setsActiveStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(reservedBooking);
        doNothing().when(spotClient).checkInSpot(5L);
        doNothing().when(notificationClient).sendNotification(any());

        Booking result = bookingService.checkIn(1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        verify(spotClient).checkInSpot(5L);
    }

    @Test
    void checkIn_nonReservedBooking_throwsException() {
        reservedBooking.setStatus(BookingStatus.ACTIVE);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));

        assertThatThrownBy(() -> bookingService.checkIn(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    void checkOut_activeBooking_setsCompletedStatus() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(activeBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(activeBooking);
        doNothing().when(spotClient).releaseSpot(5L);
        doNothing().when(parkingLotClient).incrementAvailable(10L);
        doNothing().when(notificationClient).sendNotification(any());

        Booking result = bookingService.checkOut(2L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(result.getTotalAmount()).isNotNull();
    }

    @Test
    void checkOut_nonActiveBooking_throwsException() {
        activeBooking.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(activeBooking));

        assertThatThrownBy(() -> bookingService.checkOut(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void cancelBooking_reservedBooking_setsCancelledStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(reservedBooking);
        doNothing().when(spotClient).releaseSpot(5L);
        doNothing().when(parkingLotClient).incrementAvailable(10L);
        doNothing().when(notificationClient).sendNotification(any());

        Booking result = bookingService.cancelBooking(1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_completedBooking_throwsException() {
        reservedBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    void extendBooking_futureEndTime_updatesEndTime() {
        LocalDateTime newEnd = LocalDateTime.now().plusHours(5);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(reservedBooking);

        Booking result = bookingService.extendBooking(1L, newEnd);

        assertThat(result.getEndTime()).isEqualTo(newEnd);
    }

    @Test
    void extendBooking_pastEndTime_throwsException() {
        LocalDateTime pastTime = reservedBooking.getEndTime().minusMinutes(30);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(reservedBooking));

        assertThatThrownBy(() -> bookingService.extendBooking(1L, pastTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be after");
    }

    @Test
    void calculateAmount_roundsUpToFullHour() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(90);

        Double amount = bookingService.calculateAmount(start, end, 50.0);

        assertThat(amount).isEqualTo(100.0);
    }

    @Test
    void calculateAmount_lessThanOneHour_chargesOneHourMinimum() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        Double amount = bookingService.calculateAmount(start, end, 50.0);

        assertThat(amount).isEqualTo(50.0);
    }
}
