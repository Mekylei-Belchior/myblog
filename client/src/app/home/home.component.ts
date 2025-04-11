import { Component, OnInit } from '@angular/core';
import { FooterService } from '../shared/services/footer-service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  constructor(private footerService: FooterService) {}

  ngOnInit(): void {
    this.footerService.show();
  }
}
