package jorge.matias.plantilla.controller.dto.request.account;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank
    String oldPaswword,

    @NotBlank
    String newPassword
) {}
