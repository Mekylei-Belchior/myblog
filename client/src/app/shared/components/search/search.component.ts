import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
})
export class SearchComponent implements OnInit {
  public searchForm!: FormGroup;

  constructor(private formBuilder: FormBuilder, private router: Router) {}

  ngOnInit(): void {
    this.createSearch();
  }

  /**
   * Submit url search
   */
  public onSubmit(): void {
    const title: string = this.searchForm.get('value')?.value;

    if (title) {
      this.router.navigate([`postagem/busca/${title}`]);
    }

    this.searchForm.reset();
  }

  /**
   * Creates a new form group
   */
  private createSearch(): void {
    this.searchForm = this.formBuilder.group({
      value: [''],
    });
  }
}
