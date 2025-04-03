package br.com.mekylei.myblog.dtos.news;

import br.com.mekylei.myblog.dtos.comment.FullCommentDTO;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FullNewsDTO(Long id,
                          String title,
                          String author,
                          @JsonProperty("date") String date,
                          String content,
                          List<FullCommentDTO> comment,
                          List<String> tag) {

    public FullNewsDTO(News news) {
        this(
                news.getId(),
                news.getTitle(),
                news.getAuthor(),
                DateUtil.formatDateTime(news.getDate()),
                news.getContent(),
                news.toFullComment(),
                news.getTags()
        );
    }

}
