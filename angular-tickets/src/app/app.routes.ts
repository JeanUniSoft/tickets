import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Tickets } from './tickets/tickets';
import { TicketNuevo } from './ticket-nuevo/ticket-nuevo';
import { TicketDetalle } from './ticket-detalle/ticket-detalle';
import { Categorias } from './categorias/categorias';
import { UsuarioNuevo } from './usuario-nuevo/usuario-nuevo';
import { NoEncontrado } from './no-encontrado/no-encontrado';

export const routes: Routes = [
  { path: '', redirectTo: '/tickets', pathMatch: 'full' },
  {
    path: 'login',
    component: Login,
    title: 'Iniciar sesión',
  },
  {
    path: 'tickets',
    component: Tickets,
    title: 'Tickets',
  },
  {
    path: 'tickets/nuevo',
    component: TicketNuevo,
    title: 'Nuevo ticket',
  },
  {
    path: 'tickets/:id',
    component: TicketDetalle,
    title: 'Detalle del ticket',
  },
  {
    path: 'categorias',
    component: Categorias,
    title: 'Categorías',
  },
  {
    path: 'usuarios/nuevo',
    component: UsuarioNuevo,
    title: 'Nuevo usuario',
  },
  {
    path: '**',
    component: NoEncontrado,
    title: 'Página no encontrada',
  },
];
