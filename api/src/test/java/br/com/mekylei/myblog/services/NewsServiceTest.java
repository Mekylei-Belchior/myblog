package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.news.NewsListDTO;
import br.com.mekylei.myblog.dtos.news.NewsRequestDTO;
import br.com.mekylei.myblog.dtos.news.NewsResponseDTO;
import br.com.mekylei.myblog.exceptions.NewsNotFoundByException;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    private News news;
    private NewsRequestDTO requestDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        news = new News("Title", "Author", "Content", List.of("tag1"));
        news.setId(1L);
        requestDTO = new NewsRequestDTO("Title", "Author", "Content", List.of("tag1"));
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createNewsWhenValidDataShouldReturnNewsResponseDTO() {
        when(newsRepository.save(any(News.class))).thenReturn(news);

        NewsResponseDTO result = newsService.createNews(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.author()).isEqualTo("Author");
        verify(newsRepository).save(any(News.class));
    }

    @Test
    void updateNewsWhenNewsExistsShouldReturnUpdatedNewsResponseDTO() {
        NewsRequestDTO updateRequest = new NewsRequestDTO("Updated Title", "Author", "Updated Content", List.of("tag2"));
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsRepository.save(any(News.class))).thenAnswer(inv -> inv.getArgument(0));

        NewsResponseDTO result = newsService.updateNews(1L, updateRequest);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.content()).isEqualTo("Updated Content");
        verify(newsRepository).save(news);
    }

    @Test
    void updateNewsWhenNewsNotFoundShouldThrowNewsNotFoundException() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.updateNews(99L, requestDTO))
                .isInstanceOf(NewsNotFoundException.class);
    }

    @Test
    void deleteNewsWhenNewsExistsShouldReturnNewsResponseDTO() {
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        NewsResponseDTO result = newsService.deleteNews(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(newsRepository).deleteById(1L);
    }

    @Test
    void deleteNewsWhenNewsNotFoundShouldThrowNewsNotFoundException() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.deleteNews(99L))
                .isInstanceOf(NewsNotFoundException.class);
    }

    @Test
    void getNewsWhenNullTitleShouldReturnPageOfNewsListDTO() {
        Page<News> newsPage = new PageImpl<>(List.of(news));
        when(newsRepository.findAll(pageable)).thenReturn(newsPage);

        Page<NewsListDTO> result = newsService.getNews(pageable, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Title");
        verify(newsRepository).findAll(pageable);
    }

    @Test
    void getNewsWhenTitleProvidedShouldReturnFilteredPage() {
        Page<News> newsPage = new PageImpl<>(List.of(news));
        when(newsRepository.findByTitleContainingIgnoreCase(pageable, "Title"))
                .thenReturn(Optional.of(newsPage));

        Page<NewsListDTO> result = newsService.getNews(pageable, "Title");

        assertThat(result.getContent()).hasSize(1);
        verify(newsRepository).findByTitleContainingIgnoreCase(pageable, "Title");
    }

    @Test
    void getNewsWhenTitleNotFoundShouldThrowNewsNotFoundByException() {
        when(newsRepository.findByTitleContainingIgnoreCase(pageable, "unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getNews(pageable, "unknown"))
                .isInstanceOf(NewsNotFoundByException.class)
                .hasMessageContaining("title");
    }

    @Test
    void getNewsByTagWhenTagFoundShouldReturnPage() {
        Page<News> newsPage = new PageImpl<>(List.of(news));
        when(newsRepository.findByTags(pageable, "tag1")).thenReturn(Optional.of(newsPage));

        Page<NewsListDTO> result = newsService.getNewsByTag(pageable, "tag1");

        assertThat(result.getContent()).hasSize(1);
        verify(newsRepository).findByTags(pageable, "tag1");
    }

    @Test
    void getNewsByTagWhenTagNotFoundShouldThrowNewsNotFoundByException() {
        when(newsRepository.findByTags(pageable, "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getNewsByTag(pageable, "unknown"))
                .isInstanceOf(NewsNotFoundByException.class)
                .hasMessageContaining("tag");
    }

    @Test
    void getCompleteNewsWhenNewsExistsShouldReturnNewsResponseDTO() {
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        NewsResponseDTO result = newsService.getCompleteNews(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getCompleteNewsWhenNewsNotFoundShouldThrowNewsNotFoundException() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getCompleteNews(99L))
                .isInstanceOf(NewsNotFoundException.class);
    }
}
