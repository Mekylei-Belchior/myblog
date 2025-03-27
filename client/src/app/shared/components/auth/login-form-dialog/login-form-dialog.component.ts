import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from 'src/app/auth/auth.service';
import { AlertMessageService } from 'src/app/shared/services/alert-message.service';
import { Credentials } from 'src/app/shared/components/auth/credentials';

@Component({
  selector: 'app-login-form-dialog',
  templateUrl: './login-form-dialog.component.html',
  styleUrls: ['./login-form-dialog.component.css']
})
export class LoginFormDialogComponent implements OnInit {
  public loginForm!: FormGroup;
  public hidePassword = true;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<LoginFormDialogComponent>,
    private authService: AuthService,
    private alert: AlertMessageService
  ) { }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  /**
   * Submit login
   */
  public login(): void {
    if (this.loginForm.invalid) {
      this.markFormAsTouched();
      return;
    }

    // Gets form fields values
    const credentials: Credentials = this.loginForm.value;

    // Call the service method that handler login endpoint
    this.authService.login(credentials).subscribe(() => {
      this.dialogRef.close(true);
      this.alert.showMessage('Autenticação realizada com sucesso!');
    },
      (error) => {
        this.handleLoginError(error);
      }
    );
  }

  /**
   * Mark as touched when validation has not passed
   */
  private markFormAsTouched(): void {
    Object.values(this.loginForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  /**
   * Handler authentication errors
   * @param error the error in the authentication
   */
  private handleLoginError(error: any): void {
    this.alert.showMessage('E-mail ou senha incorretos');
  }

  /**
   * Close the dialog window
   */
  public cancel(): void {
    this.dialogRef.close(false);
  }

}
