import { Component, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute } from '@angular/router';
import { ListOfNews } from 'src/app/shared/interfaces/ResponseNews.interface';

import { NewsService } from '../news.service';

@Component({
  selector: 'app-news-search',
  templateUrl: './news-search.component.html',
  styleUrls: ['./news-search.component.css'],
})
export class NewsSearchComponent implements OnInit {
  public listOfNews!: ListOfNews;
  public length!: number;
  public pageSize!: number;
  public pageSizeOptions = [5, 10, 25, 50, 100];

  public title!: string;

  constructor(
    private newsService: NewsService,
    private activeRoute: ActivatedRoute
  ) {
    this.activeRoute.params.subscribe((param) => {
      this.title = param.title;
      this.paramChange(param.title);
    });
  }

  ngOnInit(): void {
    // Gets the parameter in the active route
    this.title = this.activeRoute.snapshot.params.title;
    this.search(this.title);
  }

  /**
   * Gets pagination event to change the visualization
   * @param event The pagination event
   */
  public pageEvent(event: PageEvent): void {
    this.newsService
      .getPagedSearchingByTitle(this.title, event.pageIndex, event.pageSize)
      .subscribe(
        (news) => {
          this.listOfNews = news.content;
          this.length = news.totalElements;
          this.pageSize = news.size;
        },
        (error) => {
          console.log(error);
        }
      );
  }

  /**
   * Call the searching method when active route changes
   * @param title The url title parameter
   */
  private paramChange(title: string): void {
    this.search(title);
  }

  /**
   * Call the service method to serching news by the title
   * @param title The title passed in the url parameter
   */
  private search(title: string): void {
    this.newsService.searchByTitle(title).subscribe(
      (news) => {
        this.listOfNews = news.content;
        this.length = news.totalElements;
        this.pageSize = news.size;
      },
      (error) => {
        console.log(error);
      }
    );
  }
}
