import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css'],
})
export class ConfirmDialogComponent implements OnInit {
  public title = 'Confirmação';
  public message = 'Deseja continuar?';

  constructor(private dialogRef: MatDialogRef<ConfirmDialogComponent>) {}

  ngOnInit(): void {}

  /**
   * Submit SIM button
   */
  public confirm(): void {
    this.dialogRef.close(true);
  }

  /**
   * Submit NÂO button
   */
  public cancel(): void {
    this.dialogRef.close();
  }
}
