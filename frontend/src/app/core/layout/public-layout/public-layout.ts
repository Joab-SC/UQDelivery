import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Logo } from '../logo/logo';

/**
 * Marco de las pantallas sin sesión (login, registro, recuperar y restablecer contraseña).
 * En escritorio: panel oscuro con la marca a la izquierda y el formulario a la derecha.
 * En celular: una franja oscura con el logo arriba y el formulario debajo.
 * La página de cada ruta se dibuja dentro de <router-outlet />.
 */
@Component({
  imports: [RouterOutlet, Logo],
  selector: 'app-public-layout',
  styleUrl: './public-layout.css',
  templateUrl: './public-layout.html',
})
export class PublicLayout {}
