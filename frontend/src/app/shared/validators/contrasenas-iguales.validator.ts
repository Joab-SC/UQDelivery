import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador de formulario: comprueba que dos campos tengan el mismo valor.
 * Se pone en el FormGroup, no en el campo. Si no coinciden, el grupo tiene el error "contrasenasDistintas".
 *
 * Uso (HU-01 registro, HU-03 restablecer):
 *   this.fb.group({ contrasena: [''], confirmacion: [''] },
 *                 { validators: contrasenasIguales('contrasena', 'confirmacion') });
 */
export function contrasenasIguales(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const valor = grupo.get(campo)?.value;
    const repetido = grupo.get(confirmacion)?.value;
    return valor && repetido && valor !== repetido ? { contrasenasDistintas: true } : null;
  };
}
