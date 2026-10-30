import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CiudadCodigo, CiudadResponse, EnvioResponse } from '../../models/envio.models';

const FORMATO_COP = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

/**
 * Confirmación de HU-04: muestra el código de seguimiento del envío recién creado.
 * El código llega en la URL (/app/envios/creado/UQD-XXXXXXXX). El envío y la lista de ciudades llegan
 * en el estado de la navegación desde nuevo-envio. Si se recarga la página, ese estado se pierde
 * y solo se muestra el código.
 */
@Component({
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  selector: 'app-envio-creado',
  styleUrl: './envio-creado.css',
  templateUrl: './envio-creado.html',
})
export class EnvioCreado {
  private readonly snackBar = inject(MatSnackBar);

  /** Parámetro :codigo de la ruta (withComponentInputBinding lo pasa como input). */
  readonly codigo = input.required<string>();

  /** Envío que mandó la página nuevo-envio al navegar. Null si se recargó la página. */
  protected readonly envio: EnvioResponse | null = (history.state?.envio as EnvioResponse | undefined) ?? null;

  /** Ciudades que ya tenía cargadas nuevo-envio, para mostrar "Calarcá" en vez de "CALARCA". */
  private readonly ciudades: CiudadResponse[] = (history.state?.ciudades as CiudadResponse[] | undefined) ?? [];

  protected readonly costoTexto = computed(() => (this.envio ? FORMATO_COP.format(this.envio.costo) : ''));

  /** Nombre para mostrar de una ciudad. Si no lo encuentra, muestra el código. */
  nombreCiudad(codigo: CiudadCodigo): string {
    return this.ciudades.find((ciudad) => ciudad.codigo === codigo)?.nombre ?? codigo;
  }

  copiarCodigo(): void {
    navigator.clipboard
      .writeText(this.codigo())
      .then(() => this.snackBar.open('Código copiado', 'Cerrar', { duration: 3000 }))
      .catch(() => this.snackBar.open('No se pudo copiar el código', 'Cerrar', { duration: 3000 }));
  }
}
