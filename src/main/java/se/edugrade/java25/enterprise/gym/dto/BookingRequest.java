package se.edugrade.java25.enterprise.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank
    private String participantName;
    @NotBlank @Email
    private String email;

}
