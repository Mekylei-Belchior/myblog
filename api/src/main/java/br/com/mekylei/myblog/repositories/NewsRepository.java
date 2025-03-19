package br.com.mekylei.myblog.repositories;

import br.com.mekylei.myblog.models.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<Page<News>> findByTitleContainingIgnoreCase(Pageable pageable, String title);

    Optional<Page<News>> findByTags(Pageable pageable, String tagName);

}
