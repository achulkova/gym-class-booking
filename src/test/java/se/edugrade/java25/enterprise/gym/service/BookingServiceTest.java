package se.edugrade.java25.enterprise.gym.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.edugrade.java25.enterprise.gym.dto.BookingRequest;
import se.edugrade.java25.enterprise.gym.dto.BookingResponse;
import se.edugrade.java25.enterprise.gym.exception.CapacityExceededException;
import se.edugrade.java25.enterprise.gym.model.Booking;
import se.edugrade.java25.enterprise.gym.model.GymClass;
import se.edugrade.java25.enterprise.gym.repository.BookingRepository;
import se.edugrade.java25.enterprise.gym.repository.GymClassRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GymClassRepository gymClassRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("createBooking is successful when capacity is not exceeded")
    void createBooking_isSuccessfulWhenCapacityIsNotExceeded() {

        // Given
        GymClass gymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                2
        );
        gymClass.setId(1L);

        gymClass.setBookings(List.of());  // Simulate empty bookings list

        // Create a DTO BookingRequest with valid data
        BookingRequest request = new BookingRequest();
        request.setParticipantName("Lars");
        request.setEmail("lars@email.com");

        // Create a Booking entity with valid data and set its ID and gym class
        Booking savedBooking = new Booking("Lars", "lars@email.com");
        savedBooking.setId(1L);
        savedBooking.setGymClass(gymClass);

        // Mock the repository methods;
        // if findById(1L) is called, return the gym class; if save is called, return the saved booking
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // When
        BookingResponse response = bookingService.createBooking(1L, request);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getParticipantName()).isEqualTo("Lars");
        assertThat(response.getGymClassId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createBooking throws CapacityExceededException when class is full")
    void createBooking_throwsWhenClassIsFull() {

        // Given (with limited capacity)
        GymClass gymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                1
        );
        gymClass.setId(1L);

        // Already booked class with one booking, which is the maximum capacity
        Booking existingBooking = new Booking("Lars", "lars@email.com");
        existingBooking.setGymClass(gymClass);

        // Simulate full capacity
        gymClass.setBookings(List.of(existingBooking));

        // Create a DTO BookingRequest with valid data
        BookingRequest request = new BookingRequest();
        request.setParticipantName("Anya");
        request.setEmail("anya@email.com");

        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));

        // When + Then
        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(CapacityExceededException.class);

        // Verify that the booking was not saved because the class is full
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Extra tests
    @Test
    @DisplayName("deleteBooking removes existing booking")
    void deleteBooking_removesBooking() {

        // Given
        Booking booking = new Booking("Lars", "lars@email.com");
        booking.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        bookingService.deleteBooking(1L);

        // Then
        verify(bookingRepository).delete(booking);
    }
}
