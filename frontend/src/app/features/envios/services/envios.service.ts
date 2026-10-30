import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CiudadResponse, EnvioRequest, EnvioResponse, PoliticasResponse } from '../models/envio.models';

/**
 * Servicio de HU-04. Es el único lugar que habla con:
 *   POST /api/envios, GET /api/ciudades y GET /api/configuracion/politicas.
 * La cotización la hace CotizacionService (HU-05).
 */
@Injectable({ providedIn: 'root' })
export class EnviosService {
  private readonly http = inject(HttpClient);

  crear(request: EnvioRequest): Observable<EnvioResponse> {
    return this.http.post<EnvioResponse>(`${environment.apiUrl}/envios`, request);
  }

  listarCiudades(): Observable<CiudadResponse[]> {
    return this.http.get<CiudadResponse[]>(`${environment.apiUrl}/ciudades`);
  }

  obtenerPoliticas(): Observable<PoliticasResponse> {
    return this.http.get<PoliticasResponse>(`${environment.apiUrl}/configuracion/politicas`);
  }
}
