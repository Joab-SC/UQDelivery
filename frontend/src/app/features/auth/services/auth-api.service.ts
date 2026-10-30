import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LoginResponse, UsuarioResponse } from '../../../core/models/usuario.models';
import { LoginRequest, RegistroRequest } from '../models/auth.models';

/**
 * Servicio de HU-01. Es el único lugar que habla con /api/auth/login y /api/auth/registro.
 * Se llama "auth-api" para no confundirlo con core/auth/auth.service.ts, que solo GUARDA la sesión.
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, datos);
  }

  registrar(datos: RegistroRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${environment.apiUrl}/auth/registro`, datos);
  }
}
