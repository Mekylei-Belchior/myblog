package br.com.mekylei.myblog.dtos;

import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;

import javax.validation.constraints.NotBlank;

public class UpdateNewsDto {

    @NotBlank
    private String title;
    @NotBlank
    private String content;

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

    /**
     * Updates the news
     *
     * @param id identification of the news
     * @param newsRepository persistence interface
     * @return the news updated
     */
    public News update(Long id, NewsRepository newsRepository) {
        News news = newsRepository.getById(id);
        news.setTitle(this.title);
        news.setContent(this.content);

        return news;
    }
}
