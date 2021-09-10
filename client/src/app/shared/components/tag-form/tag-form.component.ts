import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';

import { Topic, Topics } from '../../interfaces/topic';

@Component({
  selector: 'app-tag-form',
  templateUrl: './tag-form.component.html',
  styleUrls: ['./tag-form.component.css'],
})
export class TagFormComponent implements OnInit {
  public visible = true;
  public selectable = true;
  public removable = true;
  public addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  public topics: Topics = [];
  @Input() tags = Array<string>();

  @Output() newTopicEvent = new EventEmitter<string>();
  @Output() removeTopicEvent = new EventEmitter<string>();

  constructor() {}

  ngOnInit(): void {
    this.tags.forEach((tag) => {
      this.topics.push({ name: tag });
      this.newTopicEvent.emit(tag);
    });
  }

  public add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    // Add a new topic
    if ((value || '').trim()) {
      this.topics.push({ name: value.trim() });
      this.newTopicEvent.emit(value.trim());
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  public remove(topic: Topic): void {
    const index = this.topics.indexOf(topic);
    this.removeTopicEvent.emit(topic.name);

    if (index >= 0) {
      this.topics.splice(index, 1);
    }
  }
}
