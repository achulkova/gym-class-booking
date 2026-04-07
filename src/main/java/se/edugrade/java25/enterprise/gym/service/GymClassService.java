package se.edugrade.java25.enterprise.gym.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.enterprise.gym.dto.BookingResponse;
import se.edugrade.java25.enterprise.gym.dto.GymClassRequest;
import se.edugrade.java25.enterprise.gym.dto.GymClassResponse;
import se.edugrade.java25.enterprise.gym.exception.GymClassNotFoundException;
import se.edugrade.java25.enterprise.gym.model.Booking;
import se.edugrade.java25.enterprise.gym.model.GymClass;
import se.edugrade.java25.enterprise.gym.repository.BookingRepository;
import se.edugrade.java25.enterprise.gym.repository.GymClassRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final BookingRepository bookingRepository;

    // FIND
    public Page<GymClassResponse> findAll(Pageable pageable) {
        return gymClassRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public GymClassResponse findById(Long id) {
        return gymClassRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new GymClassNotFoundException(id));
    }

    public List<GymClassResponse> findByInstructorIgnoreCase(String instructor) {
        return gymClassRepository.findByInstructorIgnoreCase(instructor)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<GymClassResponse> findByDayOfWeekIgnoreCase(String dayOfWeek) {
        return gymClassRepository.findByDayOfWeekIgnoreCase(dayOfWeek)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // CREATE
    @Transactional
    public GymClassResponse create(GymClassRequest request) {
        GymClass gymClass = new GymClass();
        gymClass.setName(request.getName());
        gymClass.setInstructor(request.getInstructor());
        gymClass.setDescription(request.getDescription());
        gymClass.setDayOfWeek(request.getDayOfWeek());
        gymClass.setStartTime(request.getStartTime());
        gymClass.setDurationMinutes(request.getDurationMinutes());
        gymClass.setMaxParticipants(request.getMaxParticipants());
        GymClass savedGymClass = gymClassRepository.save(gymClass);
        return toResponse(savedGymClass);
    }

    // UPDATE
    @Transactional
    public GymClassResponse update(Long id, GymClassRequest request) {
        GymClass gymClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new GymClassNotFoundException(id));
        gymClass.setName(request.getName());
        gymClass.setInstructor(request.getInstructor());
        gymClass.setDescription(request.getDescription());
        gymClass.setDayOfWeek(request.getDayOfWeek());
        gymClass.setStartTime(request.getStartTime());
        gymClass.setDurationMinutes(request.getDurationMinutes());
        gymClass.setMaxParticipants(request.getMaxParticipants());
        GymClass updatedGymClass = gymClassRepository.save(gymClass);
        return toResponse(updatedGymClass);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        GymClass gymClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new GymClassNotFoundException(id));
        gymClassRepository.delete(gymClass);
    }

    // Maps GymClass entity to GymClassResponse DTO
    private GymClassResponse toResponse(GymClass gymClass) {
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getName(),
                gymClass.getInstructor(),
                gymClass.getDescription(),
                gymClass.getDayOfWeek(),
                gymClass.getStartTime(),
                gymClass.getDurationMinutes(),
                gymClass.getMaxParticipants(),
                gymClass.getBookings()
                        .stream()
                        .map(this::toBookingResponse)
                        .toList()
        );
    }

    // Helper: maps Booking entity to BookingResponse DTO used in GymClassResponse
    private BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getParticipantName(),
                booking.getEmail(),
                booking.getBookedAt(),
                booking.getGymClass().getId()
        );
    }

    // VG methods
    public int getSpotsRemaining(Long id) {
        GymClass gymClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new GymClassNotFoundException(id));
        long bookedSpots = bookingRepository.countByGymClassId(id);
        return gymClass.getMaxParticipants() - (int) bookedSpots;
    }

    public List<GymClassResponse> findAvailableClasses() {
        return gymClassRepository.findAll()
                .stream()
                .filter(gymClass -> getSpotsRemaining(gymClass.getId()) > 0)
                .map(this::toResponse)
                .toList();
    }
}
