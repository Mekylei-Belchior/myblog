import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ConfirmDialogComponent } from 'src/app/shared/components/confirm-dialog/confirm-dialog.component';

import { FullNews } from './../../shared/interfaces/fullNews.interface';
import { NewsService } from './../news.service';
import { CommentFormDialogComponent } from './comment-form-dialog/comment-form-dialog.component';
import { EditFormDialogComponent } from './edit-form-dialog/edit-form-dialog.component';
import { NewsDetailsService } from './news-details.service';

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
  public fullNews!: FullNews;

  constructor(
    private newsService: NewsService,
    private detailsService: NewsDetailsService,
    private activateRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.postId = this.activateRoute.snapshot.params.postId;
    this.news$ = this.newsService.getNewsById(this.postId);

    this.news$.subscribe((news) => {
      this.fullNews = news;
    });
  }

  /**
   * Open the dialog window to add a new comment
   */
  public addCommentForm(): void {
    this.detailsService.callForm(
      CommentFormDialogComponent,
      this.fullNews,
      this
    );
  }

  /**
   * Open the dialog window to update the news informations
   */
  public editNewsForm(): void {
    this.detailsService.callForm(EditFormDialogComponent, this.fullNews, this);
  }

  /**
   * Call the service handler to delete a news
   */
  public deletePost(): void {
    this.detailsService.delete(ConfirmDialogComponent, this.postId);
  }

  /**
   * Update the page information after edit the news or insert a new comment
   */
  public updateData(): void {
    this.news$ = this.newsService.getNewsById(this.postId);

    this.news$.subscribe((news) => {
      this.fullNews = news;
    });
  }
}
