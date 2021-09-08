package br.com.mekylei.myblog.repositories;

import br.com.mekylei.myblog.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
