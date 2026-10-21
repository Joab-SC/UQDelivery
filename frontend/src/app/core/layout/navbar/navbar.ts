import { Component, computed, inject, output } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../auth/auth.service';
import { Rol } from '../../models/rol';
import { Logo } from '../logo/logo';

/** Nombre de cada rol para mostrar en pantalla. */
const NOMBRES_ROL: Record<Rol, string> = {
  ADMIN: 'Administrador',
  EMISOR: 'Emisor',
  REPARTIDOR: 'Repartidor',
};

/**
 * Barra superior del panel privado: botón del menú, logo, usuario (iniciales, nombre y rol)
 * y botón para cerrar sesión.
 */
@Component({
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, Logo],
  selector: 'app-navbar',
  styleUrl: './navbar.css',
  templateUrl: './navbar.html',
})
export class Navbar {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Avisa al layout que el usuario tocó el botón del menú. */
  readonly alternarMenu = output<void>();

  /** Primeras letras del nombre y del apellido, p. ej. "Laura Gómez" → "LG". */
  protected readonly iniciales = computed(() => {
    const palabras = (this.auth.usuario()?.nombre ?? '').trim().split(/\s+/);
    return palabras
      .slice(0, 2)
      .map((palabra) => palabra.charAt(0).toUpperCase())
      .join('');
  });

  protected readonly nombreRol = computed(() => {
    const rol = this.auth.rol();
    return rol ? NOMBRES_ROL[rol] : '';
  });

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}
