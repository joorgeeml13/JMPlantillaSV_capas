package jorge.matias.plantilla.controller.dto.request.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank
    @JsonProperty("oldPassword")
    String oldPassword,

    @NotBlank
    @JsonProperty("newPassword")
    String newPassword
) {}
