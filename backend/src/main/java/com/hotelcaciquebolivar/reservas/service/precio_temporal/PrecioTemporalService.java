package com.hotelcaciquebolivar.reservas.service.precio_temporal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

/**
 * Interfaz del servicio de gestión de precios temporales para habitaciones.
 * Permite definir rangos de fechas con precios especiales por tipo de habitación
 * y resolver el precio vigente para una fecha específica, considerando los precios
 * temporales como prioridad sobre los precios base.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PrecioTemporalService {

    /**
     * Crea un nuevo registro de precio temporal para un tipo de habitación.
     * Valida que no exista solapamiento de fechas con registros existentes.
     *
     * @param dto datos del precio temporal a crear
     * @return el precio temporal creado
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException si las fechas son inválidas o existen solapamientos
     */
    PrecioTemporalResponse crear(PrecioTemporalRequest dto);

    /**
     * Busca un precio temporal por su identificador único.
     *
     * @param id identificador del precio temporal
     * @return el precio temporal encontrado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    PrecioTemporalResponse buscarPorId(Long id);

    /**
     * Lista los precios temporales filtrados por tipo de habitación.
     * Si tipoId es null, retorna todos los precios temporales ordenados por fecha de inicio.
     *
     * @param tipoId identificador del tipo de habitación; puede ser null para listar todos
     * @return lista de precios temporales
     */
    List<PrecioTemporalResponse> listar(Long tipoId);

    /**
     * Actualiza un precio temporal existente.
     * Valida que no existan solapamientos excluyendo el propio registro.
     *
     * @param id  identificador del precio temporal a actualizar
     * @param dto datos actualizados
     * @return el precio temporal actualizado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException     si existen solapamientos de fechas
     */
    PrecioTemporalResponse actualizar(Long id, PrecioTemporalRequest dto);

    /**
     * Elimina un precio temporal por su identificador.
     *
     * @param id identificador del precio temporal a eliminar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    void eliminar(Long id);

    /**
     * Resuelve el precio por noche vigente para un tipo de habitaci��n y fecha espec��fica.
     * Busca un precio temporal que cubra la fecha indicada; si no existe, retorna el precio
     * base del tipo de habitaci��n.
     *
     * @param tipo  tipo de habitaci��n
     * @param fecha fecha para la cual se desea resolver el precio
     * @return el precio por noche vigente; BigDecimal.ZERO si tipo o fecha son null
     */
    BigDecimal resolverPrecioNoche(TipoHabitacion tipo, LocalDate fecha);

    /**
     * Resuelve el precio por noche con IVA vigente (valor impuesto almacenado).
     * Prioriza precio temporal (precioNocheIva) si existe y cubre la fecha.
     * Si no, retorna precioConIva del tipo de habitación. Si este es null, retorna precioBase.
     *
     * @param tipo  tipo de habitación
     * @param fecha fecha para la cual se desea resolver el precio
     * @return precio por noche con IVA vigente
     */
    default BigDecimal resolverPrecioNocheConIva(TipoHabitacion tipo, LocalDate fecha) {
        return resolverPrecioNoche(tipo, fecha);
    }

    /**
     * Resuelve el precio por persona extra vigente para un tipo de habitación y fecha específica.
     * Busca un precio temporal que cubra la fecha indicada; si no existe, retorna el precio
     * por persona extra del tipo de habitación.
     *
     * @param tipo  tipo de habitación
     * @param fecha fecha para la cual se desea resolver el precio
     * @return el precio por persona extra vigente; BigDecimal.ZERO si tipo o fecha son null
     */
    BigDecimal resolverPrecioPersonaExtra(TipoHabitacion tipo, LocalDate fecha);

    /**
     * Calcula el total a pagar por todas las noches en un rango de fechas (check-in a check-out).
     * Itera día por día y aplica el precio temporal vigente o el fallback/base para cada noche.
     *
     * @param tipo               tipo de habitación
     * @param precioNocheFallback precio por noche de respaldo si no hay precio temporal; puede ser null
     * @param checkIn            fecha de entrada
     * @param checkOut           fecha de salida
     * @return el costo total de las noches; BigDecimal.ZERO si los parámetros son inválidos
     */
    BigDecimal calcularTotalNoches(
            TipoHabitacion tipo,
            BigDecimal precioNocheFallback,
            LocalDate checkIn,
            LocalDate checkOut);

    /**
     * Calcula el total a pagar por personas extra en un rango de fechas.
     * Itera día por día, aplica el precio temporal vigente o el fallback/base,
     * y multiplica por el número de personas extras para cada noche.
     *
     * @param tipo                     tipo de habitación
     * @param precioPersonaExtraFallback precio por persona extra de respaldo; puede ser null
     * @param personasExtras           cantidad de personas extras
     * @param checkIn                  fecha de entrada
     * @param checkOut                 fecha de salida
     * @return el costo total de personas extra; BigDecimal.ZERO si los parámetros son inválidos o personasExtras <= 0
     */
    BigDecimal calcularTotalPersonasExtra(
            TipoHabitacion tipo,
            BigDecimal precioPersonaExtraFallback,
            int personasExtras,
            LocalDate checkIn,
            LocalDate checkOut);
}