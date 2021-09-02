package br.com.mekylei.myblog.repositories;

import br.com.mekylei.myblog.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
