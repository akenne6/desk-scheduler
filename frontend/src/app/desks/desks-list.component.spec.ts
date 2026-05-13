import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { DesksListComponent } from './desks-list.component';
import { Desk } from './desk.model';

describe('DesksListComponent', () => {
  let fixture: ComponentFixture<DesksListComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DesksListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideAnimationsAsync()],
    });
    fixture = TestBed.createComponent(DesksListComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('renders fetched desks with room/floor and the type chip', () => {
    fixture.detectChanges();

    const seeded: Desk[] = [
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
    httpMock.expectOne('http://localhost:8080/api/desks').flush(seeded);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Desk 101-A');
    expect(text).toContain('Floor 1');
    expect(text).toContain('Room 101');
    expect(text).toContain('STANDARD');
    expect(text).toContain('Available');
  });

  it('shows the error message when the request fails', () => {
    fixture.detectChanges();

    httpMock
      .expectOne('http://localhost:8080/api/desks')
      .error(new ProgressEvent('Network error'), {
        status: 500,
        statusText: 'Server Error',
      });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Could not load desks');
  });
});
