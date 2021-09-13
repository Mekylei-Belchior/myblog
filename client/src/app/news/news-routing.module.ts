import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { NewsDetailsComponent } from './news-details/news-details.component';
import { NewsListComponent } from './news-list/news-list.component';
import { NewsSearchComponent } from './news-search/news-search.component';

const routes: Routes = [
  {
    path: '',
    component: NewsListComponent,
  },
  {
    path: 'artigo/:postId',
    component: NewsDetailsComponent,
  },
  {
    path: 'busca/:title',
    component: NewsSearchComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NewsRoutingModule {}
