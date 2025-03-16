package br.com.mekylei.myblog.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class NewsDTO {

    @NotBlank
    private String title;
    @NotBlank
    private String author;
    @NotBlank
    private String content;

    private List<String> tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
