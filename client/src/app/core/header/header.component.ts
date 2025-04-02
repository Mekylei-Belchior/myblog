import { Component, ViewChild, OnInit } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { Router } from '@angular/router';
import { NewsCommunicationService } from 'src/app/news/news-communication-service';
import { ScreenService } from 'src/app/shared/services/screen-service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit {
  @ViewChild('sidenav') sidenav!: MatSidenav;
  public isMenuOpen = false;

  constructor(
    private router: Router,
    private newsCommunicationService: NewsCommunicationService,
    public screen: ScreenService,
  ) { }

  ngOnInit(): void { }

  /**
   * Open the dialog window to add a new news
   */
  addNews(): void {
    this.newsCommunicationService.triggerAddNews();
  }

  /**
   * Control menu sider (open or close)
   */
  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  /**
   * Navigate to the HOME page
   */
  public goHome(): void {
    this.router.navigate(['/']);
  }

  /**
   * Navigate to the POSTAGEM page
   */
  public goPostagem(): void {
    this.router.navigate(['postagem']);
  }
}
