import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user';
import { UserCreateDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, CommonModule, RouterLink, TranslateModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
    //Inyecciones de dependencias para poder crear formulario
    private fb = inject(FormBuilder);
    private userService = inject(UserService);
    //El formGroup es el que almacena los datos
    userForm: FormGroup;
    isSubmitting = false;
    error: string | null = null;
    success: string | null = null;

    constructor( private router: Router) {
        // Inicializar el formulario con validaciones
        this.userForm = this.fb.group({
            firstName: ['', [Validators.required, Validators.maxLength(25)]],
            lastName: ['', [Validators.required, Validators.maxLength(25)]],
            email: ['', [Validators.required, Validators.email]],
            /* DESDE EL FRONTEND EN EL VALIDADOR METERMOS AQUI TAMBIEN: Asegura que la contraseña tenga al menos 8 caracteres, una letra mayúscula y un número */
            password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Z])(?=.*\d).{8,}$/)]]
        });
    }

    onSubmit(): void {
        if (this.userForm.invalid) {
            this.userForm.markAllAsTouched(); // Muestra los errores si el usuario intenta enviar
            return;
        }
        this.isSubmitting = true;
        this.userService.register(new UserCreateDTO(this.userForm.value.firstName,this.userForm.value.lastName,this.userForm.value.email,this.userForm.value.password)).subscribe({
            next: (response) => {
                this.userForm.reset(); // Limpiar formulario
                this.isSubmitting = false;
                this.router.navigate(['/register']);
                this.success = 'Cuenta creada con éxito. Ya puedes iniciar sesión.';
            },
            error: (err) => {
                this.isSubmitting = false;
                this.error = err.error; 
                this.success = null;
            }
        });
    }
}
