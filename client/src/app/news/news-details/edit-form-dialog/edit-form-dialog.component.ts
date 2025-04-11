import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { News } from '../../news';
import { FullNews } from './../../../shared/interfaces/fullNews.interface';
import { AlertMessageService } from './../../../shared/services/alert-message.service';
import { NewsService } from './../../news.service';
import { DebugUtil } from 'src/app/shared/utils/debug.util';

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
    private alert: AlertMessageService,
    @Inject(MAT_DIALOG_DATA) public post: { data: FullNews },
    private debug: DebugUtil
  ) {}

  ngOnInit(): void {
    this.newsEditForm = this.formBuilder.group({
      title: [this.post.data.title, Validators.required],
      author: [this.post.data.author, Validators.required],
      content: [this.post.data.content, Validators.required],
    });

    this.tagsOnInit = this.post.data.tag;
  }

  /**
   * Add a topic in the list of topics
   * @param topic The name of the topic
   */
  public addTopic(topic: string): void {
    this.topicList.push(topic);
  }

  /**
   * Remove a topic form the list of topics
   */
  public removeTopic(topic: string): void {
    this.topicList.splice(this.topicList.indexOf(topic), 1);
  }

  /**
   * Edit the news information
   */
  public editNews(): void {
    if (this.newsEditForm.valid) {
      // Gets form fields values to create a News
      const title = (this.newsEditForm.get('title')?.value as string) ?? '';
      const author = (this.newsEditForm.get('author')?.value as string) ?? '';
      const content = (this.newsEditForm.get('content')?.value as string) ?? '';
      const tags = this.topicList;

      const news: News = { title, author, content, tags };

      // Call the service method that handler endpoint news updates
      this.newsService.update(news, this.post.data.id).subscribe(
        (error) => {
          this.alert.showMessage('A postagem não pode ser atualizada!');
          this.debug.error(error, 'EditFormDialogComponent.editNews', {
            message: 'A postagem não pode ser atualizada!'
          });
        }
      );

      this.dialogRef.close(true);
      this.newsEditForm.reset();
      this.alert.showMessage('A postagem foi atualizada com sucesso!');
    }
  }

  public cancel(): void {
    this.dialogRef.close();
    this.newsEditForm.reset();
  }
}
