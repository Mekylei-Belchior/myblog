package br.com.mekylei.myblog.dtos.comment;

import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FullCommentDTO(Long id,
                             String comment,
                             @JsonProperty("date") String date,
                             String author) {

    public FullCommentDTO(Comment comment) {
        this(comment.getId(), comment.getComment(), DateUtil.formatDateTime(comment.getDate()), comment.getAuthor());
    }

}
