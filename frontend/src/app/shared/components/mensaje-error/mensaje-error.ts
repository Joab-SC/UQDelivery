import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Muestra un mensaje de error en un recuadro rojo. No muestra nada si el mensaje es null o vacío.
 * Uso: <app-mensaje-error [mensaje]="error()" />
 */
@Component({
  imports: [MatIconModule],
  selector: 'app-mensaje-error',
  styleUrl: './mensaje-error.css',
  templateUrl: './mensaje-error.html',
})
export class MensajeError {
  readonly mensaje = input<string | null>(null);
}
