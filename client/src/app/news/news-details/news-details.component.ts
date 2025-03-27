import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ConfirmDialogComponent } from 'src/app/shared/components/confirm-dialog/confirm-dialog.component';

import { FullNews } from './../../shared/interfaces/fullNews.interface';
import { NewsService } from './../news.service';
import { CommentFormDialogComponent } from './comment-form-dialog/comment-form-dialog.component';
import { EditFormDialogComponent } from './edit-form-dialog/edit-form-dialog.component';
import { NewsDetailsService } from './news-details.service';
import { AuthService } from 'src/app/auth/auth.service';

@Component({
  selector: 'app-news-details',
  templateUrl: './news-details.component.html',
  styleUrls: ['./news-details.component.css'],
  providers: [NewsDetailsService],
})
export class NewsDetailsComponent implements OnInit {
  public expandedPanel = true;
  public postId!: number;
  public news$!: Observable<FullNews>;
  public comments$!: Observable<FullNews>;
  public isAuthenticated = false;

  constructor(
    private newsService: NewsService,
    private detailsService: NewsDetailsService,
    private activateRoute: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.postId = this.activateRoute.snapshot.params.postId;

    this.news$ = this.newsService.getNewsById(this.postId);
    this.comments$ = this.newsService.getNewsById(this.postId);
  }

  /**
   * Open the dialog window to add a new comment
   */
  public addCommentForm(): void {
    this.detailsService.comment(
      CommentFormDialogComponent,
      { id: this.postId },
      this
    );
  }

  /**
   * Open the dialog window to update the news informations
   */
  public editNewsForm(): void {
    this.newsService.getNewsById(this.postId).subscribe((response) => {
      const fullNews = response;
      this.detailsService.edit(EditFormDialogComponent, fullNews, this);
    });
  }

  /**
   * Call the service handler to delete a news
   */
  public deletePost(): void {
    this.detailsService.delete(ConfirmDialogComponent, this.postId);
  }

  /**
   * Update the comment component after insert a new comment
   */
  public refreshComments(): void {
    this.comments$ = this.newsService.getNewsById(this.postId);
  }

  /**
   * Update the news information
   */
  public refreshNews(): void {
    this.news$ = this.newsService.getNewsById(this.postId);
  }
}
