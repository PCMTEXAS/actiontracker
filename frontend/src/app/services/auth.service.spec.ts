import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isAuthenticated should be false initially', () => {
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('loadCurrentUser should set currentUser on success', () => {
    const mockUser = { id: '1', email: 'a@digitalchalk.com', name: 'Alice', role: 'MEMBER', pictureUrl: null, dailyDigestEnabled: true };
    service.loadCurrentUser();
    const req = httpMock.expectOne('/api/users/me');
    req.flush(mockUser);
    expect(service.currentUser()).toEqual(mockUser as any);
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('loadCurrentUser should redirect to /login on error', () => {
    service.loadCurrentUser();
    const req = httpMock.expectOne('/api/users/me');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    expect(service.currentUser()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('getInitials should return first+last initials', () => {
    expect(service.getInitials('Alice Bob')).toBe('AB');
    expect(service.getInitials('Single')).toBe('S');
    expect(service.getInitials('')).toBe('?');
  });

  it('isOwner computed returns true for OWNER role', () => {
    service.currentUser.set({ id: '1', email: 'owner@digitalchalk.com', name: 'Owner', role: 'OWNER', pictureUrl: null, dailyDigestEnabled: true });
    expect(service.isOwner()).toBeTrue();
  });
});
