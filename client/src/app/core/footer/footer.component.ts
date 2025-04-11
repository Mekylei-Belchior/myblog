import { Component, OnInit } from '@angular/core';
import { FooterService } from 'src/app/shared/services/footer-service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css'],
})
export class FooterComponent implements OnInit {
  public showFooter$ = this.footerService.showFooter$;

  constructor(private footerService: FooterService) {}

  ngOnInit(): void {}

  /**
   * Scroll to the page top
   */
  public toTop(): void {
    window.scrollTo(0, 0);
  }
}
