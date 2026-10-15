package com.hotelcaciquebolivar.reservas.dto.backup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para restaurar una copia en una base de datos nueva.
 *
 * <p>El nombre se valida aqui por forma y el servicio lo vuelve a validar contra la base
 * en uso. La validacion en el borde da un 400 claro al operador; la del servicio es la
 * que de verdad protege, porque no depende de que el endpoint sea el unico llamador.</p>
 *
 * @param baseDestino nombre de la base a crear; solo letras, digitos y guion bajo
 */
public record BackupClonRequest(

        @NotBlank(message = "El nombre de la base de destino es obligatorio")
        @Size(max = 64, message = "El nombre no puede superar los 64 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_]+$",
                message = "Solo se admiten letras, digitos y guion bajo")
        String baseDestino) {
}
