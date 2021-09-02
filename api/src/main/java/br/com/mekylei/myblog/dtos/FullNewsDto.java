package br.com.mekylei.myblog.dtos;

import br.com.mekylei.myblog.models.News;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FullNewsDto {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime date;
    private String content;
    private List<FullCommentDto> comment;

    public FullNewsDto(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.author = news.getAuthor();
        this.date = news.getDate();
        this.content = news.getContent();
        this.comment = new ArrayList<>();
        this.comment.addAll(news.getComment().stream().map(FullCommentDto::new).collect(Collectors.toList()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FullCommentDto> getComment() {
        return comment;
    }

    public void setComment(List<FullCommentDto> comment) {
        this.comment = comment;
    }
}
