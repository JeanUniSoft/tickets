import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthRest } from './servicios/auth-rest';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  auth = inject(AuthRest);
  private router = inject(Router);

  onSalir() {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}
