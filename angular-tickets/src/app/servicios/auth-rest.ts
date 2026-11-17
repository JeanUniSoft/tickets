import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

// URL base del backend Spring Boot (la usan todos los servicios)
export const URL_API = 'http://localhost:8080/api/v1';

export interface Credenciales {
  username: string;
  password: string;
}

export interface TokenResponse {
  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthRest {

  private http = inject(HttpClient);

  // Usuario con sesión iniciada ('' si no hay). Es un signal para que el menú se actualice solo.
  usuario = signal<string>(localStorage.getItem('usuario') ?? '');

  // POST /auth/login  (AuthController)
  login(credenciales: Credenciales): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(URL_API + '/auth/login', credenciales);
  }

  guardarSesion(username: string, token: string) {
    localStorage.setItem('token', token);
    localStorage.setItem('usuario', username);
    this.usuario.set(username);
  }

  cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    this.usuario.set('');
  }

  // Cabecera que exige el backend en todas las operaciones protegidas: Authorization: Bearer <token>
  cabeceras(): HttpHeaders {
    const token = localStorage.getItem('token');
    return token ? new HttpHeaders({ Authorization: 'Bearer ' + token }) : new HttpHeaders();
  }
}

// Convierte el error HTTP del backend en un mensaje para mostrar en pantalla
export function mensajeError(err: HttpErrorResponse): string {
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor (¿está encendido el backend en el puerto 8080?)';
  }
  if (err.status === 401) {
    return err.error?.detail ?? 'Sesión no iniciada o vencida. Inicia sesión nuevamente.';
  }
  if (err.status === 403) {
    return 'No tienes permiso para esta operación (solo ADMIN).';
  }
  let mensaje = err.error?.detail ?? 'Error ' + err.status;
  const campos = err.error?.errors;
  if (campos) {
    mensaje += ': ' + Object.keys(campos).map((campo) => campo + ' ' + campos[campo]).join(', ');
  }
  return mensaje;
}
