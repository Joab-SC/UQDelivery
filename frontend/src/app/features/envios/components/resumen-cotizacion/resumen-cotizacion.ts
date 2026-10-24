import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { Cargando } from '../../../../shared/components/cargando/cargando';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { CotizacionResponse } from '../../models/cotizacion.models';

const FORMATO_COP = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});
const FORMATO_KM = new Intl.NumberFormat('es-CO', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * Tarjeta de cotización (HU-05). La página de HU-04 la inserta en su formulario:
 *   <app-resumen-cotizacion
 *     [cotizacion]="cotizacion()" [cargando]="cotizando()" [error]="errorCotizacion()"
 *     [deshabilitado]="formulario.invalid" (cotizar)="cotizar()" />
 * No llama a la API: solo muestra lo que recibe y avisa cuando el usuario pulsa "Cotizar".
 */
@Component({
  imports: [MatCardModule, MatButtonModule, MatIconModule, Cargando, MensajeError],
  selector: 'app-resumen-cotizacion',
  styleUrl: './resumen-cotizacion.css',
  templateUrl: './resumen-cotizacion.html',
})
export class ResumenCotizacion {
  /** Resultado de la cotización; null si aún no se cotizó (o si cambió el formulario). */
  readonly cotizacion = input<CotizacionResponse | null>(null);
  readonly cargando = input(false);
  /** Mensaje del ApiError, p. ej. "El paquete no cumple con las políticas de tamaño". */
  readonly error = input<string | null>(null);
  /** true mientras el formulario del paquete no sea válido. */
  readonly deshabilitado = input(false);

  /** El usuario pulsó "Cotizar". */
  readonly cotizar = output<void>();

  protected readonly costoTexto = computed(() => {
    const c = this.cotizacion();
    return c ? FORMATO_COP.format(c.costo) : '';
  });

  protected readonly distanciaTexto = computed(() => {
    const c = this.cotizacion();
    return c ? `${FORMATO_KM.format(c.distanciaKm)} km` : '';
  });
}
