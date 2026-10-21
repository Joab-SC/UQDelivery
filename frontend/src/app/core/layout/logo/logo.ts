import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Logo de la aplicación: un cuadro con el ícono de paquete y el nombre "UQDelivery".
 * Lo usan la barra del panel privado y la del layout público.
 */
@Component({
  imports: [MatIconModule],
  selector: 'app-logo',
  styleUrl: './logo.css',
  templateUrl: './logo.html',
})
export class Logo {}
