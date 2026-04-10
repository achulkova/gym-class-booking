package se.edugrade.java25.enterprise.gym.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
