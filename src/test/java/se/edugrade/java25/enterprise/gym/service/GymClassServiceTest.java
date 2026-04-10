package se.edugrade.java25.enterprise.gym.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.edugrade.java25.enterprise.gym.dto.GymClassRequest;
import se.edugrade.java25.enterprise.gym.dto.GymClassResponse;
import se.edugrade.java25.enterprise.gym.exception.GymClassNotFoundException;
import se.edugrade.java25.enterprise.gym.model.GymClass;
import se.edugrade.java25.enterprise.gym.repository.BookingRepository;
import se.edugrade.java25.enterprise.gym.repository.GymClassRepository;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GymClassServiceTest {

    @Mock
    private GymClassRepository gymClassRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private GymClassService gymClassService;

    @Test
    @DisplayName("findById returns GymClassResponse")
    void findById_returnsResponse() {

        // Given
        GymClass gymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        gymClass.setId(1L);

        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));

        // When
        GymClassResponse response = gymClassService.findById(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Yoga");
        assertThat(response.getInstructor()).isEqualTo("Anna");
    }

    @Test
    @DisplayName("findById throws GymClassNotFoundException")
    void findById_throwsWhenNotFound() {

        // Given
        when(gymClassRepository.findById(any())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gymClassService.findById(1L))
                .isInstanceOf(GymClassNotFoundException.class);
    }

    @Test
    @DisplayName("create saves and returns GymClassResponse")
    void create_savesAndReturns() {


        // Given
        GymClassRequest request = new GymClassRequest();
        request.setName("Yoga");
        request.setInstructor("Anna");
        request.setDescription("Relax");
        request.setDayOfWeek("Monday");
        request.setStartTime("10:00");
        request.setDurationMinutes(60);
        request.setMaxParticipants(10);

        GymClass savedGymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        savedGymClass.setId(1L);

        when(gymClassRepository.save(any(GymClass.class))).thenReturn(savedGymClass);

        // When
        GymClassResponse response = gymClassService.create(request);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Yoga");
        assertThat(response.getInstructor()).isEqualTo("Anna");
        assertThat(response.getDayOfWeek()).isEqualTo("Monday");
    }

    @Test
    @DisplayName("delete removes existing gym class")
    void delete_removesExistingGymClass() {

        // Given
        GymClass gymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        gymClass.setId(1L);

        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));

        // When
        gymClassService.delete(1L);

        // Then
        verify(gymClassRepository).delete(gymClass);
    }

    // Extra tests
    @Test
    @DisplayName("getSpotsRemaining returns correct value")
    void getSpotsRemaining_returnsCorrectValue() {

        // Given
        GymClass gymClass = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        gymClass.setId(1L);

        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(bookingRepository.countByGymClassId(1L)).thenReturn(3L);

        // When
        int spotsRemaining = gymClassService.getSpotsRemaining(1L);

        // Then
        assertThat(spotsRemaining).isEqualTo(7);
    }
}


