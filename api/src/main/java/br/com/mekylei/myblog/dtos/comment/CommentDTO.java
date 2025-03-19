package br.com.mekylei.myblog.dtos.comment;


import jakarta.validation.constraints.NotBlank;

public record CommentDTO(@NotBlank String author, @NotBlank String comment) {
}