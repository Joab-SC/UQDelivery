import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { Rol } from '../../../../core/models/rol';
import { UsuarioResponse } from '../../../../core/models/usuario.models';

/**
 * Login TEMPORAL de HU-00.
 * Mientras HU-01 no termine el login real, estos botones simulan una sesión para poder probar
 * las pantallas de cada rol. El token es falso: el back lo rechaza en las rutas protegidas.
 * HU-01 reemplaza esta página por el formulario real y borra los botones de desarrollo.
 */
@Component({
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly roles: Rol[] = ['ADMIN', 'EMISOR', 'REPARTIDOR'];

  protected readonly opciones: { rol: Rol; icono: string }[] = [
    { rol: 'ADMIN', icono: 'admin_panel_settings' },
    { rol: 'EMISOR', icono: 'outbox' },
    { rol: 'REPARTIDOR', icono: 'two_wheeler' },
  ];

  entrarComo(rol: Rol): void {
    const usuario: UsuarioResponse = {
      id: this.roles.indexOf(rol) + 1,
      nombre: `Usuario ${rol.toLowerCase()}`,
      correo: `${rol.toLowerCase()}@uqdelivery.co`,
      telefono: '3000000000',
      rol,
      fechaRegistro: new Date().toISOString(),
    };
    this.auth.iniciarSesion({ token: `token-de-desarrollo-${rol.toLowerCase()}`, usuario });
    this.router.navigate(['/app/inicio']);
  }
}
