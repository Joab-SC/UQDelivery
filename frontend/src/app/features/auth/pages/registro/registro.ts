import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { contrasenaSegura } from '../../../../shared/validators/contrasena-segura.validator';
import { contrasenasIguales } from '../../../../shared/validators/contrasenas-iguales.validator';
import { AuthApiService } from '../../services/auth-api.service';

/**
 * Registro de usuario (HU-01). Siempre crea un EMISOR (lo decide el back; aquí no se elige rol).
 * Flujo: llenar el formulario → «Crear cuenta» → el back responde 201 → aviso → ir a /login.
 * El back no devuelve token al registrar: hay que iniciar sesión después.
 */
@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MensajeError,
  ],
  selector: 'app-registro',
  styleUrl: './registro.css',
  templateUrl: './registro.html',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  // Pizarras que lee el HTML
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  // Mismas reglas que RegistroRequest del back
  protected readonly formulario = this.fb.nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      telefono: ['', [Validators.required, Validators.maxLength(20)]],
      contrasena: ['', [Validators.required, contrasenaSegura()]],
      confirmarContrasena: ['', [Validators.required]],
    },
    // Regla de grupo: revisa dos casillas a la vez
    { validators: contrasenasIguales('contrasena', 'confirmarContrasena') },
  );

  /** true si las dos contraseñas no coinciden y la persona ya tocó la confirmación. */
  contrasenasDistintas(): boolean {
    return (
      this.formulario.hasError('contrasenasDistintas') && this.formulario.controls.confirmarContrasena.touched
    );
  }

  registrar(): void {
    // Si falta algo, se pintan en rojo los campos y no se llama al back
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const datos = this.formulario.getRawValue();

    this.authApi
      .registrar({
        nombre: datos.nombre.trim(),
        correo: datos.correo.trim(),
        telefono: datos.telefono.trim(),
        contrasena: datos.contrasena,
        confirmarContrasena: datos.confirmarContrasena,
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.snackBar.open('Cuenta creada. Ya puedes iniciar sesión', 'Cerrar', { duration: 5000 });
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.error.set(err.error?.message ?? 'No se pudo crear la cuenta');
          this.cargando.set(false);
        },
      });
  }
}
