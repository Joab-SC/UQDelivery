/** Ciudades del back (enum Ciudad). Llegan como texto. */
export type CiudadCodigo = 'ARMENIA' | 'PEREIRA' | 'MANIZALES' | 'CALARCA' | 'MONTENEGRO';

// Espejo de CotizacionRequest del back.
export interface CotizacionRequest {
  ciudadOrigen: CiudadCodigo;
  ciudadDestino: CiudadCodigo;
  pesoKg: number;
  largoCm: number;
  anchoCm: number;
  altoCm: number;
}

// Espejo de CotizacionResponse del back.
export interface CotizacionResponse {
  /** Costo en COP, redondeado a la centena. */
  costo: number;
  /** Distancia estimada entre las ciudades, en km. */
  distanciaKm: number;
}
