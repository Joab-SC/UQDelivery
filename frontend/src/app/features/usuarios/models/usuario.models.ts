import { Rol } from '../../../core/models/rol';

// Espejo de CambiarRolRequest del back (contrato en docs/api.md).
// UsuarioResponse se importa de core/models/usuario.models.ts.
export interface CambiarRolRequest {
  rol: Rol;
}
