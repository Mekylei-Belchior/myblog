import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from './../shared/shared.module';
import { CommentFormDialogComponent } from './news-details/comment-form-dialog/comment-form-dialog.component';
import { NewsDetailsComponent } from './news-details/news-details.component';
import { NewsFormDialogComponent } from './news-list/news-form-dialog/news-form-dialog.component';
import { NewsListComponent } from './news-list/news-list.component';
import { NewsRoutingModule } from './news-routing.module';
import { NewsComponent } from './news.component';
import { NewsService } from './news.service';
import { EditFormDialogComponent } from './news-details/edit-form-dialog/edit-form-dialog.component';
import { NewsSearchComponent } from './news-search/news-search.component';

@NgModule({
  declarations: [
    NewsComponent,
    NewsListComponent,
    NewsFormDialogComponent,
    NewsDetailsComponent,
    CommentFormDialogComponent,
    EditFormDialogComponent,
    NewsSearchComponent,
  ],
  imports: [CommonModule, NewsRoutingModule, ReactiveFormsModule, SharedModule],
  exports: [NewsListComponent, NewsComponent],
  providers: [NewsService],
})
export class NewsModule {}
