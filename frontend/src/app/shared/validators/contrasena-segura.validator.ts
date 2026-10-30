import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Al menos una minúscula, una mayúscula y un número (mismo @Pattern que RegistroRequest del back). */
const LETRAS_Y_NUMEROS = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/;

/**
 * Validador de campo: la contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número.
 * Si el campo está vacío no dice nada: de eso se encarga Validators.required.
 * Si no cumple, el campo tiene el error "contrasenaInsegura".
 *
 * Uso (HU-01 registro, HU-03 restablecer):
 *   contrasena: ['', [Validators.required, contrasenaSegura()]]
 */
export function contrasenaSegura(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor: string = control.value ?? '';
    if (valor === '') {
      return null;
    }
    return valor.length >= 8 && LETRAS_Y_NUMEROS.test(valor) ? null : { contrasenaInsegura: true };
  };
}
