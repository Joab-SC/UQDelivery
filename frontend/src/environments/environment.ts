// Configuración del front. Todas las URLs de la API se arman a partir de apiUrl.
// Ejemplo en un servicio: this.http.get(`${environment.apiUrl}/health`)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
};
