import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { News } from '../../news';

import { FullNews } from './../../../shared/interfaces/fullNews.interface';
import { NewsService } from './../../news.service';

@Component({
  selector: 'app-edit-form-dialog',
  templateUrl: './edit-form-dialog.component.html',
  styleUrls: ['./edit-form-dialog.component.css'],
})
export class EditFormDialogComponent implements OnInit {
  public newsEditForm!: FormGroup;
  public tagsOnInit = Array<string>();
  public topicList = Array<string>();

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<EditFormDialogComponent>,
    private newsService: NewsService,
    @Inject(MAT_DIALOG_DATA) public post: { data: FullNews }
  ) {}

  ngOnInit(): void {
    this.newsEditForm = this.formBuilder.group({
      title: [this.post.data.title, Validators.required],
      author: [this.post.data.author, Validators.required],
      content: [this.post.data.content, Validators.required],
    });

    this.tagsOnInit = this.post.data.tag;
  }

  public addTopic(topic: string): void {
    this.topicList.push(topic);
  }

  public removeTopic(topic: string): void {
    this.topicList.splice(this.topicList.indexOf(topic), 1);
  }


  public editNews(): void {
    if (this.newsEditForm.valid) {

      const title = (this.newsEditForm.get('title')?.value as string) ?? '';
      const author = (this.newsEditForm.get('author')?.value as string) ?? '';
      const content = (this.newsEditForm.get('content')?.value as string) ?? '';
      const tags = this.topicList;

      const news: News = { title, author, content, tags };

      this.newsService
        .update(news, this.post.data.id)
        .subscribe(
          (response) => {
            console.table(response);
          },
          (erros) => {
            console.log(erros);
          }
        );

      this.dialogRef.close();
      this.newsEditForm.reset();
      window.location.reload();
      alert('A postagem foi atualizada com sucesso!');
    }
  }

  public cancel(): void {
    this.dialogRef.close();
    this.newsEditForm.reset();
  }
}
