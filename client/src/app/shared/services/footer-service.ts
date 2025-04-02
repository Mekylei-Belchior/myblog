import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FooterService {
    private showFooter = new BehaviorSubject<boolean>(false);
    showFooter$ = this.showFooter.asObservable();

    constructor(
        private router: Router
    ) { }

    private setFooterVisibility(show: boolean): void {
        this.showFooter.next(show);
    }

    show(): void {
        this.setFooterVisibility(true);
    }

    hide(): void {
        this.setFooterVisibility(false);
    }
}