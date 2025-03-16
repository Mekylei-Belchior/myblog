package br.com.mekylei.myblog.dtos;

import br.com.mekylei.myblog.models.News;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FullNewsDTO {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime date;
    private String content;
    private List<FullCommentDTO> comment = new ArrayList<>();
    private List<String> tag = new ArrayList<>();

    public FullNewsDTO(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.author = news.getAuthor();
        this.date = news.getDate();
        this.content = news.getContent();
        this.comment.addAll(news.getComment().stream().map(FullCommentDTO::new).collect(Collectors.toList()));
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

    public List<FullCommentDTO> getComment() {
        return comment;
    }

    public List<String> getTag() {
        return tag;
    }
}
