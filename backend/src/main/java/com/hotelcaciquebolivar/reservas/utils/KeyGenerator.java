package com.hotelcaciquebolivar.reservas.utils;

import java.util.Base64;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para generar claves secretas compatibles con HMAC-SHA256.
 * <p>
 * Esta clase contiene un método {@link #main(String[])} que genera una clave secreta
 * aleatoria utilizando la librería JJWT y la imprime en formato Base64 por consola.
 * <p>
 * La clave generada está diseñada para ser utilizada como {@code JWT_SECRET} en el
 * archivo de configuración {@code .env}, permitiendo la firma y verificación de
 * tokens JWT con el algoritmo HMAC-SHA256.
 * <p>
 * Esta clase se ejecuta de forma independiente y no forma parte del ciclo de vida
 * de la aplicación Spring Boot.
 */
public class KeyGenerator {

    /**
     * Punto de entrada que genera una clave secreta HMAC-SHA256 y la imprime en Base64.
     * <p>
     * El método genera internamente una clave secreta de 256 bits (32 bytes) usando
     * {@link Keys#secretKeyFor(SignatureAlgorithm)} con el algoritmo HS256, la codifica
     * en Base64 y la escribe en {@code System.out}.
     *
     * @param args argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
