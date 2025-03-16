package br.com.mekylei.myblog.dtos;


import jakarta.validation.constraints.NotBlank;

public class CommentDTO {

    @NotBlank
    private String author;
    @NotBlank
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
