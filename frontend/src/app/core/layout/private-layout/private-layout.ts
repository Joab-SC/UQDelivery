import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { map } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { Rol } from '../../models/rol';
import { Navbar } from '../navbar/navbar';

interface OpcionMenu {
  etiqueta: string;
  icono: string;
  ruta: string;
  roles: Rol[];
}

/**
 * Opciones del menú lateral. Cada una se muestra solo a los roles indicados.
 * Cuando una historia crea una pantalla nueva que deba ir en el menú, agrega aquí su opción.
 */
const OPCIONES: OpcionMenu[] = [
  { etiqueta: 'Inicio', icono: 'home', ruta: '/app/inicio', roles: ['ADMIN', 'EMISOR', 'REPARTIDOR'] },
  { etiqueta: 'Usuarios', icono: 'group', ruta: '/app/usuarios', roles: ['ADMIN'] },
  { etiqueta: 'Nuevo envío', icono: 'local_shipping', ruta: '/app/envios/nuevo', roles: ['EMISOR'] },
];

/**
 * Marco de las pantallas con sesión (/app/...): barra superior, menú lateral y contenido.
 * En pantallas pequeñas el menú se superpone y se cierra al elegir una opción.
 */
@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatListModule, MatIconModule, Navbar],
  selector: 'app-private-layout',
  styleUrl: './private-layout.css',
  templateUrl: './private-layout.html',
})
export class PrivateLayout {
  private readonly auth = inject(AuthService);

  /** true en pantallas de celular. */
  protected readonly esMovil = toSignal(
    inject(BreakpointObserver).observe('(max-width: 768px)').pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  /** En escritorio el menú empieza abierto; en celular, cerrado. */
  protected readonly menuAbierto = signal(!this.esMovil());

  protected readonly opciones = computed(() => {
    const rol = this.auth.rol();
    return OPCIONES.filter((opcion) => rol !== null && opcion.roles.includes(rol));
  });

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  alElegirOpcion(): void {
    if (this.esMovil()) {
      this.menuAbierto.set(false);
    }
  }
}
