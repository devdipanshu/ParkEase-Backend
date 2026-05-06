package com.parkease.booking.repository;

import com.parkease.booking.entity.Booking;
import com.parkease.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByLotId(Long lotId);

    List<Booking> findBySpotId(Long spotId);

    List<Booking> findByStatus(BookingStatus status);

    Optional<Booking> findBySpotIdAndStatusIn(Long spotId, List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' AND b.startTime < :cutoff")
    List<Booking> findExpiredReservations(@Param("cutoff") LocalDateTime cutoff);
}
