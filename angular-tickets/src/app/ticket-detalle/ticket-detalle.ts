import { Component, inject, OnInit, signal } from '@angular/core';
import { NgForm, FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Comentario, Ticket, TicketsRest } from '../servicios/tickets-rest';
import { mensajeError } from '../servicios/auth-rest';

// Formulario 4 (de plantilla): cambiar estado -> PUT /tickets/{id}/status
// Formulario 5 (reactivo):     comentar       -> POST /tickets/{id}/comments
@Component({
  selector: 'app-ticket-detalle',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ticket-detalle.html',
  styleUrl: './ticket-detalle.css',
})
export class TicketDetalle implements OnInit {

  private ticketsService = inject(TicketsRest);

  id = 0;
  ticket = signal<Ticket | null>(null);
  comentarios = signal<Comentario[]>([]);
  error = signal('');
  errorEstado = signal('');
  errorComentario = signal('');
  mensajeEstado = signal('');

  comentarioForm!: FormGroup;

  constructor(private route: ActivatedRoute, private fb: FormBuilder) {
    // Lee el parámetro :id de la ruta tickets/:id (Semana 12)
    this.route.params.subscribe(params => {
      this.id = Number(params['id']);
      this.cargarTicket();
      this.cargarComentarios();
    });
  }

  ngOnInit() {
    this.comentarioForm = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(1000)]],
    });
  }

  // GET /tickets/{id}
  cargarTicket() {
    this.ticketsService.getTicket(this.id).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.ticket.set(data);
        this.error.set('');
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.ticket.set(null);
        this.error.set(mensajeError(err));
      },
    });
  }

  // GET /tickets/{id}/comments
  cargarComentarios() {
    this.ticketsService.getComentarios(this.id).subscribe({
      next: (data) => this.comentarios.set(data),
      error: (err) => console.error('Error al llamar al servicio:', err),
    });
  }

  onCambiarEstado(form: NgForm) {
    console.log('Data del formulario:', form.value);
    const actual = this.ticket();
    if (!actual) {
      return;
    }
    // Se envía la versión que se leyó: si otro la cambió antes, el backend responde 409
    this.ticketsService.cambiarEstado(this.id, form.value.status, actual.version).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.ticket.set(data);
        this.errorEstado.set('');
        this.mensajeEstado.set('Estado actualizado a ' + data.status);
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.mensajeEstado.set('');
        this.errorEstado.set(mensajeError(err));
      },
    });
  }

  onComentar() {
    console.log('Data:', this.comentarioForm.value);
    this.ticketsService.crearComentario(this.id, this.comentarioForm.value.content).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.errorComentario.set('');
        this.comentarioForm.reset();
        this.cargarComentarios();
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.errorComentario.set(mensajeError(err));
      },
    });
  }
}
