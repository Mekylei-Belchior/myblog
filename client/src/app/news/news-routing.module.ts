import { NewsDetailsComponent } from './news-details/news-details.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { NewsComponent } from './news.component';

const routes: Routes = [
  {
    path: '',
    component: NewsComponent,
  },
  {
    path: ':postId',
    component: NewsDetailsComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NewsRoutingModule {}
