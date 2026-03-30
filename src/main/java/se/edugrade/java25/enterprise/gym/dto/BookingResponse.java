package se.edugrade.java25.enterprise.gym.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String participantName;
    private String email;
    private LocalDateTime bookedAt;
    private Long gymClassId;
}
