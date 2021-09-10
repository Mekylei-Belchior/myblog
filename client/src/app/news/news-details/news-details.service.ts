import { ComponentType } from '@angular/cdk/portal';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

@Injectable({
  providedIn: 'root',
})
export class NewsDetailsService {
  constructor(private dialog: MatDialog) {}

  /**
   * Open the Mat-Dialog
   * @param component The component that has the mat-dialog from
   * @param data Any data to use in the component
   */
  public callForm(component: ComponentType<unknown>, data: object): void {
    const dialogRef = this.dialog.open(component, {
      data: {data},
    });

    dialogRef.afterClosed().subscribe(() => {
      console.log(`Formulário ${component.name} fechado!`);
    });
  }
}
