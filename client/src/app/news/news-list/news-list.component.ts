import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';

import { ListOfNews } from './../../shared/interfaces/ResponseNews.interface';
import { NewsService } from './../news.service';
import { NewsFormDialogComponent } from './news-form-dialog/news-form-dialog.component';

@Component({
  selector: 'app-news-list',
  templateUrl: './news-list.component.html',
  styleUrls: ['./news-list.component.css'],
})
export class NewsListComponent implements OnInit {
  public listOfNews!: ListOfNews;
  public length!: number;

  public pageSize!: number;
  public pageSizeOptions = [5, 10, 25, 50, 100];

  constructor(private newsService: NewsService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.getNews();
  }

  /**
   * Open the dialog window to add a new news
   */
  public addNews(): void {
    const dialogRef = this.dialog.open(NewsFormDialogComponent);

    dialogRef.afterClosed().subscribe((news) => {
      if (news) {
        // Update the page with brand new information after close the dialog window
        this.listOfNews = news._embedded.newsList;
        this.length = news.page.totalElements;
        this.pageSize = news.page.size;
      }
    });
  }

  /**
   * Call service method to get all the news
   */
  private getNews(): void {
    this.newsService.getAllNews().subscribe(
      (news) => {
        this.listOfNews = news._embedded.newsList;
        this.length = news.page.totalElements;
        this.pageSize = news.page.size;
      },
      (error) => {
        console.log(error);
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
        this.listOfNews = news._embedded.newsList;
        this.length = news.page.totalElements;
        this.pageSize = news.page.size;
      },
      (error) => {
        console.log(error);
      }
    );
  }
}
