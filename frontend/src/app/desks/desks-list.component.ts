import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Desk } from './desk.model';
import { DesksService } from './desks.service';

@Component({
  selector: 'app-desks-list',
  standalone: true,
  imports: [MatCardModule, MatChipsModule, MatProgressBarModule, MatToolbarModule],
  templateUrl: './desks-list.component.html',
  styleUrl: './desks-list.component.scss',
})
export class DesksListComponent implements OnInit {
  private readonly desksService = inject(DesksService);

  readonly desks = signal<Desk[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.desksService.list().subscribe({
      next: (desks) => {
        this.desks.set(desks);
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message ?? 'Unknown error');
        this.loading.set(false);
      },
    });
  }
}
