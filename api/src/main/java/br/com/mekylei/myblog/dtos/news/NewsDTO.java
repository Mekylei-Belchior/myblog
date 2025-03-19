package br.com.mekylei.myblog.dtos.news;

import br.com.mekylei.myblog.models.News;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record NewsDTO(@NotBlank String title, @NotBlank String author, @NotBlank String content, List<String> tags) {

    public News toNews() {
        return new News(title, author, content, tags);
    }

}
