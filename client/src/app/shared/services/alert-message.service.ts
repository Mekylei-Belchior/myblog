import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class AlertMessageService {
  constructor(private snackBar: MatSnackBar) {}

  /**
   * Shows a neutral message
   * @param msg The message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  public showMessage(msg: string, duration: number = 3000): void {
    this.snackBar.open(msg, '✕', {
      duration,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['default-snackbar'],
    });
  }

  /**
   * Shows a success message with green background
   * @param msg The success message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  public showSuccess(msg: string, duration: number = 3000): void {
    this.snackBar.open(msg, '✓', {
      duration,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['success-snackbar'],
    });
  }

  /**
   * Shows an error message with red background
   * @param msg The error message to display
   * @param duration Duration in milliseconds (default: 5000)
   */
  public showError(msg: string, duration: number = 5000): void {
    this.snackBar.open(msg, '✕', {
      duration,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['error-snackbar'],
    });
  }
}
