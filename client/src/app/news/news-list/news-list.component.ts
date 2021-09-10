import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

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
    this.newsService.getAllNews().subscribe((news) => {
      this.listOfNews = news.content;
      this.length = news.totalElements;
      this.pageSize = news.size;
    });
  }

  public addNews(): void {
    const dialogRef = this.dialog.open(NewsFormDialogComponent);

    dialogRef.afterClosed().subscribe(() => {
      console.log('Formulário de nova postagem fechado!');
    });
  }
}
