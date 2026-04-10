package se.edugrade.java25.enterprise.gym.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import se.edugrade.java25.enterprise.gym.model.Booking;
import se.edugrade.java25.enterprise.gym.model.GymClass;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
public class GymClassRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GymClassRepository gymClassRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("findByInstructor returns correct classes")
    void findByInstructor() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );

        GymClass hiit = new GymClass(
                "HIIT",
                "Tobias",
                "Hard training",
                "Tuesday",
                "12:00",
                45,
                15
        );
        entityManager.persistAndFlush(yoga);
        entityManager.persistAndFlush(hiit);

        // When
        List<GymClass> result = gymClassRepository.findByInstructorIgnoreCase("anna");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("findByDayOfWeek returns correct classes")
    void findByDayOfWeek() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );

        GymClass hiit = new GymClass(
                "HIIT",
                "Tobias",
                "Hard training",
                "Tuesday",
                "12:00",
                45,
                15
        );
        entityManager.persistAndFlush(yoga);
        entityManager.persistAndFlush(hiit);

        // When
        List<GymClass> result = gymClassRepository.findByDayOfWeekIgnoreCase("monday");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("countByGymClassId returns correct number of bookings")
    void countByGymClassId() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        GymClass savedGymClass = entityManager.persistAndFlush(yoga);

        Booking bookingOne = new Booking("Lars", "lars@example.com");
        bookingOne.setGymClass(savedGymClass);

        Booking bookingTwo = new Booking("Valdemar", "valdemar@example.com");
        bookingTwo.setGymClass(savedGymClass);

        entityManager.persistAndFlush(bookingOne);
        entityManager.persistAndFlush(bookingTwo);

        // When
        long count = bookingRepository.countByGymClassId(savedGymClass.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByGymClassId returns 0 for class without bookings")
    void countByGymClassIdReturnsZeroWhenNoBookingsExist() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        GymClass savedGymClass = entityManager.persistAndFlush(yoga);

        // When
        long count = bookingRepository.countByGymClassId(savedGymClass.getId());

        // Then
        assertThat(count).isEqualTo(0);
    }

    // Extra tests
    @Test
    @DisplayName("findByInstructor returns empty list when no match")
    void findByInstructor_returnsEmpty() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        entityManager.persistAndFlush(yoga);

        // When
        List<GymClass> result = gymClassRepository.findByInstructorIgnoreCase("Lars");

        // Then
        assertThat(result).isEmpty();
    }
    @Test
    @DisplayName("findByDayOfWeek returns empty list when no match")
    void findByDayOfWeek_returnsEmpty() {

        // Given
        GymClass yoga = new GymClass(
                "Yoga",
                "Anna",
                "Relax",
                "Monday",
                "10:00",
                60,
                10
        );
        entityManager.persistAndFlush(yoga);

        // When
        List<GymClass> result = gymClassRepository.findByDayOfWeekIgnoreCase("Sunday");

        // Then
        assertThat(result).isEmpty();
    }
}