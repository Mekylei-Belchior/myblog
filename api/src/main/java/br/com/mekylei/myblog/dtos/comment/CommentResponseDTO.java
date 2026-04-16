package br.com.mekylei.myblog.dtos.comment;

import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentResponseDTO(Long id,
                                 String comment,
                                 @JsonProperty("date") String date,
                                 String author) {

    public static CommentResponseDTO from(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getComment(),
                DateUtil.formatDateTime(comment.getDate()),
                comment.getAuthor()
        );
    }

}
