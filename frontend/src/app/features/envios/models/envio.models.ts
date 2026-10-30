// Modelos de HU-04: espejo de los DTOs del back, con los mismos nombres.
// Los enums llegan como texto y LocalDateTime como string ISO.

/** Valores del enum Ciudad del back. */
export type CiudadCodigo = 'ARMENIA' | 'PEREIRA' | 'MANIZALES' | 'CALARCA' | 'MONTENEGRO';

/** Valores del enum EstadoEnvio del back. */
export type EstadoEnvio = 'PENDIENTE' | 'ASIGNADO' | 'EN_CAMINO' | 'ENTREGADO' | 'CANCELADO';

// Espejo de CiudadResponse (GET /api/ciudades).
export interface CiudadResponse {
  /** Valor que se envía al back, p. ej. "CALARCA". */
  codigo: CiudadCodigo;
  /** Nombre para mostrar, p. ej. "Calarcá". */
  nombre: string;
}

// Espejo de PoliticasResponse (GET /api/configuracion/politicas).
export interface PoliticasResponse {
  pesoMaximoKg: number;
  /** Máximo para cada medida: largo, ancho y alto. */
  medidaMaximaCm: number;
}

// Espejo de EnvioRequest (POST /api/envios). Sin costo: lo calcula el back.
export interface EnvioRequest {
  direccionOrigen: string;
  ciudadOrigen: CiudadCodigo;
  direccionDestino: string;
  ciudadDestino: CiudadCodigo;
  nombreDestinatario: string;
  telefonoDestinatario: string;
  /** Opcional. */
  correoDestinatario: string | null;
  descripcion: string;
  pesoKg: number;
  largoCm: number;
  anchoCm: number;
  altoCm: number;
}

// Espejo de EnvioResponse.
export interface EnvioResponse {
  id: number;
  codigoSeguimiento: string;
  direccionOrigen: string;
  ciudadOrigen: CiudadCodigo;
  direccionDestino: string;
  ciudadDestino: CiudadCodigo;
  nombreDestinatario: string;
  telefonoDestinatario: string;
  correoDestinatario: string | null;
  descripcion: string;
  pesoKg: number;
  largoCm: number;
  anchoCm: number;
  altoCm: number;
  distanciaKm: number;
  costo: number;
  estado: EstadoEnvio;
  fechaCreacion: string;
}
