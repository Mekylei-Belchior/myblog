package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.dtos.news.NewsListDTO;
import br.com.mekylei.myblog.dtos.news.NewsRequestDTO;
import br.com.mekylei.myblog.dtos.news.NewsResponseDTO;
import br.com.mekylei.myblog.services.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<NewsResponseDTO> create(@RequestBody @Valid NewsRequestDTO news) {
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
    public ResponseEntity<NewsResponseDTO> update(@PathVariable Long id, @RequestBody @Valid NewsRequestDTO news) {
        return ResponseEntity.ok(newsService.updateNews(id, news));
    }

    /**
     * Delete the news
     *
     * @param id identification of the news that will be deleted
     * @return if ok status 200. Otherwise, status 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> delete(@PathVariable Long id) {
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
    public ResponseEntity<Page<NewsListDTO>> getNewsByTag(@RequestParam(value = "tag", required = false) String tag,
                                                      @PageableDefault(
                                                              page = 0,
                                                              size = 5,
                                                              sort = "id",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(newsService.getNewsByTag(pageable, tag));
    }

    /**
     * Get news with all information
     *
     * @param id identification of the news
     * @return An object with all information about the news or status 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> getCompleteNews(@PathVariable Long id) {
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
    public ResponseEntity<Page<NewsListDTO>> getNews(@RequestParam(required = false) String title,
                                                 @PageableDefault(
                                                         page = 0,
                                                         size = 10,
                                                         sort = "id",
                                                         direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(newsService.getNews(pageable, title));
    }

}
