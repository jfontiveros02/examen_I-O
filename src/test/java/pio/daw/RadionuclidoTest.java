package pio.daw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests progresivos para la clase Radionuclido.
 * Están organizados por tarea: ejecuta los tests de cada tarea
 * cuando hayas implementado el método correspondiente.
 *
 * Isótopo de referencia: Co60 (semivida 5.27 años — resultados rápidos y fechas cercanas)
 *   - Actividad específica : 20 000 000 000 000 Bq/kg (20 TBq/kg)
 *   - Masa                 : 75 kg
 *   - Actividad inicial    : 1 500 000 000 000 000 Bq (1.5e15 Bq)
 *   - Semivida             : 5.27 años ≈ 166 220 040 s
 *   - Fecha de entrega     : 2026-01-01T00:00
 *   - Fecha segura         : ~2043 (17.5 años después)
 */
public class RadionuclidoTest {

    private static final double TOLERANCIA_ACTIVIDAD  = 1e9;   // ±1 GBq
    private static final double TOLERANCIA_PORCENTAJE = 1e-4;  // ±0.01%

    private Radionuclido co60;
    private LocalDateTime fechaEntrega;
    private double semividaCo60;

    @BeforeEach
    void setUp() {
        fechaEntrega  = LocalDateTime.of(2026, 1, 1, 0, 0);
        semividaCo60  = Utilidades.semividas.get("Co60");
        co60 = new Radionuclido("2", "Co60", 20_000_000_000_000.0, 75.0, fechaEntrega);
    }

    // =========================================================================
    // TAREA 1 — Constructor y getters
    // =========================================================================

    @Test
    void tarea1_getId() {
        assertEquals("2", co60.getId());
    }

    @Test
    void tarea1_getIsotopo() {
        assertEquals("Co60", co60.getIsotopo());
    }

    @Test
    void tarea1_getMasa() {
        assertEquals(75.0, co60.getMasa());
    }

    @Test
    void tarea1_getActividadEspecificaInicial() {
        assertEquals(20_000_000_000_000.0, co60.getActividadEspecificaInicial());
    }

    @Test
    void tarea1_getFechaEntrega() {
        assertEquals(fechaEntrega, co60.getFechaEntrega());
    }

    @Test
    void tarea1_getActividadInicial_esProductoDeEspecificaYMasa() {
        // 20e12 Bq/kg × 75 kg = 1.5e15 Bq
        double esperado = 20_000_000_000_000.0 * 75.0;
        assertEquals(esperado, co60.getActividadInicial(), TOLERANCIA_ACTIVIDAD);
    }

    // =========================================================================
    // TAREA 2 — actividad(LocalDateTime fecha)
    // =========================================================================

    @Test
    void tarea2_actividad_enFechaEntregaEqualsActividadInicial() {
        // A t=0, la actividad debe ser igual a la inicial
        double a0 = co60.getActividadInicial();
        assertEquals(a0, co60.actividad(fechaEntrega), TOLERANCIA_ACTIVIDAD);
    }

    @Test
    void tarea2_actividad_trasUnaSemivida_esMitad() {
        // Tras exactamente una semivida, queda el 50% de la actividad inicial
        LocalDateTime trasSemivida = fechaEntrega.plusSeconds((long) semividaCo60);
        double esperado = co60.getActividadInicial() * 0.5;
        assertEquals(esperado, co60.actividad(trasSemivida), TOLERANCIA_ACTIVIDAD);
    }

    @Test
    void tarea2_actividad_trasDoseSemividas_esCuartoInicial() {
        // Tras dos semividas queda el 25%
        LocalDateTime trasDos = fechaEntrega.plusSeconds((long) (2 * semividaCo60));
        double esperado = co60.getActividadInicial() * 0.25;
        assertEquals(esperado, co60.actividad(trasDos), TOLERANCIA_ACTIVIDAD);
    }

    @Test
    void tarea2_actividad_decrece_con_el_tiempo() {
        LocalDateTime masAtras  = fechaEntrega.plusSeconds((long) semividaCo60);
        LocalDateTime masAdelante = fechaEntrega.plusSeconds((long) (2 * semividaCo60));
        assertTrue(co60.actividad(masAtras) > co60.actividad(masAdelante));
    }

    // =========================================================================
    // TAREA 3 — porcentajeActividad(LocalDateTime fecha)
    // =========================================================================

    @Test
    void tarea3_porcentaje_enFechaEntregaEsUno() {
        assertEquals(1.0, co60.porcentajeActividad(fechaEntrega), TOLERANCIA_PORCENTAJE);
    }

    @Test
    void tarea3_porcentaje_trasUnaSemivida_esCincoPorCiento() {
        LocalDateTime trasSemivida = fechaEntrega.plusSeconds((long) semividaCo60);
        assertEquals(0.5, co60.porcentajeActividad(trasSemivida), TOLERANCIA_PORCENTAJE);
    }

    @Test
    void tarea3_porcentaje_estaEntreZeroYUno() {
        LocalDateTime fecha = fechaEntrega.plusSeconds((long) (3 * semividaCo60));
        double p = co60.porcentajeActividad(fecha);
        assertTrue(p > 0.0 && p < 1.0);
    }

    // =========================================================================
    // TAREA 4 — getFechaSegura()
    // =========================================================================

    @Test
    void tarea4_fechaSegura_esDespusDeFechaEntrega() {
        assertTrue(co60.getFechaSegura().isAfter(fechaEntrega));
    }

    @Test
    void tarea4_fechaSegura_actividadEsAproximadamenteDiezPorCiento() {
        // En la fecha segura el porcentaje debe estar muy cerca de 0.10
        LocalDateTime fs = co60.getFechaSegura();
        assertEquals(0.1, co60.porcentajeActividad(fs), 1e-3);
    }

    @Test
    void tarea4_fechaSegura_Co60_estaEntre2040y2050() {
        // Co60 tiene semivida de 5.27 años → seguro en ~17.5 años desde 2026 → ~2043
        int anyo = co60.getFechaSegura().getYear();
        assertTrue(anyo >= 2040 && anyo <= 2050,
            "Se esperaba un año entre 2040 y 2050, pero fue: " + anyo);
    }

    // =========================================================================
    // TAREA 5 — getCosteRefrigeracion()
    // =========================================================================

    @Test
    void tarea5_coste_esPositivo() {
        assertTrue(co60.getCosteRefrigeracion() > 0);
    }

    @Test
    void tarea5_coste_Co60_estaEnRangoEsperado() {
        // Con los parámetros del ejemplo se espera ~10 000 € (±20%)
        double coste = co60.getCosteRefrigeracion();
        assertTrue(coste > 8_000 && coste < 12_000,
            "Coste fuera de rango: " + coste + " €");
    }

    @Test
    void tarea5_masaMayor_implicaMayorCoste() {
        // El mismo isótopo con más masa debe costar más
        Radionuclido co60Grande = new Radionuclido("X", "Co60",
                20_000_000_000_000.0, 150.0, fechaEntrega);
        assertTrue(co60Grande.getCosteRefrigeracion() > co60.getCosteRefrigeracion());
    }

    // =========================================================================
    // TAREA 6 — toFactura()
    // =========================================================================

    @Test
    void tarea6_factura_contieneId() {
        assertTrue(co60.toFactura().contains("#2"));
    }

    @Test
    void tarea6_factura_contieneIsotopo() {
        assertTrue(co60.toFactura().contains("Co60"));
    }

    @Test
    void tarea6_factura_contieneMasa() {
        assertTrue(co60.toFactura().contains("75"));
    }

    @Test
    void tarea6_factura_contieneFechaEntrega() {
        assertTrue(co60.toFactura().contains("2026"));
    }

    @Test
    void tarea6_factura_contieneFechaSegura() {
        // La fecha segura es ~2043
        assertTrue(co60.toFactura().contains("204"));
    }

    @Test
    void tarea6_factura_contieneLineasSeparadoras() {
        String factura = co60.toFactura();
        assertTrue(factura.contains("========================================"));
        assertTrue(factura.contains("----------------------------------------"));
    }
}
