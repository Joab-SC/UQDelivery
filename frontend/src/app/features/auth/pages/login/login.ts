import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../../core/auth/auth.service';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { AuthApiService } from '../../services/auth-api.service';

/**
 * Inicio de sesión (HU-01).
 * Flujo: llenar correo y contraseña → «Ingresar» → el back responde { token, usuario }
 * → se guarda la sesión → se va a /app/inicio. Si falla, se muestra el mensaje del back.
 */
@Component({
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MensajeError],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // Pizarras que lee el HTML
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  // El formulario: dos casillas con las mismas reglas que LoginRequest del back
  protected readonly formulario = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required]],
  });

  ingresar(): void {
    // Si falta algo, se pintan en rojo los campos y no se llama al back
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const datos = this.formulario.getRawValue();

    this.authApi.login({ correo: datos.correo.trim(), contrasena: datos.contrasena }).subscribe({
      next: (respuesta) => {
        this.auth.iniciarSesion(respuesta);
        this.cargando.set(false);
        this.router.navigate(['/app/inicio']);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'No se pudo iniciar sesión');
        this.cargando.set(false);
      },
    });
  }
}
