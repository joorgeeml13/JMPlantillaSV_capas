package jorge.matias.plantilla.controller.dto.response;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String code,
    String message,
    String path
) {}
