// Espejo de los DTOs de HU-01 en el back (contrato en docs/api.md).
// LoginResponse y UsuarioResponse ya existen en core/models/usuario.models.ts.

// Lo que se envía a POST /api/auth/login
export interface LoginRequest {
  correo: string;
  contrasena: string;
}

// Lo que se envía a POST /api/auth/registro (responde UsuarioResponse, sin token)
export interface RegistroRequest {
  nombre: string;
  correo: string;
  telefono: string;
  contrasena: string;
  confirmarContrasena: string;
}
