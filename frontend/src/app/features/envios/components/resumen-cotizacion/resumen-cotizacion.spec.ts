import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResumenCotizacion } from './resumen-cotizacion';

describe('ResumenCotizacion', () => {
  let fixture: ComponentFixture<ResumenCotizacion>;

  const texto = () => (fixture.nativeElement as HTMLElement).textContent ?? '';
  const boton = () => (fixture.nativeElement as HTMLElement).querySelector('button')!;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ResumenCotizacion] }).compileComponents();
    fixture = TestBed.createComponent(ResumenCotizacion);
    fixture.detectChanges();
  });

  it('antes de cotizar muestra la ayuda', () => {
    expect(texto()).toContain('pulsa «Cotizar»');
  });

  it('muestra costo en pesos y distancia', () => {
    fixture.componentRef.setInput('cotizacion', { costo: 16200, distanciaKm: 43.56 });
    fixture.detectChanges();

    expect(texto()).toContain('16.200');
    expect(texto()).toContain('43,56 km');
  });

  it('muestra el error del back', () => {
    fixture.componentRef.setInput('error', 'El paquete no cumple con las políticas de tamaño');
    fixture.detectChanges();

    expect(texto()).toContain('El paquete no cumple con las políticas de tamaño');
  });

  it('emite "cotizar" al pulsar el botón', () => {
    const avisos = vi.fn();
    fixture.componentInstance.cotizar.subscribe(avisos);

    boton().click();

    expect(avisos).toHaveBeenCalledTimes(1);
  });

  it('el botón se bloquea si el formulario es inválido o está cargando', () => {
    fixture.componentRef.setInput('deshabilitado', true);
    fixture.detectChanges();
    expect(boton().disabled).toBe(true);

    fixture.componentRef.setInput('deshabilitado', false);
    fixture.componentRef.setInput('cargando', true);
    fixture.detectChanges();
    expect(boton().disabled).toBe(true);
  });
});
