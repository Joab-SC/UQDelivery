import { Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/**
 * Indicador de carga (círculo girando) con un texto opcional.
 * Uso: @if (cargando()) { <app-cargando texto="Cargando usuarios..." /> }
 */
@Component({
  imports: [MatProgressSpinnerModule],
  selector: 'app-cargando',
  styleUrl: './cargando.css',
  templateUrl: './cargando.html',
})
export class Cargando {
  readonly texto = input('Cargando...');
}
