import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { CommentResponse } from './../../shared/interfaces/comment.interface';
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
    private activateRoute: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.postId = this.activateRoute.snapshot.params.postId;
    this.news$ = this.newsService.getNewsById(this.postId);

    this.news$.subscribe((news) => {
      this.fullNews = news;
    });
  }

  public addCommentForm(): void {
    this.detailsService.callForm(CommentFormDialogComponent, this.fullNews);
  }

  public editNewsForm(): void {
    this.detailsService.callForm(EditFormDialogComponent, this.fullNews);
  }

  public deletePost(): void {
    if (confirm('Deseja prosseguir com a exclusão da postagem?')) {
      this.newsService.delete(this.postId).subscribe(
        () => {
          this.router.navigate(['/postagem']);
        },
        (error) => {
          console.log(error);
        }
      );
    }
  }

  public identify(index: number, comment: CommentResponse): number {
    return comment.id;
  }
}
