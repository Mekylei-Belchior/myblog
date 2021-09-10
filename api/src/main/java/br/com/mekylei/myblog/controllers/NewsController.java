package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.dtos.FullNewsDto;
import br.com.mekylei.myblog.dtos.NewsDto;
import br.com.mekylei.myblog.dtos.UpdateNewsDto;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.repositories.NewsRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    /**
     * Create a news
     *
     * @param newsDto An object that represents a news
     * @return the news created and http status (201)
     */
    @PostMapping
    @Transactional
    public ResponseEntity<News> create(@RequestBody @Valid NewsDto newsDto) {
        News news = new News();
        BeanUtils.copyProperties(newsDto, news);
        this.newsRepository.save(news);

        return new ResponseEntity<>(news, HttpStatus.CREATED);
    }

    /**
     * Update the news
     *
     * @param id identification of the news
     * @param newsData current data of the news
     * @return the news updated
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<News> update(@PathVariable Long id, @RequestBody @Valid UpdateNewsDto newsData) {
        Optional<News> nws = this.newsRepository.findById(id);

        if (nws.isPresent()) {
            News news = newsData.update(id, this.newsRepository);

            return ResponseEntity.ok(news);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Delete the news
     *
     * @param id identification of the news that will be deleted
     * @return if ok status 200. Otherwise status 404
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<News> delete(@PathVariable Long id) {
        Optional<News> news = this.newsRepository.findById(id);

        if (news.isPresent()) {
            this.newsRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get all news by tag name
     *
     * @param tag topic tag name
     * @param pageable pagination
     * @return all news paged
     */
    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public Page<News> getNewsByTag(@RequestParam( value = "tag", required = false) String tag,
                              @PageableDefault(
                                      page = 0,
                                      size = 5,
                                      sort = "id",
                                      direction = Sort.Direction.DESC) Pageable pageable) {
        System.out.println(tag);
            return this.newsRepository.findByTags(pageable, tag);
    }

    /**
     * Get news with all information
     *
     * @param id identification of the news
     * @return An object with all information about the news or status 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<FullNewsDto> getCompleteNews(@PathVariable Long id) {
        Optional<News> news = this.newsRepository.findById(id);

        if (news.isPresent()) {
            return ResponseEntity.ok(new FullNewsDto(news.get()));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get all news or a specific news
     *
     * @param title when informed, gets the news by the title
     * @param pageable pagination
     * @return all news paged or a specific news
     */
    @GetMapping()
    public Page<News> getNews(@RequestParam(required = false) String title,
                              @PageableDefault(
                                      page = 0,
                                      size = 10,
                                      sort = "id",
                                      direction = Sort.Direction.DESC) Pageable pageable) {

        if (title != null) {
            /* When title parameter has passed */
            return this.newsRepository.findByTitle(pageable, title);
        }

        return this.newsRepository.findAll(pageable);
    }
}
