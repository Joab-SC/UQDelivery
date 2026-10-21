import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/api-error';
import { AuthService } from '../auth/auth.service';

/**
 * Maneja errores comunes a todas las peticiones:
 * - 401 (token vencido o inválido): cierra la sesión y manda a /login.
 *   Excepto en /api/auth/**, donde un 401 significa "correo o contraseña incorrectos" y lo muestra la página.
 * - Sin conexión con el back (status 0): deja un ApiError con un mensaje claro, para que las páginas
 *   siempre puedan mostrar err.error.message.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/api/auth/')) {
        auth.cerrarSesion();
        router.navigate(['/login']);
      }

      if (err.status === 0) {
        const sinConexion: ApiError = {
          status: 0,
          message: 'No se pudo conectar con el servidor. Revisa que el back esté encendido.',
        };
        return throwError(() => new HttpErrorResponse({ error: sinConexion, status: 0, url: req.url }));
      }

      return throwError(() => err);
    }),
  );
};
