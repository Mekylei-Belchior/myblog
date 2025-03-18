package br.com.mekylei.myblog.services;

import br.com.mekylei.myblog.dtos.comment.CommentDTO;
import br.com.mekylei.myblog.dtos.comment.FullCommentDTO;
import br.com.mekylei.myblog.exceptions.NewsNotFoundException;
import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.CommentRepository;
import br.com.mekylei.myblog.repositories.NewsRepository;
import org.springframework.beans.BeanUtils;
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
    public FullCommentDTO createComment(Long idNews, CommentDTO comment) {
        News news = newsRepository.findById(idNews).orElseThrow(() ->
                new NewsNotFoundException("News not found for ID: " + idNews));

        Comment fullComment = new Comment();
        fullComment.setNews(news);
        BeanUtils.copyProperties(comment, fullComment);
        commentRepository.save(fullComment);

        return new FullCommentDTO(fullComment);
    }

}
