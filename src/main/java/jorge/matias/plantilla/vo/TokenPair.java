package jorge.matias.plantilla.vo;

import lombok.*;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken
) {}
