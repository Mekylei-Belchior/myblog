import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}

  public goHome(): void {
    this.router.navigate(['/']);
  }

  public goPostagem(): void {
    this.router.navigate(['postagem']);
  }
}
