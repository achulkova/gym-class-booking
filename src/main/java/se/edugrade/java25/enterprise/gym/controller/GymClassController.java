package se.edugrade.java25.enterprise.gym.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.edugrade.java25.enterprise.gym.dto.GymClassRequest;
import se.edugrade.java25.enterprise.gym.dto.GymClassResponse;
import se.edugrade.java25.enterprise.gym.service.GymClassService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/classes")
public class GymClassController {

    private final GymClassService gymClassService;

    @GetMapping
    public ResponseEntity<Page<GymClassResponse>> getAllClasses(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(gymClassService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymClassResponse> getGymClassById(@PathVariable Long id) {
        return ResponseEntity.ok(gymClassService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GymClassResponse>> searchClassesByInstructor(@RequestParam String instructor) {
        return ResponseEntity.ok(gymClassService.findByInstructorIgnoreCase(instructor));
    }

    @PostMapping
    public ResponseEntity<GymClassResponse> createGymClass(@RequestBody @Valid GymClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gymClassService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymClassResponse> updateGymClass(
            @PathVariable Long id,
            @RequestBody @Valid GymClassRequest request) {
        return ResponseEntity.ok(gymClassService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGymClass(@PathVariable Long id) {
        gymClassService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // VG controllers
    @GetMapping("/{id}/spots-remaining")
    public ResponseEntity<Map<String, Integer>> getSpotsRemaining(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("spotsRemaining", gymClassService.getSpotsRemaining(id)));
    }

    @GetMapping("/available")
    public ResponseEntity<List<GymClassResponse>> getAvailableClasses() {
        return ResponseEntity.ok(gymClassService.findAvailableClasses());
    }
}
