package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.dtos.news.FullNewsDTO;
import br.com.mekylei.myblog.dtos.news.NewsDTO;
import br.com.mekylei.myblog.models.News;
import br.com.mekylei.myblog.services.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;


    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }


    /**
     * Create a news
     *
     * @param news An object that represents a news
     * @return the news created and http status (201)
     */
    @PostMapping
    public ResponseEntity<News> create(@RequestBody @Valid NewsDTO news) {
        return new ResponseEntity<>(newsService.createNews(news), HttpStatus.CREATED);
    }

    /**
     * Update the news
     *
     * @param id   identification of the news
     * @param news current data of the news
     * @return the news updated
     */
    @PutMapping("/{id}")
    public ResponseEntity<News> update(@PathVariable Long id, @RequestBody @Valid NewsDTO news) {
        return ResponseEntity.ok(newsService.updateNews(id, news));
    }

    /**
     * Delete the news
     *
     * @param id identification of the news that will be deleted
     * @return if ok status 200. Otherwise, status 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<News> delete(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.deleteNews(id));
    }

    /**
     * Get all news by tag name
     *
     * @param tag      topic tag name
     * @param pageable pagination
     * @return all news paged
     */
    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public PagedModel<EntityModel<News>> getNewsByTag(@RequestParam(value = "tag", required = false) String tag,
                                                      @PageableDefault(
                                                              page = 0,
                                                              size = 5,
                                                              sort = "id",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return newsService.getNewsByTag(pageable, tag);
    }

    /**
     * Get news with all information
     *
     * @param id identification of the news
     * @return An object with all information about the news or status 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<FullNewsDTO> getCompleteNews(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getCompleteNews(id));
    }

    /**
     * Get all news or a specific news
     *
     * @param title    when informed, gets the news by the title
     * @param pageable pagination
     * @return all news paged or a specific news
     */
    @GetMapping()
    public PagedModel<EntityModel<News>> getNews(@RequestParam(required = false) String title,
                                                 @PageableDefault(
                                                         page = 0,
                                                         size = 10,
                                                         sort = "id",
                                                         direction = Sort.Direction.DESC) Pageable pageable) {
        return newsService.getNews(pageable, title);
    }

}
