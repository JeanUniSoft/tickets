import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthRest, URL_API } from './auth-rest';

export interface Categoria {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface NuevaCategoria {
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class CategoriasRest {

  private http = inject(HttpClient);
  private auth = inject(AuthRest);

  private urlCategorias = URL_API + '/categories';

  // GET /categories  (CategoryController)
  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.urlCategorias, { headers: this.auth.cabeceras() });
  }

  // POST /categories  (CategoryController, solo ADMIN)
  crearCategoria(nueva: NuevaCategoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.urlCategorias, nueva, { headers: this.auth.cabeceras() });
  }
}
