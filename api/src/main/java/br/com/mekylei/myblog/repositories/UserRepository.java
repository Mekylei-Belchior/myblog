package br.com.mekylei.myblog.repositories;

import br.com.mekylei.myblog.models.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ApiUser, Long> {

    Optional<ApiUser> findByEmail(String email);

}
