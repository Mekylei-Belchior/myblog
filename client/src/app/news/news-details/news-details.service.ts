import { ComponentType } from '@angular/cdk/portal';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AlertMessageService } from 'src/app/shared/services/alert-message.service';

import { NewsService } from './../news.service';
import { NewsDetailsComponent } from './news-details.component';
import { DebugUtil } from 'src/app/shared/utils/debug.util';

@Injectable({
  providedIn: 'root',
})
export class NewsDetailsService {
  constructor(
    private dialog: MatDialog,
    private newsService: NewsService,
    private router: Router,
    private alert: AlertMessageService,
    private debug: DebugUtil
  ) {}

  /**
   * Open the Mat-Dialog
   * @param component The mat-dialog from component
   * @param data The news information
   * @param mainComponent Father component view
   */
  public edit(
    component: ComponentType<unknown>,
    data: object,
    mainComponent: NewsDetailsComponent
  ): void {
    const dialogRef = this.dialog.open(component, {
      data: { data },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        mainComponent.refreshNews();
      }
    });
  }

  /**
   * Open the Mat-Dialog
   * @param component The mat-dialog from component
   * @param data The news id
   * @param mainComponent Father component view
   */
  public comment(
    component: ComponentType<unknown>,
    data: object,
    mainComponent: NewsDetailsComponent
  ): void {
    const dialogRef = this.dialog.open(component, {
      data: { data },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        mainComponent.refreshComments();
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
            this.alert.showSuccess('A postagem foi excluída com sucesso!');
          },
          (error) => {
            this.alert.showError('A postagem não pode ser excluída!');
            this.debug.error(error, 'NewsDetailsService.delete');
          }
        );
      }
    });
  }
}
