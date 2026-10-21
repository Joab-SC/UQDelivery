import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { Cargando } from '../../../../shared/components/cargando/cargando';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { InicioService } from '../../services/inicio.service';

/**
 * Página de bienvenida. Sirve de EJEMPLO del patrón de página que usan todas las historias:
 * 1. Signals para el estado: cargando, error y el resultado.
 * 2. El componente llama al servicio de su feature (nunca a HttpClient).
 * 3. subscribe({ next, error }) y, si falla, se muestra err.error.message del ApiError.
 */
@Component({
  imports: [MatCardModule, MatButtonModule, MatIconModule, Cargando, MensajeError],
  selector: 'app-inicio',
  styleUrl: './inicio.css',
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  private readonly inicioService = inject(InicioService);
  protected readonly auth = inject(AuthService);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly estadoApi = signal<string | null>(null);

  ngOnInit(): void {
    this.consultarEstado();
  }

  consultarEstado(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.inicioService.obtenerEstadoApi().subscribe({
      next: (respuesta) => {
        this.estadoApi.set(respuesta.estado);
        this.cargando.set(false);
      },
      error: (err) => {
        this.estadoApi.set(null);
        this.error.set(err.error?.message ?? 'No se pudo consultar el estado de la API');
        this.cargando.set(false);
      },
    });
  }
}
