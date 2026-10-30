import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../../core/auth/auth.service';
import { Rol } from '../../../../core/models/rol';
import { UsuarioResponse } from '../../../../core/models/usuario.models';
import { Cargando } from '../../../../shared/components/cargando/cargando';
import { MensajeError } from '../../../../shared/components/mensaje-error/mensaje-error';
import { UsuariosService } from '../../services/usuarios.service';

/**
 * Gestión de usuarios y roles (solo ADMIN).
 * Lista los usuarios en una tabla y permite cambiar el rol de cada uno con un selector.
 * El admin no puede cambiar su propio rol: su selector aparece deshabilitado (y el back también lo bloquea).
 */
@Component({
  imports: [DatePipe, MatTableModule, MatSelectModule, MatButtonModule, MatIconModule, Cargando, MensajeError],
  selector: 'app-gestion-usuarios',
  styleUrl: './gestion-usuarios.css',
  templateUrl: './gestion-usuarios.html',
})
export class GestionUsuarios implements OnInit {
  private readonly usuariosService = inject(UsuariosService);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);

  protected readonly roles: Rol[] = ['ADMIN', 'EMISOR', 'REPARTIDOR'];
  protected readonly columnas = ['nombre', 'correo', 'telefono', 'fechaRegistro', 'rol'];

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly usuarios = signal<UsuarioResponse[]>([]);
  /** Id del usuario cuyo rol se está guardando (null si ninguno). */
  protected readonly guardandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.usuariosService.listar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'No se pudo cargar la lista de usuarios');
        this.cargando.set(false);
      },
    });
  }

  esUsuarioActual(usuario: UsuarioResponse): boolean {
    return usuario.id === this.auth.usuario()?.id;
  }

  cambiarRol(usuario: UsuarioResponse, evento: MatSelectChange): void {
    const nuevoRol = evento.value as Rol;
    if (nuevoRol === usuario.rol) {
      return;
    }

    this.guardandoId.set(usuario.id);
    this.usuariosService.cambiarRol(usuario.id, nuevoRol).subscribe({
      next: (actualizado) => {
        this.usuarios.update((lista) => lista.map((u) => (u.id === actualizado.id ? actualizado : u)));
        this.guardandoId.set(null);
        this.snackBar.open(`Rol de ${actualizado.nombre} actualizado a ${actualizado.rol}`, 'Cerrar', {
          duration: 4000,
        });
      },
      error: (err) => {
        // Se vuelve a pintar la lista para que el selector regrese al rol real
        this.usuarios.update((lista) => [...lista]);
        this.guardandoId.set(null);
        this.snackBar.open(err.error?.message ?? 'No se pudo cambiar el rol', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
