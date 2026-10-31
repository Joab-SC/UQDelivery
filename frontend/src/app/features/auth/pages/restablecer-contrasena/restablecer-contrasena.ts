import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { contrasenaSegura } from '../../../../shared/validators/contrasena-segura.validator';
import { contrasenasIguales } from '../../../../shared/validators/contrasenas-iguales.validator';
import { RecuperacionService } from '../../services/recuperacion.service';

/**
 * Paso 2 de la recuperación de contraseña (HU-03). Ruta pública: /restablecer?token=...
 * Se llega aquí desde el enlace del correo.
 * Flujo: escribir la contraseña nueva dos veces → «Cambiar contraseña» → aviso → ir a /login.
 * Si el enlace venció o ya se usó, el back responde 400 y se ofrece pedir uno nuevo.
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
  selector: 'app-restablecer-contrasena',
  styleUrl: './restablecer-contrasena.css',
  templateUrl: './restablecer-contrasena.html',
})
export class RestablecerContrasena {
  private readonly fb = inject(FormBuilder);
  private readonly recuperacion = inject(RecuperacionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  /**
   * El ?token=... de la URL. Llega solo gracias a withComponentInputBinding() de app.config.ts:
   * Angular pasa los parámetros de la URL a los input() que se llamen igual.
   */
  readonly token = input<string>();

  // Pizarras que lee el HTML
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  // Mismas reglas que RestablecerRequest del back (72 = límite de BCrypt)
  protected readonly formulario = this.fb.nonNullable.group(
    {
      contrasena: ['', [Validators.required, Validators.maxLength(72), contrasenaSegura()]],
      confirmarContrasena: ['', [Validators.required]],
    },
    // Regla de grupo: revisa dos casillas a la vez
    { validators: contrasenasIguales('contrasena', 'confirmarContrasena') },
  );

  /** true si las dos contraseñas no coinciden y la persona ya tocó la confirmación. */
  contrasenasDistintas(): boolean {
    return (
      this.formulario.hasError('contrasenasDistintas') &&
      this.formulario.controls.confirmarContrasena.touched
    );
  }

  cambiar(): void {
    // Si falta algo, se pintan en rojo los campos y no se llama al back
    const token = this.token();
    if (this.formulario.invalid || !token) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const datos = this.formulario.getRawValue();

    this.recuperacion
      .restablecer({
        token,
        contrasena: datos.contrasena,
        confirmarContrasena: datos.confirmarContrasena,
      })
      .subscribe({
        next: (respuesta) => {
          this.cargando.set(false);
          this.snackBar.open(respuesta.mensaje, 'Cerrar', { duration: 5000 });
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.error.set(err.error?.message ?? 'No se pudo cambiar la contraseña');
          this.cargando.set(false);
        },
      });
  }
}
