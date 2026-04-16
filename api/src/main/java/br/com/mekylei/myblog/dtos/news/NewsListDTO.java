package br.com.mekylei.myblog.dtos.news;

import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.utils.DateUtil;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Relation(collectionRelation = "newsList")
public record NewsListDTO(Long id,
                          String title,
                          String author,
                          String date,
                          String content,
                          List<String> tags) {

    public static NewsListDTO from(News news) {
        return new NewsListDTO(
                news.getId(),
                news.getTitle(),
                news.getAuthor(),
                DateUtil.formatDateTime(news.getDate()),
                news.getContent(),
                news.getTags()
        );
    }

}
