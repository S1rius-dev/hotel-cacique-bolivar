package com.hotelcaciquebolivar.reservas.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DineroUtilTest {

    @Test
    void redondear_conCentavos_deberiaRedondearHaciaArribaALibre() {
        assertEquals(new BigDecimal("264500"), DineroUtil.redondear(new BigDecimal("264500.04")));
        assertEquals(new BigDecimal("264501"), DineroUtil.redondear(new BigDecimal("264500.50")));
        assertEquals(new BigDecimal("264500"), DineroUtil.redondear(new BigDecimal("264500.49")));
    }

    @Test
    void redondear_conValorEntero_deberiaMantenerlo() {
        assertEquals(new BigDecimal("600"), DineroUtil.redondear(new BigDecimal("600")));
        assertEquals(BigDecimal.ZERO, DineroUtil.redondear(BigDecimal.ZERO));
    }

    @Test
    void redondear_conNulo_deberiaRetornarCero() {
        assertEquals(BigDecimal.ZERO, DineroUtil.redondear(null));
    }
}
