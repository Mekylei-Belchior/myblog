import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

import { CommentResponse } from './../shared/interfaces/comment.interface';
import { FullNews } from './../shared/interfaces/fullNews.interface';
import { NewsPageable } from './../shared/interfaces/newsPageable.interface';
import { Post } from './../shared/interfaces/ResponseNews.interface';
import { News } from './news';
import { Comment } from './news-details/comment-form-dialog/comment';

// The standard API URL
const API = environment.apiURL;

@Injectable({
  providedIn: 'root',
})
export class NewsService {
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private httpClient: HttpClient) {}

  /**
   * Retrive all news
   * @returns An observable of all news
   */
  public getAllNews(): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(`${API}/news`);
  }

  /**
   * Retrive the news paged
   * @param page Target page
   * @param size How many items per page
   * @returns An observable with seraching result
   */
  public getPagedNews(page: number, size: number): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(
      `${API}/news?page=${page}&size=${size}`
    );
  }

  /**
   * Retrive a news by the identification
   * @param id The identification of the news
   * @returns An observable with all news information
   */
  public getNewsById(id: number): Observable<FullNews> {
    return this.httpClient.get<FullNews>(`${API}/news/${id}`);
  }

  /**
   * Request to API endpoint to create a news
   * @param news An object that represents a news
   * @returns An observable of news
   */
  public create(news: News): Observable<Post> {
    return this.httpClient.post<Post>(`${API}/news`, news, this.httpOptions);
  }

  /**
   * Request to API endpoint to create a new comment
   * @param comment An object that represent a commnet
   * @param id Identification of the news
   * @returns An observable of comment
   */
  public comment(comment: Comment, id: number): Observable<CommentResponse> {
    return this.httpClient.post<CommentResponse>(
      `${API}/news/${id}`,
      comment,
      this.httpOptions
    );
  }

  /**
   * Request to API endpoint to delete a news
   * @param id Identification of the news
   * @returns An observable of the news deleted
   */
  public delete(id: number): Observable<Post> {
    return this.httpClient.delete<Post>(`${API}/news/${id}`);
  }

  /**
   * Request to API endpoint to updates a news
   * @param news An object that represents a news
   * @param id Identification of the news
   * @returns An observable of the news updated
   */
  public update(news: News, id: number): Observable<Post> {
    return this.httpClient.put<Post>(
      `${API}/news/${id}`,
      news,
      this.httpOptions
    );
  }

  /**
   * Search news by tag name
   * @param tag The news topic tag
   * @returns An observable of all matches
   */
  public searchByTagName(tag: string): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(`${API}/news/topic?tag=${tag}`);
  }

  /**
   * Search news by news title
   * @param title The news title
   * @returns An observable of all matches
   */
  public searchByTitle(title: string): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(`${API}/news?title=${title}`);
  }

  /**
   * Gets paged search news
   * @param title The news title
   * @param page Target page
   * @param size How many items per page
   * @returns An observable of the searching result
   */
  public getPagedSearchingByTitle(
    title: string,
    page: number,
    size: number
  ): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(
      `${API}/news?title=${title}&page=${page}&size=${size}`
    );
  }
}
