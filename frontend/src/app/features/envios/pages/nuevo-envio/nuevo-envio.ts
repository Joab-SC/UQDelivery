import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin, merge } from 'rxjs';
import { Cargando } from '../../../../shared/components/cargando/cargando';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { ResumenCotizacion } from '../../components/resumen-cotizacion/resumen-cotizacion';
import { CotizacionResponse } from '../../models/cotizacion.models';
import { CiudadCodigo, CiudadResponse, EnvioRequest, PoliticasResponse } from '../../models/envio.models';
import { CotizacionService } from '../../services/cotizacion.service';
import { EnviosService } from '../../services/envios.service';

/** Número mayor a 0 con máximo 2 decimales (mismas reglas que @Positive y @Digits del back). */
const NUMERO_POSITIVO = /^\d+(\.\d{1,2})?$/;
const TELEFONO = /^[0-9]{7,15}$/;

/** Campos que cambian el precio: si cambian después de cotizar, hay que cotizar otra vez. */
const CAMPOS_COTIZACION = ['ciudadOrigen', 'ciudadDestino', 'pesoKg', 'largoCm', 'anchoCm', 'altoCm'] as const;

/** Nombres de los campos del formulario (para mensajeError). */
type CampoEnvio =
  | 'direccionOrigen'
  | 'ciudadOrigen'
  | 'direccionDestino'
  | 'ciudadDestino'
  | 'nombreDestinatario'
  | 'telefonoDestinatario'
  | 'correoDestinatario'
  | 'descripcion'
  | 'pesoKg'
  | 'largoCm'
  | 'anchoCm'
  | 'altoCm';

/** Rechaza 0 y negativos (Validators.min(0) aceptaría el 0). */
const mayorACero: ValidatorFn = (control: AbstractControl) => {
  const valor = control.value;
  return valor === null || valor === '' || Number(valor) > 0 ? null : { mayorACero: true };
};

/**
 * Formulario de solicitud de envío (HU-04).
 * Flujo: llenar → «Cotizar» (HU-05, tarjeta <app-resumen-cotizacion>) → «Confirmar envío» → pantalla con el código.
 * El costo que se muestra es informativo: el back lo recalcula al crear el envío.
 */
@Component({
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    Cargando,
    MensajeError,
    ResumenCotizacion,
  ],
  selector: 'app-nuevo-envio',
  styleUrl: './nuevo-envio.css',
  templateUrl: './nuevo-envio.html',
})
export class NuevoEnvio implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly enviosService = inject(EnviosService);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  // Datos iniciales del formulario (ciudades y límites del paquete)
  protected readonly cargandoDatos = signal(false);
  protected readonly errorDatos = signal<string | null>(null);
  protected readonly ciudades = signal<CiudadResponse[]>([]);
  protected readonly politicas = signal<PoliticasResponse | null>(null);

  // Cotización (HU-05)
  protected readonly cotizacion = signal<CotizacionResponse | null>(null);
  protected readonly cotizando = signal(false);
  protected readonly errorCotizacion = signal<string | null>(null);

  // Creación del envío
  protected readonly creando = signal(false);
  protected readonly errorCrear = signal<string | null>(null);

  protected readonly formulario = this.fb.group({
    direccionOrigen: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    ciudadOrigen: this.fb.control<CiudadCodigo | null>(null, Validators.required),
    direccionDestino: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    ciudadDestino: this.fb.control<CiudadCodigo | null>(null, Validators.required),
    nombreDestinatario: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    telefonoDestinatario: this.fb.nonNullable.control('', [Validators.required, Validators.pattern(TELEFONO)]),
    correoDestinatario: this.fb.nonNullable.control('', [Validators.email, Validators.maxLength(150)]),
    descripcion: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
    pesoKg: this.fb.control<number | null>(null, this.validadoresNumero()),
    largoCm: this.fb.control<number | null>(null, this.validadoresNumero()),
    anchoCm: this.fb.control<number | null>(null, this.validadoresNumero()),
    altoCm: this.fb.control<number | null>(null, this.validadoresNumero()),
  });

  ngOnInit(): void {
    this.cargarDatos();
    this.borrarCotizacionAlCambiarElPaquete();
  }

  cargarDatos(): void {
    this.cargandoDatos.set(true);
    this.errorDatos.set(null);

    forkJoin({
      ciudades: this.enviosService.listarCiudades(),
      politicas: this.enviosService.obtenerPoliticas(),
    }).subscribe({
      next: ({ ciudades, politicas }) => {
        this.ciudades.set(ciudades);
        this.politicas.set(politicas);
        this.aplicarLimites(politicas);
        this.cargandoDatos.set(false);
      },
      error: (err) => {
        this.errorDatos.set(err.error?.message ?? 'No se pudieron cargar los datos del formulario');
        this.cargandoDatos.set(false);
      },
    });
  }

  /** true mientras falten o estén mal los datos que necesita la cotización. */
  datosCotizacionInvalidos(): boolean {
    return CAMPOS_COTIZACION.some((campo) => this.formulario.controls[campo].invalid);
  }

  cotizar(): void {
    const v = this.formulario.getRawValue();
    this.cotizando.set(true);
    this.errorCotizacion.set(null);

    this.cotizacionService
      .cotizar({
        ciudadOrigen: v.ciudadOrigen!,
        ciudadDestino: v.ciudadDestino!,
        pesoKg: v.pesoKg!,
        largoCm: v.largoCm!,
        anchoCm: v.anchoCm!,
        altoCm: v.altoCm!,
      })
      .subscribe({
        next: (respuesta) => {
          this.cotizacion.set(respuesta);
          this.cotizando.set(false);
        },
        error: (err) => {
          this.errorCotizacion.set(err.error?.message ?? 'No se pudo cotizar el envío');
          this.cotizando.set(false);
        },
      });
  }

  confirmar(): void {
    this.errorCrear.set(null);

    // Criterio 2 de HU-04: con campos vacíos no se envía y se señalan los errores.
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.errorCrear.set('Revisa los campos marcados en rojo');
      return;
    }
    if (!this.cotizacion()) {
      this.errorCrear.set('Cotiza el envío para conocer el costo antes de confirmarlo');
      return;
    }

    this.creando.set(true);
    this.enviosService.crear(this.armarRequest()).subscribe({
      next: (envio) => {
        this.creando.set(false);
        this.router.navigate(['/app/envios/creado', envio.codigoSeguimiento], {
          state: { envio, ciudades: this.ciudades() },
        });
      },
      error: (err) => {
        this.errorCrear.set(err.error?.message ?? 'No se pudo crear el envío');
        this.creando.set(false);
      },
    });
  }

  /** Mensaje del primer error del campo, en español y con las mismas reglas que el back. */
  mensajeError(campo: CampoEnvio): string {
    const errores = this.formulario.controls[campo].errors;
    if (!errores) return '';
    if (errores['required']) return 'Este campo es obligatorio';
    if (errores['maxlength']) return `Máximo ${errores['maxlength'].requiredLength} caracteres`;
    if (errores['email']) return 'El correo no es válido';
    if (campo === 'telefonoDestinatario' && errores['pattern']) return 'Debe tener entre 7 y 15 dígitos';
    if (errores['mayorACero']) return 'Debe ser mayor a 0';
    if (errores['pattern']) return 'Máximo 2 decimales';
    if (errores['max']) return `Máximo permitido: ${errores['max'].max}`;
    return 'Valor no válido';
  }

  private validadoresNumero(): ValidatorFn[] {
    return [Validators.required, mayorACero, Validators.pattern(NUMERO_POSITIVO)];
  }

  /** Agrega el máximo de peso y medidas que viene de configuracion_tarifa. */
  private aplicarLimites(politicas: PoliticasResponse): void {
    const c = this.formulario.controls;
    c.pesoKg.addValidators(Validators.max(politicas.pesoMaximoKg));
    for (const medida of [c.largoCm, c.anchoCm, c.altoCm]) {
      medida.addValidators(Validators.max(politicas.medidaMaximaCm));
    }
    for (const control of [c.pesoKg, c.largoCm, c.anchoCm, c.altoCm]) {
      control.updateValueAndValidity({ emitEvent: false });
    }
  }

  /** Si cambian ciudades, peso o medidas después de cotizar, la cotización ya no vale. */
  private borrarCotizacionAlCambiarElPaquete(): void {
    merge(...CAMPOS_COTIZACION.map((campo) => this.formulario.controls[campo].valueChanges))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.cotizacion.set(null);
        this.errorCotizacion.set(null);
      });
  }

  private armarRequest(): EnvioRequest {
    const v = this.formulario.getRawValue();
    const correo = v.correoDestinatario.trim();
    return {
      direccionOrigen: v.direccionOrigen.trim(),
      ciudadOrigen: v.ciudadOrigen!,
      direccionDestino: v.direccionDestino.trim(),
      ciudadDestino: v.ciudadDestino!,
      nombreDestinatario: v.nombreDestinatario.trim(),
      telefonoDestinatario: v.telefonoDestinatario.trim(),
      correoDestinatario: correo === '' ? null : correo,
      descripcion: v.descripcion.trim(),
      pesoKg: v.pesoKg!,
      largoCm: v.largoCm!,
      anchoCm: v.anchoCm!,
      altoCm: v.altoCm!,
    };
  }
}
