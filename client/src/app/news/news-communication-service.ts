import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NewsCommunicationService {
  private addNewsTrigger = new Subject<void>();
  addNewsTriggered$ = this.addNewsTrigger.asObservable();

  triggerAddNews() {
    this.addNewsTrigger.next();
  }
}
