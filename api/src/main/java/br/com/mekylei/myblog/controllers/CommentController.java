package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.dtos.CommentDto;
import br.com.mekylei.myblog.dtos.FullCommentDto;
import br.com.mekylei.myblog.models.Comment;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.CommentRepository;
import br.com.mekylei.myblog.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/news/{id}")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NewsRepository newsRepository;

    /**
     * Create a new comment
     *
     * @param commentDto An object that represents a comment
     * @return the all comment information
     */
    @PostMapping()
    @Transactional
    public ResponseEntity<FullCommentDto> create(@PathVariable Long id, @RequestBody @Valid CommentDto commentDto) {
        Comment comment = new Comment();
        Optional<News> news = this.newsRepository.findById(id);

        if (news.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        comment.setNews(news.get());
        BeanUtils.copyProperties(commentDto, comment);
        this.commentRepository.save(comment);

        return new ResponseEntity<>(new FullCommentDto(comment), HttpStatus.CREATED);
    }
}
