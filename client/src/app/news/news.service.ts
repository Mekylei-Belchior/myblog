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

const API = environment.apiURL;

@Injectable({
  providedIn: 'root',
})
export class NewsService {
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private httpClient: HttpClient) {}

  public getAllNews(): Observable<NewsPageable> {
    return this.httpClient.get<NewsPageable>(`${API}/news`);
  }

  public getNewsById(id: number): Observable<FullNews> {
    return this.httpClient.get<FullNews>(`${API}/news/${id}`);
  }

  /**
   * Creates a news
   * @param news An object that represents a news
   * @returns An observable of news
   */
  public create(news: News): Observable<Post> {
    return this.httpClient.post<Post>(`${API}/news`, news, this.httpOptions);
  }

  public comment(comment: Comment, id: number): Observable<CommentResponse> {
    return this.httpClient.post<CommentResponse>(
      `${API}/news/${id}`,
      comment,
      this.httpOptions
    );
  }

  public delete(id: number): Observable<Post> {
    return this.httpClient.delete<Post>(`${API}/news/${id}`);
  }

  public update(news: News, id: number): Observable<Post> {
    return this.httpClient.put<Post>(
      `${API}/news/${id}`,
      news,
      this.httpOptions
    );
  }
}
