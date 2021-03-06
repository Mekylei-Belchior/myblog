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
    private List<FullCommentDto> comment = new ArrayList<>();
    private List<String> tag = new ArrayList<>();

    public FullNewsDto(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.author = news.getAuthor();
        this.date = news.getDate();
        this.content = news.getContent();
        this.comment.addAll(news.getComment().stream().map(FullCommentDto::new).collect(Collectors.toList()));
        this.tag.addAll(news.getTags());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public List<FullCommentDto> getComment() {
        return comment;
    }

    public List<String> getTag() {
        return tag;
    }
}
