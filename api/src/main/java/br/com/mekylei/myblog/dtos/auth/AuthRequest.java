package br.com.mekylei.myblog.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank String email, @NotBlank String password) {
}
