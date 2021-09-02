package br.com.mekylei.myblog.dtos;

import br.com.mekylei.myblog.models.Comment;

import java.time.LocalDateTime;

public class FullCommentDto {
    private Long id;
    private String comment;
    private LocalDateTime date;
    private String author;

    public FullCommentDto(Comment comment) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.date = comment.getDate();
        this.author = comment.getAuthor();
    }

    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }
}
