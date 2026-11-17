import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NuevoTicket, TicketsRest } from '../servicios/tickets-rest';
import { Categoria, CategoriasRest } from '../servicios/categorias-rest';
import { mensajeError } from '../servicios/auth-rest';

// Formulario 3 (reactivo): crear ticket -> POST /tickets  (las categorías del combo vienen de GET /categories)
@Component({
  selector: 'app-ticket-nuevo',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-nuevo.html',
  styleUrl: './ticket-nuevo.css',
})
export class TicketNuevo implements OnInit {

  private ticketsService = inject(TicketsRest);
  private categoriasService = inject(CategoriasRest);
  private router = inject(Router);

  miForm!: FormGroup;
  categorias = signal<Categoria[]>([]);
  error = signal('');

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.miForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(150)]],
      description: ['', [Validators.required, Validators.maxLength(2000)]],
      priority: ['MEDIUM', Validators.required],
      categoryId: ['', Validators.required],
    });
    this.categoriasService.getCategorias().subscribe({
      next: (data) => this.categorias.set(data),
      error: (err) => this.error.set(mensajeError(err)),
    });
  }

  onSubmit() {
    console.log('Data:', this.miForm.value);
    const nuevo: NuevoTicket = {
      title: this.miForm.value.title,
      description: this.miForm.value.description,
      priority: this.miForm.value.priority,
      categoryId: Number(this.miForm.value.categoryId),
    };
    this.ticketsService.crearTicket(nuevo).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.router.navigate(['/tickets', data.id]);
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.error.set(mensajeError(err));
      },
    });
  }
}
