import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';

import { ListOfNews } from './../../shared/interfaces/ResponseNews.interface';
import { NewsService } from './../news.service';
import { NewsFormDialogComponent } from './news-form-dialog/news-form-dialog.component';
import { FooterService } from 'src/app/shared/services/footer-service';
import { ScreenService } from 'src/app/shared/services/screen-service';
import { Subscription } from 'rxjs';
import { NewsCommunicationService } from '../news-communication-service';
import { DebugUtil } from 'src/app/shared/utils/debug.util';

@Component({
  selector: 'app-news-list',
  templateUrl: './news-list.component.html',
  styleUrls: ['./news-list.component.css'],
})
export class NewsListComponent implements OnInit, OnDestroy {
  public listOfNews!: ListOfNews;
  public length!: number;

  public pageSize!: number;
  public pageSizeOptions = [5, 10, 25, 50, 100];

  private addNewsSubscription!: Subscription;

  constructor(
    private newsService: NewsService,
    private newsCommunicationService: NewsCommunicationService,
    private dialog: MatDialog,
    private footerService: FooterService,
    public screen: ScreenService,
    private debug: DebugUtil
  ) { }

  ngOnInit(): void {
    this.footerService.hide();
    this.getNews();

    this.addNewsSubscription = this.newsCommunicationService.addNewsTriggered$.subscribe(() => {
      this.addNews();
    });
  }

  ngOnDestroy(): void {
    this.addNewsSubscription.unsubscribe();
  }

  /**
   * Open the dialog window to add a new news
   */
  public addNews(): void {
    const dialogRef = this.dialog.open(NewsFormDialogComponent);

    dialogRef.afterClosed().subscribe((news) => {
      if (news) {
        // Update the page with brand new information after close the dialog window
        this.listOfNews = news._embedded?.newsList || [];
        this.length = news.page?.totalElements || 0;
        this.pageSize = news.page?.size || 0;
      }
    });
  }

  /**
   * Call service method to get all the news
   */
  private getNews(): void {
    this.newsService.getAllNews().subscribe(
      (news) => {
        this.listOfNews = news._embedded?.newsList || [];
        this.length = news.page?.totalElements || 0;
        this.pageSize = news.page?.size || 0;
      },
      (error) => {
        this.debug.error(error, 'NewsListComponent.getNews', {
          message: 'As postagem não puderam ser obtidas',
        });
      }
    );
  }

  /**
   * Gets pagination event to change the visualization
   * @param event The pagination event
   */
  public pageEvent(event: PageEvent): void {
    this.newsService.getPagedNews(event.pageIndex, event.pageSize).subscribe(
      (news) => {
        this.listOfNews = news._embedded?.newsList || [];
        this.length = news.page?.totalElements || 0;
        this.pageSize = news.page?.size || 0;
      },
      (error) => {
        this.debug.error(error, 'NewsListComponent.pageEvent', {
          message: 'Paginação não realizada',
        });
      }
    );
  }
}
