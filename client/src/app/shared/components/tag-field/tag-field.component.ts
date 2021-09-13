import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-tag-field',
  templateUrl: './tag-field.component.html',
  styleUrls: ['./tag-field.component.css'],
})
export class TagFieldComponent implements OnInit {
  @Input() tags = Array<string>();

  constructor() {}

  ngOnInit(): void {}
}
