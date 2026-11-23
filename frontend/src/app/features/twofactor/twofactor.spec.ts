import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Twofactor } from './twofactor';

describe('Twofactor', () => {
  let component: Twofactor;
  let fixture: ComponentFixture<Twofactor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Twofactor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Twofactor);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
