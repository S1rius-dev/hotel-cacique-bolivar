package com.hotelcaciquebolivar.reservas.entity.caja;

import java.time.LocalTime;

/**
 * Enumeración que define los tipos de turno de caja disponibles en el hotel.
 * Cada tipo de turno tiene un rango de horas específico y un nombre para mostrar.
 * Incluye métodos estáticos para determinar el turno actual según la hora del día.
 */
public enum TipoTurno {

    /** Turno de mañana: 7:00 a 15:00. */
    MANANA("Mañana", LocalTime.of(7, 0), LocalTime.of(15, 0)),
    /** Turno de tarde: 15:00 a 22:00. */
    TARDE("Tarde", LocalTime.of(15, 0), LocalTime.of(22, 0)),
    /** Turno de noche: 22:00 a 7:00 (cruza medianoche). */
    NOCHE("Noche", LocalTime.of(22, 0), LocalTime.of(7, 0));

    /** Nombre para mostrar del turno. */
    private final String displayName;
    /** Hora de inicio del turno. */
    private final LocalTime horaInicio;
    /** Hora de fin del turno. */
    private final LocalTime horaFin;

    TipoTurno(String displayName, LocalTime horaInicio, LocalTime horaFin) {
        this.displayName = displayName;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Obtiene el nombre para mostrar del turno.
     *
     * @return nombre legible del turno
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene la hora de inicio del turno.
     *
     * @return hora de inicio
     */
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    /**
     * Obtiene la hora de fin del turno.
     *
     * @return hora de fin
     */
    public LocalTime getHoraFin() {
        return horaFin;
    }

    /**
     * Determina el tipo de turno actual según la hora del sistema.
     *
     * @return tipo de turno correspondiente a la hora actual
     */
    public static TipoTurno desdeAhora() {
        return desdeHora(LocalTime.now());
    }

    /**
     * Determina el tipo de turno según una hora específica.
     * Mañana: 7:00 <= hora &lt; 15:00
     * Tarde: 15:00 <= hora &lt; 22:00
     * Noche: fuera de los rangos anteriores (22:00 - 7:00)
     *
     * @param hora la hora a evaluar
     * @return tipo de turno correspondiente
     */
    public static TipoTurno desdeHora(LocalTime hora) {
        if (!hora.isBefore(LocalTime.of(7, 0)) && hora.isBefore(LocalTime.of(15, 0))) {
            return MANANA;
        }
        if (!hora.isBefore(LocalTime.of(15, 0)) && hora.isBefore(LocalTime.of(22, 0))) {
            return TARDE;
        }
        return NOCHE;
    }
}
