import { Rol } from './rol';

// Espejo de UsuarioResponse del back (contrato en docs/api.md).
export interface UsuarioResponse {
  id: number;
  nombre: string;
  correo: string;
  telefono: string;
  rol: Rol;
  /** LocalDateTime del back: llega como texto ISO, p. ej. "2026-10-21T10:30:00" */
  fechaRegistro: string;
}

// Respuesta de POST /api/auth/login (HU-01).
export interface LoginResponse {
  token: string;
  usuario: UsuarioResponse;
}
