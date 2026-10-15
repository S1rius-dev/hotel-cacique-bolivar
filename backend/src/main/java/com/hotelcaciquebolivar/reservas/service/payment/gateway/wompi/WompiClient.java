package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import java.math.BigDecimal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiData;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;

/**
 * Cliente HTTP para la API de Wompi.
 * Consulta transacciones por ID o referencia, y gestiona reembolsos.
 * Configura automáticamente la URL base según el entorno (sandbox/producción)
 * y valida la coherencia entre la URL base y la clave pública.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class WompiClient {

    private final RestClient restClient;

    /**
     * Construye el cliente REST configurando la URL base, autenticación Bearer
     * y validando la coherencia sandbox/producción.
     *
     * @param properties propiedades de configuración de pagos Wompi
     */
    public WompiClient(PaymentProperties properties) {
        String effectiveBaseUrl = properties.getWompi().getEffectiveBaseUrl();
        if (properties.getWompi().getBaseUrl() != null
                && !properties.getWompi().getBaseUrl().isBlank()
                && effectiveBaseUrl.equals("https://sandbox.wompi.co/v1")
                && properties.getWompi().getBaseUrl().contains("production.wompi.co")) {
            org.slf4j.LoggerFactory.getLogger(WompiClient.class)
                    .warn("WOMPI_BASE_URL apunta a production pero publicKey es test; usando sandbox automáticamente");
        }
        this.restClient = RestClient.builder()
                .baseUrl(effectiveBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getWompi().getPrivateKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Consulta una transacción en Wompi por su ID.
     *
     * @param idTransaccion ID de la transacción en Wompi
     * @return datos de la transacción o null si no se encuentra
     * @throws PaymentGatewayException si Wompi retorna un error HTTP
     */
    public WompiData consultarTransaccion(String idTransaccion) {
        try {
            WompiTransaccionResponse response = restClient.get()
                    .uri("/transactions/{id}", idTransaccion)
                    .retrieve()
                    .body(WompiTransaccionResponse.class);
            return response != null ? response.data() : null;
        } catch (HttpStatusCodeException ex) {
            throw new PaymentGatewayException(
                    "Wompi no pudo consultar la transacci\u00f3n " + idTransaccion + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        }
    }

    /**
     * Busca transacción Wompi por reference (nuestra referenciaExterna).
     * Útil para reconciliación cuando no tenemos el id de Wompi.
     *
     * @param referencia referencia externa de la transacción
     * @return primera transacción encontrada o null si no existe
     * @throws PaymentGatewayException si Wompi retorna un error HTTP
     */
    public WompiData consultarPorReferencia(String referencia) {
        try {
            WompiTransaccionesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/transactions")
                            .queryParam("reference", referencia).build())
                    .retrieve()
                    .body(WompiTransaccionesResponse.class);
            if (response != null && response.data() != null && !response.data().isEmpty()) {
                return response.data().get(0);
            }
            return null;
        } catch (HttpStatusCodeException ex) {
            throw new PaymentGatewayException(
                    "Wompi no pudo consultar por referencia " + referencia + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        }
    }

    /**
     * Solicita el reembolso de una transacción en Wompi.
     * Convierte el monto a centavos antes de enviarlo.
     *
     * @param idTransaccion ID de la transacción a reembolsar
     * @param monto monto a reembolsar en la moneda original
     * @throws PaymentGatewayException si Wompi rechaza el reembolso
     */
    public void reembolsar(String idTransaccion, BigDecimal monto) {
        long centavos = monto.multiply(BigDecimal.valueOf(100)).longValueExact();
        WompiReembolso body = new WompiReembolso(centavos);
        try {
            restClient.post()
                    .uri("/transactions/{id}/refund", idTransaccion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException ex) {
            throw new PaymentGatewayException(
                    "Wompi rechaz\u00f3 el reembolso de " + idTransaccion + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        }
    }

    private record WompiTransaccionResponse(WompiData data) {
    }

    private record WompiTransaccionesResponse(java.util.List<WompiData> data) {
    }

    private record WompiReembolso(long amountInCents) {
    }
}
