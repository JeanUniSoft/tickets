import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthRest, URL_API } from './auth-rest';

export interface Ticket {
  id: number;
  title: string;
  description: string;
  status: string;
  priority: string;
  category: string;
  creator: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface PaginaTickets {
  content: Ticket[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface NuevoTicket {
  title: string;
  description: string;
  priority: string;
  categoryId: number;
}

export interface Comentario {
  id: number;
  content: string;
  author: string;
  ticketId: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class TicketsRest {

  private http = inject(HttpClient);
  private auth = inject(AuthRest);

  private urlTickets = URL_API + '/tickets';

  // GET /tickets?status=&page=&size=  (TicketController)
  getTickets(status: string, page: number, size: number): Observable<PaginaTickets> {
    let url = this.urlTickets + '?page=' + page + '&size=' + size;
    if (status) {
      url += '&status=' + status;
    }
    return this.http.get<PaginaTickets>(url, { headers: this.auth.cabeceras() });
  }

  // GET /tickets/{id}  (TicketController)
  getTicket(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(this.urlTickets + '/' + id, { headers: this.auth.cabeceras() });
  }

  // POST /tickets  (TicketController)
  crearTicket(nuevo: NuevoTicket): Observable<Ticket> {
    return this.http.post<Ticket>(this.urlTickets, nuevo, { headers: this.auth.cabeceras() });
  }

  // PUT /tickets/{id}/status  (TicketController, solo ADMIN)
  cambiarEstado(id: number, status: string, version: number): Observable<Ticket> {
    return this.http.put<Ticket>(this.urlTickets + '/' + id + '/status', { status: status, version: version },
      { headers: this.auth.cabeceras() });
  }

  // GET /tickets/{id}/comments  (CommentController)
  getComentarios(ticketId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(this.urlTickets + '/' + ticketId + '/comments',
      { headers: this.auth.cabeceras() });
  }

  // POST /tickets/{id}/comments  (CommentController)
  crearComentario(ticketId: number, content: string): Observable<Comentario> {
    return this.http.post<Comentario>(this.urlTickets + '/' + ticketId + '/comments', { content: content },
      { headers: this.auth.cabeceras() });
  }
}
