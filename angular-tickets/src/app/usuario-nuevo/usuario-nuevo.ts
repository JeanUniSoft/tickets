import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NuevoUsuario, Usuario, UsuariosRest } from '../servicios/usuarios-rest';
import { mensajeError } from '../servicios/auth-rest';

// Formulario 7 (reactivo): crear usuario -> POST /users  (solo ADMIN)
@Component({
  selector: 'app-usuario-nuevo',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuario-nuevo.html',
  styleUrl: './usuario-nuevo.css',
})
export class UsuarioNuevo implements OnInit {

  private usuariosService = inject(UsuariosRest);

  miForm!: FormGroup;
  creado = signal<Usuario | null>(null);
  error = signal('');

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.miForm = this.fb.group({
      username: ['', [Validators.required, Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
      fullName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      role: ['USER', Validators.required],
    });
  }

  onSubmit() {
    const nuevo: NuevoUsuario = this.miForm.value;
    console.log('Data:', { ...nuevo, password: '******' });
    this.usuariosService.crearUsuario(nuevo).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.creado.set(data);
        this.error.set('');
        this.miForm.reset({ role: 'USER' });
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.creado.set(null);
        this.error.set(mensajeError(err));
      },
    });
  }
}
