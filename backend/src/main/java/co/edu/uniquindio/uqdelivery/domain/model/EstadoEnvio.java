package co.edu.uniquindio.uqdelivery.domain.model;

/**
 * Estados por los que pasa un envío. Un envío no se puede cancelar desde EN_CAMINO ni ENTREGADO.
 */
public enum EstadoEnvio {
    PENDIENTE,
    ASIGNADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}
