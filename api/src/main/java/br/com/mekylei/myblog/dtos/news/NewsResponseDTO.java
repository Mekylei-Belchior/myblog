package br.com.mekylei.myblog.dtos.news;

import br.com.mekylei.myblog.dtos.comment.CommentResponseDTO;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NewsResponseDTO(Long id,
                              String title,
                              String author,
                              @JsonProperty("date") String date,
                              String content,
                              List<CommentResponseDTO> comment,
                              List<String> tag) {

    public static NewsResponseDTO from(News news) {
        return new NewsResponseDTO(
                news.getId(),
                news.getTitle(),
                news.getAuthor(),
                DateUtil.formatDateTime(news.getDate()),
                news.getContent(),
                news.getComment().stream().map(CommentResponseDTO::from).toList(),
                news.getTags()
        );
    }

}
