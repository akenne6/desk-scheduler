import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Desk } from './desk.model';

@Injectable({ providedIn: 'root' })
export class DesksService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080';

  list(): Observable<Desk[]> {
    return this.http.get<Desk[]>(`${this.baseUrl}/api/desks`);
  }
}
