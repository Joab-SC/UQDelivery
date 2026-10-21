package co.edu.uniquindio.uqdelivery.config.exception;

import co.edu.uniquindio.uqdelivery.dtos.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private record Formulario(String nombre, String correo) {
    }

    @Test
    void validacionDevuelve400ConElErrorDeCadaCampo() throws NoSuchMethodException {
        var resultado = new BeanPropertyBindingResult(new Formulario(null, null), "formulario");
        resultado.rejectValue("nombre", "NotBlank", "El nombre es obligatorio");
        resultado.rejectValue("correo", "Email", "El correo no es válido");
        var parametro = new MethodParameter(getClass().getDeclaredMethod("validacionDevuelve400ConElErrorDeCadaCampo"), -1);

        ResponseEntity<ApiError> respuesta =
                handler.validacion(new MethodArgumentNotValidException(parametro, resultado));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody().message()).isEqualTo("Hay campos inválidos");
        assertThat(respuesta.getBody().errors()).containsExactly(
                entry("nombre", "El nombre es obligatorio"),
                entry("correo", "El correo no es válido"));
    }

    @Test
    void reglaDeNegocioDevuelve400ConSuMensaje() {
        var respuesta = handler.reglaNegocio(
                new ReglaNegocioException("El paquete no cumple con las políticas de tamaño"));

        assertThat(respuesta.getBody())
                .isEqualTo(new ApiError(400, "El paquete no cumple con las políticas de tamaño"));
    }

    @Test
    void recursoNoEncontradoDevuelve404() {
        var respuesta = handler.noEncontrado(new RecursoNoEncontradoException("Usuario no encontrado"));

        assertThat(respuesta.getBody()).isEqualTo(new ApiError(404, "Usuario no encontrado"));
    }

    @Test
    void conflictoDevuelve409() {
        var respuesta = handler.conflicto(new ConflictoException("El correo ya está registrado"));

        assertThat(respuesta.getBody()).isEqualTo(new ApiError(409, "El correo ya está registrado"));
    }

    @Test
    void credencialesIncorrectasDevuelve401ConMensajeGenerico() {
        var respuesta = handler.credencialesIncorrectas(new BadCredentialsException("Bad credentials"));

        assertThat(respuesta.getBody()).isEqualTo(new ApiError(401, "Correo o contraseña incorrectos"));
    }

    @Test
    void accesoDenegadoDevuelve403() {
        var respuesta = handler.accesoDenegado(new AccessDeniedException("Access Denied"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void errorInesperadoDevuelve500SinExponerElDetalle() {
        var respuesta = handler.errorInesperado(new IllegalStateException("detalle interno secreto"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody().message()).doesNotContain("secreto");
        assertThat(respuesta.getBody().errors()).isNull();
    }
}
