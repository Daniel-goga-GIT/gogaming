import { GoogleLoginButton } from '../../shared/google-login-button/google-login-button';
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../core/services/auth';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { UserService } from '../../core/services/user';
import { UserDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  imports: [GoogleLoginButton, FormsModule, CommonModule, RouterLink, TranslateModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit, OnDestroy {

  email = '';
  password = '';
  error: string | null = null;
  isLoggedIn: boolean = false;
  user: UserDTO = new UserDTO();
  isSubmitting: boolean = false;
  private subscription: Subscription | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService
  ) {}

  ngOnInit() {
    this.subscription = this.auth.isLoggedIn().subscribe((loggedIn) => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.userService.fetchUser().subscribe((res: UserDTO) => {
          this.user.firstName = res.firstName;
          this.user.lastName = res.lastName;
          this.cdr.detectChanges();
        });
      }
    });
  }

  onSubmit(form: NgForm) {
    //Tener una variable que cuando le de al boton de submit, ponga false para que no se vuelva a pedir el código de dos factores
    //Así ponemos un boton como cargando

    if (this.isSubmitting) {
      return;
    }


    //Lógica para los campos obligatorios
    if (form.invalid) {
      Object.values(form.controls).forEach(control => {
        control.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;

    this.auth.login(this.email, this.password).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        //Guardar en el local storage que valga algo para en el header
        //Pongo false para que sepa que no ha introducido el código de dos factores
        localStorage.setItem("twofactor", "true");
        this.auth.setToken(res.token);

        this.router.navigate(['/twofactor']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.error = this.translate.instant('LOGIN.ERROR_INVALID');
      },
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
  //asd


}
