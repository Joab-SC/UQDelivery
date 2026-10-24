import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CotizacionRequest, CotizacionResponse } from '../models/cotizacion.models';

/**
 * Servicio de la cotización (HU-05). Es el único lugar que habla con POST /api/envios/cotizacion.
 * Lo usa la página de HU-04 y pasa el resultado a <app-resumen-cotizacion>.
 */
@Injectable({ providedIn: 'root' })
export class CotizacionService {
  private readonly http = inject(HttpClient);

  cotizar(request: CotizacionRequest): Observable<CotizacionResponse> {
    return this.http.post<CotizacionResponse>(`${environment.apiUrl}/envios/cotizacion`, request);
  }
}
