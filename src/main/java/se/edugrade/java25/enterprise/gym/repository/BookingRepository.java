package se.edugrade.java25.enterprise.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.edugrade.java25.enterprise.gym.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGymClassId(Long gymClassId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.gymClass.id = :classId")
    long countByGymClassId(@Param("classId") Long classId);

}
