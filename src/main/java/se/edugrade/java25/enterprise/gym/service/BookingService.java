package se.edugrade.java25.enterprise.gym.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.edugrade.java25.enterprise.gym.dto.BookingRequest;
import se.edugrade.java25.enterprise.gym.dto.BookingResponse;
import se.edugrade.java25.enterprise.gym.exception.BookingNotFoundException;
import se.edugrade.java25.enterprise.gym.exception.CapacityExceededException;
import se.edugrade.java25.enterprise.gym.exception.GymClassNotFoundException;
import se.edugrade.java25.enterprise.gym.model.Booking;
import se.edugrade.java25.enterprise.gym.model.GymClass;
import se.edugrade.java25.enterprise.gym.repository.BookingRepository;
import se.edugrade.java25.enterprise.gym.repository.GymClassRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GymClassRepository gymClassRepository;

    // FIND
    public List<BookingResponse> findByGymClassId(Long gymClassId) {

        gymClassRepository.findById(gymClassId)
                .orElseThrow(() -> new GymClassNotFoundException(gymClassId));

        return bookingRepository.findByGymClassId(gymClassId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // CREATE
    @Transactional
    public BookingResponse createBooking(Long gymClassId, BookingRequest request) {
        GymClass gymClass = gymClassRepository.findById(gymClassId)
                .orElseThrow(() -> new GymClassNotFoundException(gymClassId));

        if (gymClass.getBookings().size() >= gymClass.getMaxParticipants()) {
            throw new CapacityExceededException(
                    "GymClass '" + gymClass.getName() + "' is full (max "
                    + gymClass.getMaxParticipants() + " participants)"
            );
        }

        Booking booking = new Booking();
        booking.setParticipantName(request.getParticipantName());
        booking.setEmail(request.getEmail());
        booking.setGymClass(gymClass);
        Booking savedBooking = bookingRepository.save(booking);
        return toResponse(savedBooking);
    }

    // DELETE
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        bookingRepository.delete(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getParticipantName(),
                booking.getEmail(),
                booking.getBookedAt(),
                booking.getGymClass().getId()
        );
    }
}
