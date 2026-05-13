import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DesksService } from './desks.service';
import { Desk } from './desk.model';

describe('DesksService', () => {
  let service: DesksService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DesksService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GET /api/desks returns the desk list', () => {
    const mockDesks: Desk[] = [
      {
        id: 1,
        name: 'Desk 101-A',
        type: 'STANDARD',
        occupiedBy: null,
        room: {
          id: 1,
          name: 'Room 101',
          floor: { id: 1, name: 'Floor 1' },
        },
      },
    ];

    let received: Desk[] | undefined;
    service.list().subscribe((d) => (received = d));

    const req = httpMock.expectOne('http://localhost:8080/api/desks');
    expect(req.request.method).toBe('GET');
    req.flush(mockDesks);

    expect(received).toEqual(mockDesks);
  });
});
