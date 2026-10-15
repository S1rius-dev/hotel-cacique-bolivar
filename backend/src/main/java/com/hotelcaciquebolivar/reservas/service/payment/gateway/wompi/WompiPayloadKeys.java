package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

/**
 * Constantes que definen las claves JSON utilizadas en los payloads
 * de notificaciones webhook de Wompi y en la construcción de firmas.
 *
 * @author Hotel Cacique Bolívar
 */
public final class WompiPayloadKeys {

    private WompiPayloadKeys() {
    }

    public static final String EVENT = "event";
    public static final String TRANSACTION_APPROVED = "transaction.updated";
    public static final String DATA = "data";
    public static final String TRANSACTION = "transaction";
    public static final String ID = "id";
    public static final String REFERENCE = "reference";
    public static final String STATUS = "status";
    public static final String CURRENCY = "currency";
    public static final String AMOUNT_IN_CENTS = "amount_in_cents";

    public static final String APPROVED = "APPROVED";
    public static final String PENDING = "PENDING";
    public static final String DECLINED = "DECLINED";
    public static final String VOIDED = "VOIDED";
    public static final String EXPIRED = "EXPIRED";
    public static final String ERROR = "ERROR";
}
