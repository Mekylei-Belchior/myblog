import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

import { News } from '../../news';
import { AlertMessageService } from './../../../shared/services/alert-message.service';
import { NewsService } from './../../news.service';
import { DebugUtil } from 'src/app/shared/utils/debug.util';

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
    private newsService: NewsService,
    private alert: AlertMessageService,
    private debug: DebugUtil
  ) {}

  ngOnInit(): void {
    this.newsForm = this.formBuilder.group({
      title: ['', Validators.required],
      author: ['', Validators.required],
      content: ['', Validators.required],
    });
  }

  /**
   * Add a new topic in the list of topic
   * @param topic A topic name
   */
  public addTopic(topic: string): void {
    this.topicList.push(topic);
  }

  /**
   * Remove a topic from the list
   * @param topic A topic name
   */
  public removeTopic(topic: string): void {
    this.topicList.splice(this.topicList.indexOf(topic), 1);
  }

  /**
   * Create a new news
   */
  public createNews(): void {
    if (this.newsForm.valid) {
      // Gets form fields values to create a News
      const title = (this.newsForm.get('title')?.value as string) ?? '';
      const author = (this.newsForm.get('author')?.value as string) ?? '';
      const content = (this.newsForm.get('content')?.value as string) ?? '';
      const tags = this.topicList;

      const post: News = { title, author, content, tags };

      // Call the service method that handler endpoint news creation
      this.newsService.create(post).subscribe(
        () => {
          this.newsService.getAllNews().subscribe((response) => {
            this.dialogRef.close(response);
            this.newsForm.reset();
            this.alert.showMessage('Nova postagem criada com sucesso!');
          });
        },
        (error) => {
          this.alert.showMessage('A nova postagem não pode ser criada!');
          this.debug.error(error, 'NewsFormDialogComponent.createNews', {
            message: 'A nova postagem não pode ser criada!',
          });
        }
      );
    }
  }

  /**
   * Close the dialog window
   */
  public cancel(): void {
    this.dialogRef.close();
    this.newsForm.reset();
  }
}
