import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css'],
})
export class FooterComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}

  /**
   * Scroll to the page top
   */
  public toTop(): void {
    window.scrollTo(0, 0);
  }
}
