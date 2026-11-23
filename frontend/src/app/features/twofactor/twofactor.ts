import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';
import { Router, RouterModule } from '@angular/router';
import { interval } from 'rxjs';

@Component({
  selector: 'app-twofactor',
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './twofactor.html',
  styleUrl: './twofactor.css'
})
export class Twofactor {
  code: string = '';
  error: string = '';
  timeout: any;
  count: number = 60;
  miIntervalo: any;

  constructor(private authService: AuthService, private router: Router) { }

    //Temporizador para 60 segundos
    ngOnInit() {
    // Iniciamos el contador
        this.miIntervalo = setInterval(() => {
        this.count--;

        // Si llega a 0, mandamos al login
        if (this.count <= 0) {
            this.detenerContador();
            this.router.navigate(['/login']);
        }
        }, 1000);
    }
    // Si cierra la pestaña o navega a otro lado manualmente
    ngOnDestroy() {
        this.detenerContador();   
    }
    //Funcion para detener el contador
    private detenerContador() {
        if (this.miIntervalo) {
        clearInterval(this.miIntervalo);
        }
    }
    
    onSubmit() {
        if (this.code == "") {
            this.error = 'Por favor, ingrese el código de autenticación';
            return;
        }
        this.error = '';
        this.authService.twofactor(this.code).subscribe({
            next: (res) => {
                localStorage.setItem("twofactor", "false");
                this.authService.setToken(res.token);
                this.router.navigate(['/']);
            },
            error: (err) => {
                this.error = 'Código de dos factores inválido.';
            },
        });
    }

    //

}
