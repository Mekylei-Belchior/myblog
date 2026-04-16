package br.com.mekylei.myblog.dtos.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDTO(@NotBlank String author, @NotBlank String comment) {
}
