package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.comment.CommentRequestDTO;
import br.com.mekylei.myblog.dtos.comment.CommentResponseDTO;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.repositories.CommentRepository;
import br.com.mekylei.myblog.repositories.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;


    public CommentService(CommentRepository commentRepository, NewsRepository newsRepository) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
    }


    @Transactional
    public CommentResponseDTO createComment(Long idNews, CommentRequestDTO data) {
        Comment comment = new Comment();
        comment.setNews(newsRepository.findById(idNews).orElseThrow(() -> new NewsNotFoundException(idNews)));
        comment.setAuthor(data.author());
        comment.setComment(data.comment());

        return CommentResponseDTO.from(commentRepository.save(comment));
    }

}
