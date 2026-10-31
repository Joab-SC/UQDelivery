import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  MensajeResponse,
  RecuperarRequest,
  RestablecerRequest,
} from '../models/recuperacion.models';

/**
 * Servicio de HU-03. Es el único lugar que habla con /api/auth/recuperar y /api/auth/restablecer.
 * Va en un archivo propio (no en auth-api.service.ts de HU-01) para no editar el mismo archivo.
 */
@Injectable({ providedIn: 'root' })
export class RecuperacionService {
  private readonly http = inject(HttpClient);

  /** Paso 1: pide el enlace al correo. El back responde lo mismo aunque el correo no exista. */
  solicitar(datos: RecuperarRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${environment.apiUrl}/auth/recuperar`, datos);
  }

  /** Paso 2: cambia la contraseña con el token del enlace. */
  restablecer(datos: RestablecerRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${environment.apiUrl}/auth/restablecer`, datos);
  }
}
