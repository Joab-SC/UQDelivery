import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Rol } from '../../../core/models/rol';
import { UsuarioResponse } from '../../../core/models/usuario.models';
import { CambiarRolRequest } from '../models/usuario.models';

/**
 * Servicio de la feature "usuarios". Es el único lugar de la feature que habla con la API.
 * Ambos endpoints son solo para ADMIN; el token lo agrega auth.interceptor.ts.
 */
@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly http = inject(HttpClient);

  listar(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(`${environment.apiUrl}/usuarios`);
  }

  cambiarRol(id: number, rol: Rol): Observable<UsuarioResponse> {
    const cuerpo: CambiarRolRequest = { rol };
    return this.http.patch<UsuarioResponse>(`${environment.apiUrl}/usuarios/${id}/rol`, cuerpo);
  }
}
