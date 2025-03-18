package br.com.mekylei.myblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.mekylei.myblog")
@EnableJpaRepositories(basePackages = "br.com.mekylei.myblog.repositories")
@EntityScan(basePackages = "br.com.mekylei.myblog.models")
public class MyblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyblogApplication.class, args);
    }

}
