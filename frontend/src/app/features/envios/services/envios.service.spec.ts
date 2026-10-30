import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { EnvioRequest } from '../models/envio.models';
import { EnviosService } from './envios.service';

describe('EnviosService', () => {
  let servicio: EnviosService;
  let http: HttpTestingController;

  const request: EnvioRequest = {
    direccionOrigen: 'Carrera 14 # 20-35',
    ciudadOrigen: 'ARMENIA',
    direccionDestino: 'Calle 19 # 8-40',
    ciudadDestino: 'PEREIRA',
    nombreDestinatario: 'Andrés López',
    telefonoDestinatario: '3151112233',
    correoDestinatario: null,
    descripcion: 'Documentos en sobre de manila',
    pesoKg: 2.5,
    largoCm: 35,
    anchoCm: 25,
    altoCm: 5,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(EnviosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crea el envío por POST y devuelve el código de seguimiento', () => {
    let codigo = '';
    servicio.crear(request).subscribe((r) => (codigo = r.codigoSeguimiento));

    const peticion = http.expectOne(`${environment.apiUrl}/envios`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(request);
    expect(peticion.request.body).not.toHaveProperty('costo');

    peticion.flush({ ...request, id: 6, codigoSeguimiento: 'UQD-7KQ2M9XA', costo: 16200, estado: 'PENDIENTE' });
    expect(codigo).toBe('UQD-7KQ2M9XA');
  });

  it('lista las ciudades por GET', () => {
    let total = 0;
    servicio.listarCiudades().subscribe((ciudades) => (total = ciudades.length));

    const peticion = http.expectOne(`${environment.apiUrl}/ciudades`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([
      { codigo: 'ARMENIA', nombre: 'Armenia' },
      { codigo: 'CALARCA', nombre: 'Calarcá' },
    ]);
    expect(total).toBe(2);
  });

  it('obtiene las políticas del paquete por GET', () => {
    let pesoMaximo = 0;
    servicio.obtenerPoliticas().subscribe((p) => (pesoMaximo = p.pesoMaximoKg));

    const peticion = http.expectOne(`${environment.apiUrl}/configuracion/politicas`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush({ pesoMaximoKg: 50, medidaMaximaCm: 150 });
    expect(pesoMaximo).toBe(50);
  });

  it('propaga el ApiError del back', () => {
    let mensaje = '';
    servicio.crear(request).subscribe({ error: (err) => (mensaje = err.error.message) });

    http
      .expectOne(`${environment.apiUrl}/envios`)
      .flush(
        { status: 400, message: 'El paquete no cumple con las políticas de tamaño' },
        { status: 400, statusText: 'Bad Request' },
      );

    expect(mensaje).toBe('El paquete no cumple con las políticas de tamaño');
  });
});
