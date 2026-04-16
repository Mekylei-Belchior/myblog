package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.dtos.comment.CommentRequestDTO;
import br.com.mekylei.myblog.dtos.comment.CommentResponseDTO;
import br.com.mekylei.myblog.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{id}")
public class CommentController {

    private final CommentService commentService;


    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    /**
     * Create a news comment
     *
     * @param id      The ID of the news article
     * @param comment An object that represents a comment
     * @return the hole comment information
     */
    @PostMapping()
    public ResponseEntity<CommentResponseDTO> create(@PathVariable Long id, @RequestBody @Valid CommentRequestDTO comment) {
        return new ResponseEntity<>(commentService.createComment(id, comment), HttpStatus.CREATED);
    }

}
