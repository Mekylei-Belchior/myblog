import { Topic, Topics } from './../../../shared/interfaces/topic';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { News } from '../../news';

import { NewsService } from './../../news.service';

@Component({
  selector: 'app-news-form-dialog',
  templateUrl: './news-form-dialog.component.html',
  styleUrls: ['./news-form-dialog.component.css'],
})
export class NewsFormDialogComponent implements OnInit {
  public newsForm!: FormGroup;
  public topicList = Array<string>();

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<NewsFormDialogComponent>,
    private newsService: NewsService
  ) {}

  ngOnInit(): void {
    this.newsForm = this.formBuilder.group({
      title: ['', Validators.required],
      author: ['', Validators.required],
      content: ['', Validators.required],
    });
  }

  public addTopic(topic: string): void {
    this.topicList.push(topic);
  }

  public removeTopic(topic: string): void {
    this.topicList.splice(this.topicList.indexOf(topic), 1);
  }

  public createNews(): void {
    if (this.newsForm.valid) {
      const title = (this.newsForm.get('title')?.value as string) ?? '';
      const author = (this.newsForm.get('author')?.value as string) ?? '';
      const content = (this.newsForm.get('content')?.value as string) ?? '';
      const tags = this.topicList;

      const post: News = { title, author, content, tags };

      this.newsService.create(post).subscribe((response) => {
        console.log(response);
      });

      this.dialogRef.close();
      this.newsForm.reset();
      alert('Postagem criada com sucesso!');
      window.location.reload();
    }
  }

  public cancel(): void {
    this.dialogRef.close();
    this.newsForm.reset();
  }
}
