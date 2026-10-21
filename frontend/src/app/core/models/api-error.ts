// Forma de todas las respuestas de error del back (ApiError).
// En un subscribe: error: (err) => this.error.set(err.error.message)
export interface ApiError {
  status: number;
  message: string;
  /** Error de cada campo. Solo viene en errores de validación (400). */
  errors?: Record<string, string>;
}
