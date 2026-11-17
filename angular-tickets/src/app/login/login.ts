import { Component, inject, signal } from '@angular/core';
import { NgForm, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthRest, Credenciales, mensajeError } from '../servicios/auth-rest';

// Formulario 1 (de plantilla): iniciar sesión -> POST /auth/login
@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  private authService = inject(AuthRest);
  private router = inject(Router);

  error = signal('');

  onIngresar(form: NgForm) {
    console.log('Data del formulario:', form.value);
    const credenciales: Credenciales = {
      username: form.value.username,
      password: form.value.password,
    };
    this.authService.login(credenciales).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.authService.guardarSesion(credenciales.username, data.token);
        this.router.navigate(['/tickets']);
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.error.set(mensajeError(err));
      },
    });
  }
}
