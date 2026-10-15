package com.hotelcaciquebolivar.reservas.service.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.caja.AbrirTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.CerrarTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.ConciliacionResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.GastoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.MovimientoResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.TurnoResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;

/**
 * Interfaz que define las operaciones de gestión de caja del sistema hotelero.
 * Proporciona funcionalidades para apertura y cierre de turnos, registro de gastos,
 * movimientos por pagos, reembolsos y ventas de productos.
 * Los movimientos se registran siempre en la tabla movimientos_caja,
 * asociados a un turno si hay uno activo, o con turno nulo si no lo hay.
 */
public interface CajaService {

    /**
     * Abre un nuevo turno de caja con el monto inicial especificado.
     *
     * @param cajaNombre nombre de la caja donde se abrirá el turno
     * @param dto objeto que contiene los datos para la apertura del turno (monto inicial)
     * @param email email del usuario que abre el turno
     * @return el turno abierto con los datos iniciales
     */
    TurnoResponse abrirTurno(String cajaNombre, AbrirTurnoRequest dto, String email);

    /**
     * Cierra el turno de caja activo con los totales calculados.
     *
     * @param cajaNombre nombre de la caja donde se cerrará el turno
     * @param dto objeto que contiene los datos para el cierre del turno
     * @param email email del usuario que cierra el turno
     * @return el turno cerrado con los totales finales
     */
    TurnoResponse cerrarTurno(String cajaNombre, CerrarTurnoRequest dto, String email);

    /**
     * Obtiene el turno de caja actualmente activo para la caja especificada.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @return el turno activo actual
     */
    TurnoResponse obtenerTurnoActivo(String cajaNombre);

    /**
     * Obtiene el historial de turnos de caja de forma paginada.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene el historial de turnos
     */
    PageResponse<TurnoResponse> obtenerHistorial(String cajaNombre, Pageable pageable);

    /**
     * Registra un gasto en la caja. Si hay turno activo se asocia al mismo,
     * si no queda con turno nulo.
     *
     * @param cajaNombre nombre de la caja donde se registrará el gasto
     * @param dto objeto que contiene los datos del gasto a registrar
     * @param email email del usuario que registra el gasto
     * @return el movimiento de gasto registrado
     */
    MovimientoResponse registrarGasto(String cajaNombre, GastoRequest dto, String email);

    /**
     * Cuadra las cajas del hotel en un período.
     *
     * @param inicio fecha y hora de inicio del período, inclusive
     * @param fin fecha y hora de fin del período, inclusive
     * @return ingresos y gastos por caja, con los totales acumulados
     */
    ConciliacionResponse conciliar(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Registra un movimiento de caja asociado a un pago realizado.
     * Si hay turno activo se asocia al mismo, si no queda con turno nulo.
     *
     * @param pago objeto pago que generó el movimiento
     * @param email email del usuario que procesó el pago
     */
    void registrarMovimientoPorPago(Pago pago, String email);

    /**
     * Registra el movimiento de caja de una línea de desglose de un pago.
     * A diferencia de {@link #registrarMovimientoPorPago}, que imputa el pago
     * completo a recepción, este método recibe el monto ya asignado a una caja y
     * el nombre de esa caja, de modo que un pago repartido entre recepción y
     * tienda genere un movimiento en cada una.
     *
     * <p>Solo actúa sobre pagos en efectivo completados: los demás métodos de pago
     * no ingresan físicamente a caja. Si el movimiento no puede registrarse se
     * deja traza en el log y se devuelve, sin propagar el error ni tragar la causa.</p>
     *
     * @param pago pago al que pertenece la línea
     * @param monto monto asignado a la caja en esta línea
     * @param cajaNombre nombre de la caja de destino
     * @param email email del usuario que procesó el pago
     */
    void registrarMovimientoPorPagoDesglose(Pago pago, BigDecimal monto, String cajaNombre, String email);

    /**
     * Registra un movimiento de caja asociado a un reembolso realizado.
     * Si hay turno activo se asocia al mismo, si no queda con turno nulo.
     *
     * @param pago objeto pago que generó el reembolso
     * @param email email del usuario que procesó el reembolso
     */
    void registrarMovimientoPorReembolso(Pago pago, String email);

    /**
     * Registra un movimiento de caja asociado a la venta de un producto.
     * Si hay turno activo se asocia al mismo, si no queda con turno nulo.
     *
     * @param productoId identificador del producto vendido
     * @param productoNombre nombre del producto vendido
     * @param cantidad cantidad de unidades vendidas
     * @param montoTotal monto total de la venta
     * @param metodoPago método de pago utilizado
     * @param email email del usuario que registró la venta
     * @param cajaNombre nombre de la caja donde se registró la venta
     */
    void registrarMovimientoPorVenta(Long productoId, String productoNombre, int cantidad,
            BigDecimal montoTotal, MetodoPago metodoPago, String email, String cajaNombre);

    /**
     * Cuenta la cantidad de movimientos sin turno asociado en la caja especificada.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @return cantidad de movimientos sin turno
     */
    long countMovimientosSinTurno(String cajaNombre);

    /**
     * Calcula la suma de los montos de los movimientos sin turno asociado en la caja.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @return suma total de los montos sin turno
     */
    BigDecimal sumMovimientosSinTurno(String cajaNombre);

    /**
     * Obtiene los movimientos sin turno asociado para la caja especificada.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @return lista de movimientos sin turno
     */
    List<MovimientoResponse> listarMovimientosSinTurno(String cajaNombre);

    /**
     * Obtiene todos los movimientos de la caja, incluyendo los de turnos cerrados.
     *
     * @param cajaNombre nombre de la caja a consultar
     * @return lista de todos los movimientos de la caja
     */
    List<MovimientoResponse> listarTodosLosMovimientos(String cajaNombre);
}
