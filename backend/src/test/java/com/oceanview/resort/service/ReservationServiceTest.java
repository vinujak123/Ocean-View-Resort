package com.oceanview.resort.service;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @InjectMocks
    private ReservationService service;

    private Reservation validRes;

    @BeforeEach
    public void setup() {
        validRes = new Reservation();
        validRes.setGuestName("John Doe");
        validRes.setPhone("0771234567");
        validRes.setRoomType(Reservation.RoomType.STANDARD);
        validRes.setBoardType(Reservation.BoardType.BB);
        validRes.setCheckInDate(LocalDate.now().plusDays(1));
        validRes.setCheckOutDate(LocalDate.now().plusDays(3));
    }

    @Test
    public void testCreateReservation_ValidData_CalculatesBillAndGeneratesId() throws Exception {
        // Arrange
        when(repository.findMaxReferenceId()).thenReturn("1005");
        when(repository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation created = service.create(validRes);

        // Assert
        assertEquals("1006", created.getReferenceId(), "Next Reference ID should be generated correctly");
        // 2 nights * (STANDARD 15000 + BB 0) = 2 * 15000 = 30000
        assertEquals(30000.0, created.getTotalBill(), "Total bill should be calculated based on nights and rates");
        verify(repository, times(1)).save(any(Reservation.class));
    }

    @Test
    public void testCreateReservation_MissingRequiredField_ThrowsException() {
        // Arrange
        validRes.setGuestName(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.create(validRes);
        });

        assertTrue(exception.getMessage().contains("Guest name"), "Exception should mention the missing field");
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    public void testCreateReservation_InvalidDates_ThrowsException() {
        // Arrange
        validRes.setCheckInDate(LocalDate.now().plusDays(3));
        validRes.setCheckOutDate(LocalDate.now().plusDays(1)); // checkout before checkin

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.create(validRes);
        });

        assertTrue(exception.getMessage().contains("Check-out must be at least one day after Check-in"));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    public void testCalculateTotalRevenue() {
        // Arrange
        Reservation r1 = new Reservation();
        r1.setTotalBill(15000.0);
        Reservation r2 = new Reservation();
        r2.setTotalBill(25000.0);

        when(repository.findAll()).thenReturn(Arrays.asList(r1, r2));

        // Act
        Double totalRevenue = service.calculateTotalRevenue();

        // Assert
        assertEquals(40000.0, totalRevenue);
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testUpdateReservation_ValidData_RecalculatesBill() throws Exception {
        // Arrange
        String refId = "1001";
        Reservation existingRes = new Reservation();
        existingRes.setReferenceId(refId);
        existingRes.setCheckInDate(LocalDate.now().plusDays(1));
        existingRes.setCheckOutDate(LocalDate.now().plusDays(2));
        existingRes.setRoomType(Reservation.RoomType.STANDARD);
        existingRes.setBoardType(Reservation.BoardType.BB);

        when(repository.findByReferenceId(refId)).thenReturn(Optional.of(existingRes));
        when(repository.save(any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        validRes.setRoomType(Reservation.RoomType.DELUXE); // Change room type to Deluxe (25000 + 0 = 25000)
        validRes.setCheckOutDate(LocalDate.now().plusDays(2)); // 1 night
        Reservation updated = service.update(refId, validRes);

        // Assert
        assertEquals(Reservation.RoomType.DELUXE, updated.getRoomType());
        assertEquals(25000.0, updated.getTotalBill(), "Total bill should be recalculated on update");
        verify(repository, times(1)).save(existingRes);
    }

    @Test
    public void testDeleteReservation_NotFound_ThrowsException() {
        // Arrange
        when(repository.findByReferenceId("9999")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.delete("9999");
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(repository, never()).deleteByReferenceId(anyString());
    }

    @Test
    public void testConcurrency_MultipleReservations() throws InterruptedException {
        // Setup a mock repository that simulates a slightly slow database read/write
        // to increase the chance of race conditions if they exist.
        // We will just provide a simple incrementing behavior.
        AtomicInteger mockIdCounter = new AtomicInteger(1000);
        when(repository.findMaxReferenceId()).thenAnswer(i -> String.valueOf(mockIdCounter.get()));
        when(repository.save(any(Reservation.class))).thenAnswer(i -> {
            // Update max id slightly after to simulate a gap where race conditions occur
            mockIdCounter.incrementAndGet();
            return i.getArguments()[0];
        });

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // wait for start signal

                    Reservation res = new Reservation();
                    res.setGuestName("Concurrent Guest");
                    res.setPhone("0000000000");
                    res.setRoomType(Reservation.RoomType.STANDARD);
                    res.setBoardType(Reservation.BoardType.BB);
                    res.setCheckInDate(LocalDate.now().plusDays(1));
                    res.setCheckOutDate(LocalDate.now().plusDays(2));

                    service.create(res);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Ignored for this test since we just want to execute them
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        latch.countDown();
        doneLatch.await(); // Wait for all to finish

        executor.shutdown();

        // Assert that all 10 tries succeeded in calling the method without errors
        assertEquals(10, successCount.get(), "All concurrent creation requests should successfully finish processing");
    }
}
