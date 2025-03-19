package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.news.FullNewsDTO;
import br.com.mekylei.myblog.dtos.news.NewsDTO;
import br.com.mekylei.myblog.enums.NotFoundBy;
import br.com.mekylei.myblog.exceptions.NewsNotFoundByException;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final PagedResourcesAssembler<News> pagedResourcesAssembler;


    public NewsService(NewsRepository newsRepository, PagedResourcesAssembler<News> pagedResourcesAssembler) {
        this.newsRepository = newsRepository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Transactional
    public News createNews(NewsDTO data) {
        return newsRepository.save(data.toNews());
    }

    @Transactional
    public News updateNews(long idNews, NewsDTO updateData) {
        News news = newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews));

        news.setTitle(updateData.title());
        news.setContent(updateData.content());
        news.setTags(updateData.tags());

        return newsRepository.save(news);
    }

    @Transactional
    public News deleteNews(long idNews) {
        News news = newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews));
        newsRepository.deleteById(idNews);
        return news;
    }

    public PagedModel<EntityModel<News>> getNews(Pageable pageable, String title) {
        Page<News> newsPage = (title != null) ? getNewsByTitle(pageable, title) : getNews(pageable);
        return pagedResourcesAssembler.toModel(newsPage);
    }

    private Page<News> getNews(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    private Page<News> getNewsByTitle(Pageable pageable, String title) {
        return newsRepository.findByTitleContainingIgnoreCase(pageable, title)
                .orElseThrow(() -> new NewsNotFoundByException(NotFoundBy.TITLE, title));
    }

    public PagedModel<EntityModel<News>> getNewsByTag(Pageable pageable, String tag) {
        Page<News> newsPage = newsRepository.findByTags(pageable, tag)
                .orElseThrow(() -> new NewsNotFoundByException(NotFoundBy.TAG, tag));
        return pagedResourcesAssembler.toModel(newsPage);
    }

    public FullNewsDTO getCompleteNews(long idNews) {
        return new FullNewsDTO(newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews)));
    }

}
