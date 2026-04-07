package se.edugrade.java25.enterprise.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.edugrade.java25.enterprise.gym.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find all bookings for a given gym class ID.
     * @param gymClassId the ID of the gym class.
     * @return a list of {@link Booking} entities linked to the given gym class.
     */
    List<Booking> findByGymClassId(Long gymClassId);

    /**
     * Count the number of bookings for a given gym class ID.
     * @param classId the ID of the gym class.
     * @return the total number of bookings for the given gym class.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.gymClass.id = :classId")
    long countByGymClassId(@Param("classId") Long classId);

}
