package br.com.mekylei.myblog.dtos;

import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class UpdateNewsDTO {

    @NotBlank
    private String title;
    @NotBlank
    private String content;

    private List<String> tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Updates the news
     *
     * @param id             identification of the news
     * @param newsRepository persistence interface
     * @return the news updated
     */
    public News update(Long id, NewsRepository newsRepository) {
        News news = newsRepository.getReferenceById(id);
        news.setTitle(this.title);
        news.setContent(this.content);
        news.setTags(this.tags);

        return news;
    }
}
