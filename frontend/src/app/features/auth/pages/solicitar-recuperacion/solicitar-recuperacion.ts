import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { RecuperacionService } from '../../services/recuperacion.service';

/**
 * Paso 1 de la recuperación de contraseña (HU-03). Ruta pública: /recuperar
 * Flujo: escribir el correo → «Enviar enlace» → el back responde siempre el mismo mensaje
 * (exista o no la cuenta) → se cambia el formulario por un aviso de "revisa tu correo".
 */
@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MensajeError,
  ],
  selector: 'app-solicitar-recuperacion',
  styleUrl: './solicitar-recuperacion.css',
  templateUrl: './solicitar-recuperacion.html',
})
export class SolicitarRecuperacion {
  private readonly fb = inject(FormBuilder);
  private readonly recuperacion = inject(RecuperacionService);

  // Pizarras que lee el HTML
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Mensaje del back cuando la solicitud se envió; mientras sea null se muestra el formulario. */
  protected readonly confirmacion = signal<string | null>(null);

  // Mismas reglas que RecuperarRequest del back
  protected readonly formulario = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
  });

  enviar(): void {
    // Si falta algo, se pinta en rojo el campo y no se llama al back
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.recuperacion.solicitar({ correo: this.formulario.getRawValue().correo.trim() }).subscribe({
      next: (respuesta) => {
        this.confirmacion.set(respuesta.mensaje);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'No se pudo enviar la solicitud');
        this.cargando.set(false);
      },
    });
  }

  /** «Enviar otra vez»: vuelve al formulario con el mismo correo escrito. */
  volverAIntentar(): void {
    this.confirmacion.set(null);
  }
}
