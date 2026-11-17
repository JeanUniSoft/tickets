import { Component, inject, OnInit, signal } from '@angular/core';
import { NgForm, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { PaginaTickets, TicketsRest } from '../servicios/tickets-rest';
import { mensajeError } from '../servicios/auth-rest';

// Formulario 2 (de plantilla): filtro de tickets -> GET /tickets?status=&page=&size=
@Component({
  selector: 'app-tickets',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './tickets.html',
  styleUrl: './tickets.css',
})
export class Tickets implements OnInit {

  private ticketsService = inject(TicketsRest);
  private router = inject(Router);

  pagina = signal<PaginaTickets | null>(null);
  error = signal('');

  // Último filtro enviado: lo reutilizan los botones Anterior / Siguiente
  private status = '';
  private size = 5;

  ngOnInit() {
    this.cargar(0);
  }

  onBuscar(form: NgForm) {
    console.log('Data del formulario:', form.value);
    this.status = form.value.status;
    this.size = form.value.size;
    this.cargar(0);
  }

  cargar(page: number) {
    this.ticketsService.getTickets(this.status, page, this.size).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.pagina.set(data);
        this.error.set('');
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        if (err.status === 401) {
          this.router.navigate(['/login']);
        }
        this.error.set(mensajeError(err));
      },
    });
  }
}
