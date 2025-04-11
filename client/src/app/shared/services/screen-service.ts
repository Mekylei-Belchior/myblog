import { Injectable } from '@angular/core';
import { fromEvent, Observable } from 'rxjs';
import {
  map,
  startWith,
  throttleTime,
  distinctUntilChanged,
} from 'rxjs/operators';

type ScreenSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
type Device = 'MOBILE' | 'DESKTOP';

@Injectable({
  providedIn: 'root',
})
export class ScreenService {
  // Breakpoints
  private breakpoints = {
    xs: 576,
    sm: 768,
    md: 992,
    lg: 1366,
    xl: 7680,
  };

  // Devices
  private deviceSize = {
    MOBILE: 768,
    DESKTOP: 992,
  };

  // Monitor continuous screen size changes
  screenSize$: Observable<ScreenSize>;

  constructor() {
    this.screenSize$ = fromEvent(window, 'resize').pipe(
      throttleTime(50),
      map(() => this.getCurrentSize()),
      startWith(this.getCurrentSize()),
      distinctUntilChanged()
    );
  }

  private getCurrentSize(): ScreenSize {
    const width = this.width;
    const { xs, sm, md, lg } = this.breakpoints;

    return width < xs
      ? 'xs'
      : width < sm
      ? 'sm'
      : width < md
      ? 'md'
      : width < lg
      ? 'lg'
      : 'xl';
  }

  setBreakpoints(customBreakpoints: Partial<Record<ScreenSize, number>>) {
    this.breakpoints = { ...this.breakpoints, ...customBreakpoints };
  }

  get width(): number {
    return window.innerWidth;
  }

  get height(): number {
    return window.innerHeight;
  }

  getCurrentBreakpoint(): number {
    const size = this.getCurrentSize();
    return this.breakpoints[size];
  }

  isScreenSize(size: ScreenSize): boolean {
    return this.width < this.breakpoints[size];
  }

  isSmallerThan(size: ScreenSize): boolean {
    return this.width < this.breakpoints[size];
  }

  isLargerThan(size: ScreenSize): boolean {
    return this.width > this.breakpoints[size];
  }

  isScreenDevice(device: Device): boolean {
    return this.width < this.deviceSize[device];
  }

  isMobile(): boolean {
    return this.width < this.deviceSize['DESKTOP'];
  }

  isDesktop(): boolean {
    return this.width > this.deviceSize['MOBILE'];
  }
}
