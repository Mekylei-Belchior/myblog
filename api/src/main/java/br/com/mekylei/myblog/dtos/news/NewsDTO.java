package br.com.mekylei.myblog.dtos.news;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record NewsDTO(@NotBlank String title, @NotBlank String author, @NotBlank String content, List<String> tags) {
}
