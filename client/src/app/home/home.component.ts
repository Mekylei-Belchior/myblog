import { Component, OnInit } from '@angular/core';
import { FooterService } from '../shared/services/footer-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  constructor(
    private footerService: FooterService,
  private router: Router) {}

  ngOnInit(): void {
    this.footerService.show();
  }

  /**
   * Navigate to the POSTAGEM page
   */
  public goPostagem(): void {
    this.router.navigate(['postagem']);
  }
  
}
