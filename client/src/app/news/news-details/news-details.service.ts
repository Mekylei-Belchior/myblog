import { ComponentType } from '@angular/cdk/portal';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AlertMessageService } from 'src/app/shared/services/alert-message.service';

import { NewsService } from './../news.service';
import { NewsDetailsComponent } from './news-details.component';

@Injectable({
  providedIn: 'root',
})
export class NewsDetailsService {
  constructor(
    private dialog: MatDialog,
    private newsService: NewsService,
    private router: Router,
    private alert: AlertMessageService
  ) {}

  /**
   * Open the Mat-Dialog
   * @param component The mat-dialog from component
   * @param data Any data to use in the component
   * @param mainComponent Father component view
   */
  public callForm(
    component: ComponentType<unknown>,
    data: object,
    mainComponent: NewsDetailsComponent
  ): void {
    const dialogRef = this.dialog.open(component, {
      data: { data },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        mainComponent.updateData();
      }
    });
  }

  /**
   * Delete the news if dialog result is true
   * @param component The mat-dialog from component
   * @param postId Identification of the news that will be removed
   */
  public delete(component: ComponentType<unknown>, postId: number): void {
    // Open the dialog window confirmation
    const dialogRef = this.dialog.open(component);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.newsService.delete(postId).subscribe(
          () => {
            this.router.navigate(['/postagem']);
            this.alert.showMessage('A postagem foi excluída com sucesso!');
          },
          (error) => {
            console.log(error);
          }
        );
      }
    });
  }
}
