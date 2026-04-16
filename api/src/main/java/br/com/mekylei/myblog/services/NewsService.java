package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.news.NewsListDTO;
import br.com.mekylei.myblog.dtos.news.NewsRequestDTO;
import br.com.mekylei.myblog.dtos.news.NewsResponseDTO;
import br.com.mekylei.myblog.enums.NotFoundBy;
import br.com.mekylei.myblog.exceptions.NewsNotFoundByException;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NewsService {

    private final NewsRepository newsRepository;


    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Transactional
    public NewsResponseDTO createNews(NewsRequestDTO data) {
        News news = new News(data.title(), data.author(), data.content(), data.tags());
        return NewsResponseDTO.from(newsRepository.save(news));
    }

    @Transactional
    public NewsResponseDTO updateNews(long idNews, NewsRequestDTO updateData) {
        News news = newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews));

        news.setTitle(updateData.title());
        news.setContent(updateData.content());
        news.setTags(updateData.tags());

        return NewsResponseDTO.from(newsRepository.save(news));
    }

    @Transactional
    public NewsResponseDTO deleteNews(long idNews) {
        News news = newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews));
        newsRepository.deleteById(idNews);
        return NewsResponseDTO.from(news);
    }

    public Page<NewsListDTO> getNews(Pageable pageable, String title) {
        Page<News> newsPage = (title != null) ? getNewsByTitle(pageable, title) : getNews(pageable);
        return newsPage.map(NewsListDTO::from);
    }

    private Page<News> getNews(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    private Page<News> getNewsByTitle(Pageable pageable, String title) {
        return newsRepository.findByTitleContainingIgnoreCase(pageable, title)
                .orElseThrow(() -> new NewsNotFoundByException(NotFoundBy.TITLE, title));
    }

    public Page<NewsListDTO> getNewsByTag(Pageable pageable, String tag) {
        Page<News> newsPage = newsRepository.findByTags(pageable, tag)
                .orElseThrow(() -> new NewsNotFoundByException(NotFoundBy.TAG, tag));
        return newsPage.map(NewsListDTO::from);
    }

    public NewsResponseDTO getCompleteNews(long idNews) {
        return NewsResponseDTO.from(newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews)));
    }

}
