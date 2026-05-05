package jorge.matias.plantilla.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @Email(message = "auth.validation.invalid-email")
    @NotBlank(message = "auth.validation.email-required")
    String email,

    @NotBlank(message = "auth.validation.password-required")
    String password
) {}