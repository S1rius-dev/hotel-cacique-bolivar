package com.hotelcaciquebolivar.reservas.repository.pago;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;

/**
 * Repositorio para la gestión de transacciones de pago con pasarelas de pago externas.
 * Permite buscar transacciones por referencia externa, clave de idempotencia y reserva.
 */
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {

    /**
     * Busca una transacción por referencia externa y tipo de pasarela de pago.
     *
     * @param referenciaExterna referencia proporcionada por la pasarela de pago
     * @param gateway tipo de pasarela de pago (WOMPI, STRIPE, etc.)
     * @return Optional con la transacción encontrada o vacío si no existe
     */
    Optional<TransaccionPago> findByReferenciaExternaAndGateway(
            String referenciaExterna, PaymentGatewayType gateway);

    /**
     * Busca una transacción por su clave de idempotencia.
     *
     * @param idempotenciaKey clave única de idempotencia para prevenir transacciones duplicadas
     * @return Optional con la transacción encontrada o vacío si no existe
     */
    Optional<TransaccionPago> findByIdempotenciaKey(String idempotenciaKey);

    /**
     * Obtiene las transacciones de pago de una reserva específica, ordenadas por fecha de creación descendente.
     *
     * @param reservaId identificador de la reserva
     * @return lista de transacciones de la reserva
     */
    List<TransaccionPago> findByReservaIdOrderByCreatedAtDesc(Long reservaId);

    /**
     * Busca la transacción más reciente de una reserva con un estado específico de pasarela.
     *
     * @param reservaId identificador de la reserva
     * @param estadoPasarela estado de la transacción en la pasarela (APPROVED, DECLINED, PENDING, etc.)
     * @return Optional con la transacción más reciente o vacío si no existe
     */
    Optional<TransaccionPago> findTopByReservaIdAndEstadoPasarelaOrderByCreatedAtDesc(
            Long reservaId, PaymentStatus estadoPasarela);

    /**
     * Verifica si existe una transacción con la clave de idempotencia especificada.
     *
     * @param idempotenciaKey clave de idempotencia a verificar
     * @return true si existe una transacción con esa clave, false en caso contrario
     */
    boolean existsByIdempotenciaKey(String idempotenciaKey);
}
