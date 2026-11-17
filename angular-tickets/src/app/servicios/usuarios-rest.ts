import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthRest, URL_API } from './auth-rest';

export interface NuevoUsuario {
  username: string;
  password: string;
  fullName: string;
  email: string;
  role: string;
}

export interface Usuario {
  id: number;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root',
})
export class UsuariosRest {

  private http = inject(HttpClient);
  private auth = inject(AuthRest);

  private urlUsuarios = URL_API + '/users';

  // POST /users  (UserController, solo ADMIN)
  crearUsuario(nuevo: NuevoUsuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.urlUsuarios, nuevo, { headers: this.auth.cabeceras() });
  }
}
