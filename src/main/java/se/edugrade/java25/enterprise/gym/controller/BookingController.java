package se.edugrade.java25.enterprise.gym.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.edugrade.java25.enterprise.gym.dto.BookingRequest;
import se.edugrade.java25.enterprise.gym.dto.BookingResponse;
import se.edugrade.java25.enterprise.gym.service.BookingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // FIND
    @GetMapping("/classes/{gymClassId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForClass(@PathVariable Long gymClassId) {
        return ResponseEntity.ok(bookingService.findByGymClassId(gymClassId));
    }

    // CREATE
    @PostMapping("/classes/{gymClassId}/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long gymClassId,
            @RequestBody @Valid BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(gymClassId, request));
    }

    // DELETE
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
