import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from 'src/app/auth/auth.service';
import { LoginFormDialogComponent } from './login-form-dialog/login-form-dialog.component';
import { AlertMessageService } from '../../services/alert-message.service';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css'],
})
export class AuthComponent implements OnInit {
  public isAuthenticated = false;

  constructor(
    private authService: AuthService,
    private dialog: MatDialog,
    private alert: AlertMessageService
  ) {}

  ngOnInit(): void {
    this.authService.isAuthenticated$.subscribe((authStatus) => {
      this.isAuthenticated = authStatus;
    });
  }

  login(): void {
    this.dialog.open(LoginFormDialogComponent);
  }

  logout(): void {
    this.authService.logout().subscribe({
      complete: () => {
        this.isAuthenticated = false;
        this.alert.showSuccess(
          'Revogação de credenciais realizada com sucesso!'
        );
      },
    });
  }
}
