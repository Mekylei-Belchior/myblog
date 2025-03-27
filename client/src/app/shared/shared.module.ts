import { AngularMaterialModule } from './modules/angular-material.module';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TagFormComponent } from './components/tag-form/tag-form.component';
import { TagFieldComponent } from './components/tag-field/tag-field.component';
import { SearchComponent } from './components/search/search.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { MatPaginatorModule } from '@angular/material/paginator';
import { PageNotFoundComponent } from './components/page-not-found/page-not-found.component';
import { AuthComponent } from './components/auth/auth.component';
import { LoginFormDialogComponent } from './components/auth/login-form-dialog/login-form-dialog.component';

@NgModule({
  imports: [
    CommonModule,
    AngularMaterialModule,
    FormsModule,
    ReactiveFormsModule,
    MatPaginatorModule,
  ],
  exports: [
    AngularMaterialModule,
    TagFormComponent,
    TagFieldComponent,
    SearchComponent,
    AuthComponent,
    FormsModule,
    ReactiveFormsModule,
    MatPaginatorModule,
  ],
  declarations: [TagFormComponent, TagFieldComponent, SearchComponent, ConfirmDialogComponent, PageNotFoundComponent, AuthComponent, LoginFormDialogComponent],
})
export class SharedModule { }
