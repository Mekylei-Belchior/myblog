package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.comment.CommentRequestDTO;
import br.com.mekylei.myblog.dtos.comment.CommentResponseDTO;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.CommentRepository;
import br.com.mekylei.myblog.repositories.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private CommentService commentService;

    private News news;
    private CommentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        news = new News("Title", "Author", "Content", List.of("tag1"));
        news.setId(1L);
        requestDTO = new CommentRequestDTO("Commenter", "Great article!");
    }

    @Test
    void createCommentWhenNewsExistsShouldReturnCommentResponseDTO() {
        Comment savedComment = new Comment("Great article!", LocalDateTime.now(), "Commenter", news);
        savedComment.setId(1L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponseDTO result = commentService.createComment(1L, requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.comment()).isEqualTo("Great article!");
        assertThat(result.author()).isEqualTo("Commenter");
        assertThat(result.id()).isEqualTo(1L);
        verify(newsRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createCommentWhenNewsNotFoundShouldThrowNewsNotFoundException() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(99L, requestDTO))
                .isInstanceOf(NewsNotFoundException.class);

        verify(newsRepository).findById(99L);
    }
}
