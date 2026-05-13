import { Component } from '@angular/core';
import { DesksListComponent } from './desks/desks-list.component';

@Component({
  selector: 'app-root',
  imports: [DesksListComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
