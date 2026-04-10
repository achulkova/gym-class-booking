package se.edugrade.java25.enterprise.gym.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {

    public static AuthResponse of(String accessToken, String tokenType, Long expiresIn) {
        return new AuthResponse(accessToken, "Bearer", expiresIn);
    }
}
