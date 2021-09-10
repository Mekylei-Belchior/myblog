import { AngularMaterialModule } from './modules/angular-material.module';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TagFormComponent } from './components/tag-form/tag-form.component';
import { TagFieldComponent } from './components/tag-field/tag-field.component';

@NgModule({
  imports: [CommonModule, AngularMaterialModule],
  exports: [AngularMaterialModule, TagFormComponent, TagFieldComponent],
  declarations: [TagFormComponent, TagFieldComponent],
})
export class SharedModule {}
