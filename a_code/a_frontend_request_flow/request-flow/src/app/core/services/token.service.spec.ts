import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';

describe('TokenService', () => {
  let service: TokenService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
  });

  it('should save and recover a token', () => {
    service.saveToken('fake-token');

    expect(service.getToken()).toBe('fake-token');
  });

  it('should remove a token', () => {
    service.saveToken('fake-token');
    service.removeToken();

    expect(service.getToken()).toBeNull();
  });
});
