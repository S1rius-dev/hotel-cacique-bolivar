package com.hotelcaciquebolivar.reservas.service.pago;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.pago.ReciboSequence;
import com.hotelcaciquebolivar.reservas.repository.pago.ReciboSequenceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la generación de códigos de recibo consecutivos para pagos en efectivo.
 * Cada año se reinicia la secuencia. El formato es "REC-{año}-{secuencia de 6 dígitos}".
 * Utiliza bloqueo pesimista (SELECT FOR UPDATE) para garantizar atomicidad.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@RequiredArgsConstructor
public class ReciboSequenceService {

    private final ReciboSequenceRepository sequenceRepository;

    /**
     * Genera el siguiente código de recibo consecutivo para el año actual.
     * Incrementa el contador de la secuencia anual de forma atómica.
     *
     * @return código de recibo en formato "REC-{año}-{secuencia}" (ej. "REC-2026-000001")
     */
    @Transactional
    public String nextRecibo() {
        int year = LocalDate.now(ZoneId.of("America/Bogota")).getYear();
        ReciboSequence seq = sequenceRepository.findByYearForUpdate(year)
                .orElseGet(() -> ReciboSequence.builder().year(year).lastNumber(0L).build());
        long next = seq.getLastNumber() + 1;
        seq.setLastNumber(next);
        sequenceRepository.save(seq);
        return String.format("REC-%d-%06d", year, next);
    }
}
