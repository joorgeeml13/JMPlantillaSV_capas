package jorge.matias.plantilla.controller.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
