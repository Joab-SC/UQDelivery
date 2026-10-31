// Espejo de los DTOs de HU-03 en el back (contrato en docs/api.md).

// Lo que se envía a POST /api/auth/recuperar
export interface RecuperarRequest {
  correo: string;
}

// Lo que se envía a POST /api/auth/restablecer (el token llega en el enlace del correo)
export interface RestablecerRequest {
  token: string;
  contrasena: string;
  confirmarContrasena: string;
}

// Lo que responden los dos endpoints: un mensaje para mostrar
export interface MensajeResponse {
  mensaje: string;
}
