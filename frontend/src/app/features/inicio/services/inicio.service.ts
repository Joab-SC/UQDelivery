import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { HealthResponse } from '../models/health.models';

/**
 * Servicio de la feature "inicio". Es el único lugar de la feature que habla con la API:
 * los componentes nunca usan HttpClient directamente.
 *
 * Mientras un endpoint no exista en el back, el método devuelve datos falsos con el mismo contrato:
 *   return of({ estado: 'UP' }).pipe(delay(800));
 *   // return this.http.get<HealthResponse>(`${environment.apiUrl}/health`);
 * Cuando el endpoint esté listo, se cambia solo esta línea y el componente no se toca.
 */
@Injectable({ providedIn: 'root' })
export class InicioService {
  private readonly http = inject(HttpClient);

  obtenerEstadoApi(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${environment.apiUrl}/health`);
  }
}
