package br.com.mekylei.myblog.repositories;

import br.com.mekylei.myblog.models.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {

    Page<News> findByTitle(Pageable pageable, String title);

    Page<News> findByTags(Pageable pageable, String tagName);
}
