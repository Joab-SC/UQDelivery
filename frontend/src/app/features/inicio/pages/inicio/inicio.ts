import { Component, inject } from '@angular/core';
import { AuthService } from '../../../../core/auth/auth.service';

/** Página de bienvenida: saluda al usuario y muestra su rol. */
@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.html',
})
export class Inicio {
  protected readonly auth = inject(AuthService);
}
