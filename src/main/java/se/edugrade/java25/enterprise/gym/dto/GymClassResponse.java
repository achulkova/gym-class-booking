package se.edugrade.java25.enterprise.gym.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GymClassResponse {

    private Long id;
    private String name;
    private String instructor;
    private String description;
    private String dayOfWeek;
    private String startTime;
    private int durationMinutes;
    private int maxParticipants;
    private List<BookingResponse> bookings;

}
