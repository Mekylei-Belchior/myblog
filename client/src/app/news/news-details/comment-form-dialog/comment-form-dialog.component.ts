import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { FullNews } from './../../../shared/interfaces/fullNews.interface';
import { NewsService } from './../../news.service';

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
    @Inject(MAT_DIALOG_DATA) public post: { data: FullNews }
  ) {}

  ngOnInit(): void {
    this.commentForm = this.formBuilder.group({
      author: ['', Validators.required],
      comment: ['', Validators.required],
    });
  }

  public createComment(): void {
    if (this.commentForm.valid) {
      this.newsService
        .comment(this.commentForm.value, this.post.data.id)
        .subscribe((response) => {
          console.table(response);
        });

      this.dialogRef.close();
      this.commentForm.reset();
      alert('Commentário criado com sucesso!');
      // window.location.reload();
    }
  }

  public cancel(): void {
    this.dialogRef.close();
    this.commentForm.reset();
  }
}
