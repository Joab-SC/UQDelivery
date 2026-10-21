import { computed, Injectable, signal } from '@angular/core';
import { LoginResponse, UsuarioResponse } from '../models/usuario.models';

const CLAVE_TOKEN = 'uqd_token';
const CLAVE_USUARIO = 'uqd_usuario';

/**
 * Guarda la sesión del usuario: el token JWT y sus datos.
 * - Los datos viven en signals: cualquier componente que los lea se actualiza solo cuando cambian.
 * - También se guardan en localStorage para que la sesión sobreviva a recargar la página.
 * - Este servicio NO llama a la API: el login lo hace el servicio de la feature auth (HU-01)
 *   y luego llama a iniciarSesion(...) con la respuesta.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _token = signal<string | null>(null);
  private readonly _usuario = signal<UsuarioResponse | null>(null);

  /** Token JWT actual (null si no hay sesión). */
  readonly token = this._token.asReadonly();
  /** Usuario con sesión iniciada (null si no hay sesión). */
  readonly usuario = this._usuario.asReadonly();
  /** Rol del usuario actual. */
  readonly rol = computed(() => this._usuario()?.rol ?? null);
  /** true si hay una sesión iniciada. */
  readonly autenticado = computed(() => this._token() !== null);

  constructor() {
    this.restaurarSesion();
  }

  iniciarSesion(respuesta: LoginResponse): void {
    this._token.set(respuesta.token);
    this._usuario.set(respuesta.usuario);
    try {
      localStorage.setItem(CLAVE_TOKEN, respuesta.token);
      localStorage.setItem(CLAVE_USUARIO, JSON.stringify(respuesta.usuario));
    } catch {
      // Sin localStorage (modo privado): la sesión dura hasta recargar la página
    }
  }

  cerrarSesion(): void {
    this._token.set(null);
    this._usuario.set(null);
    try {
      localStorage.removeItem(CLAVE_TOKEN);
      localStorage.removeItem(CLAVE_USUARIO);
    } catch {
      // Nada que limpiar
    }
  }

  /** Recupera la sesión guardada, si existe y el token no ha vencido. */
  private restaurarSesion(): void {
    try {
      const token = localStorage.getItem(CLAVE_TOKEN);
      const usuario = localStorage.getItem(CLAVE_USUARIO);
      if (token && usuario && !this.tokenVencido(token)) {
        this._token.set(token);
        this._usuario.set(JSON.parse(usuario) as UsuarioResponse);
      } else {
        this.cerrarSesion();
      }
    } catch {
      this.cerrarSesion();
    }
  }

  /** Lee la fecha de vencimiento (exp) del token. Si no se puede leer, no lo da por vencido: el back decide. */
  private tokenVencido(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 < Date.now();
    } catch {
      return false;
    }
  }
}
