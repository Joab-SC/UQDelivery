import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { CotizacionRequest } from '../models/cotizacion.models';
import { CotizacionService } from './cotizacion.service';

describe('CotizacionService', () => {
  let servicio: CotizacionService;
  let http: HttpTestingController;

  const request: CotizacionRequest = {
    ciudadOrigen: 'ARMENIA',
    ciudadDestino: 'PEREIRA',
    pesoKg: 2.5,
    largoCm: 35,
    anchoCm: 25,
    altoCm: 5,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(CotizacionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía el paquete por POST y devuelve costo y distancia', () => {
    let resultado: unknown;
    servicio.cotizar(request).subscribe((r) => (resultado = r));

    const peticion = http.expectOne(`${environment.apiUrl}/envios/cotizacion`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(request);

    peticion.flush({ costo: 16200, distanciaKm: 43.56 });
    expect(resultado).toEqual({ costo: 16200, distanciaKm: 43.56 });
  });

  it('propaga el error del back (ApiError)', () => {
    let mensaje = '';
    servicio.cotizar(request).subscribe({ error: (err) => (mensaje = err.error.message) });

    http
      .expectOne(`${environment.apiUrl}/envios/cotizacion`)
      .flush(
        { status: 400, message: 'El paquete no cumple con las políticas de tamaño' },
        { status: 400, statusText: 'Bad Request' },
      );

    expect(mensaje).toBe('El paquete no cumple con las políticas de tamaño');
  });
});
