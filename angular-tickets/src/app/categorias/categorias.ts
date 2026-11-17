import { Component, inject, OnInit, signal } from '@angular/core';
import { NgForm, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Categoria, CategoriasRest, NuevaCategoria } from '../servicios/categorias-rest';
import { mensajeError } from '../servicios/auth-rest';

// Formulario 6 (de plantilla): crear categoría -> POST /categories  (la tabla usa GET /categories)
@Component({
  selector: 'app-categorias',
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrl: './categorias.css',
})
export class Categorias implements OnInit {

  private categoriasService = inject(CategoriasRest);

  categorias = signal<Categoria[]>([]);
  error = signal('');
  mensaje = signal('');

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.categoriasService.getCategorias().subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.categorias.set(data);
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.error.set(mensajeError(err));
      },
    });
  }

  onGuardar(form: NgForm) {
    console.log('Data del formulario:', form.value);
    const nueva: NuevaCategoria = {
      name: form.value.name,
      description: form.value.description,
    };
    this.categoriasService.crearCategoria(nueva).subscribe({
      next: (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.error.set('');
        this.mensaje.set('Categoría "' + data.name + '" creada');
        form.resetForm();
        this.cargar();
      },
      error: (err) => {
        console.error('Error al llamar al servicio:', err);
        this.mensaje.set('');
        this.error.set(mensajeError(err));
      },
    });
  }
}
