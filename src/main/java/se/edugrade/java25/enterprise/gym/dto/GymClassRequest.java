package se.edugrade.java25.enterprise.gym.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GymClassRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String instructor;
    private String description;
    @NotBlank
    private String dayOfWeek;
    @NotBlank
    private String startTime;
    @Min(15) @Max(120)
    private int durationMinutes;
    @Min(1) @Max(50)
    private int maxParticipants;

}
