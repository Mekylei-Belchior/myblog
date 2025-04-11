import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { FullNews } from './../../../shared/interfaces/fullNews.interface';
import { AlertMessageService } from './../../../shared/services/alert-message.service';
import { NewsService } from './../../news.service';
import { DebugUtil } from 'src/app/shared/utils/debug.util';

@Component({
  selector: 'app-comment-form-dialog',
  templateUrl: './comment-form-dialog.component.html',
  styleUrls: ['./comment-form-dialog.component.css'],
})
export class CommentFormDialogComponent implements OnInit {
  public commentForm!: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CommentFormDialogComponent>,
    private newsService: NewsService,
    private alert: AlertMessageService,
    @Inject(MAT_DIALOG_DATA) public post: { data: FullNews },
    private debug: DebugUtil
  ) {}

  ngOnInit(): void {
    this.commentForm = this.formBuilder.group({
      author: ['', Validators.required],
      comment: ['', Validators.required],
    });
  }

  /**
   * Create a new comment
   */
  public createComment(): void {
    if (this.commentForm.valid) {
      // Call the service method that handler endpoint comment creation
      this.newsService
        .comment(this.commentForm.value, this.post.data.id)
        .subscribe(
          () => {},
          (error) => {
            this.alert.showError('Commentário não pode ser adicionado!');
            this.debug.error(error, 'CommentFormDialogComponent.createComment');
          }
        );

      this.dialogRef.close(true);
      this.commentForm.reset();
      this.alert.showSuccess('Commentário adicionado com sucesso!');
    }
  }

  public cancel(): void {
    this.dialogRef.close();
    this.commentForm.reset();
  }
}
