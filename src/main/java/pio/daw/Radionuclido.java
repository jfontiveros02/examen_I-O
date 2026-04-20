package pio.daw;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Radionuclido {

    private String id;
    private String isotopo;
    private double masa;
    private double actividadEspecificaInicial;
    private LocalDateTime fechaEntrega;

    public Radionuclido(String id, String isotopo, Double actividadEspecificaInicial,
                        Double masa, LocalDateTime fechaEntrega) {
        this.id = id;
        this.isotopo = isotopo;
        this.actividadEspecificaInicial = actividadEspecificaInicial;
        this.masa = masa;
        this.fechaEntrega = fechaEntrega;
    }

    public String getId() {
        return id;
    }

    public String getIsotopo() {
        return isotopo;
    }

    public Double getMasa() {
        return masa;
    }

    public Double getActividadEspecificaInicial() {
        return actividadEspecificaInicial;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public double getActividadInicial() {
        return actividadEspecificaInicial * masa;
    }

    public double actividad(LocalDateTime fecha) {
        double semivida = Utilidades.semividas.get(isotopo);
        double tiempo = ChronoUnit.SECONDS.between(fechaEntrega, fecha);
        return getActividadInicial() * Math.exp(-Math.log(2) / semivida * tiempo);
    }

    public double porcentajeActividad(LocalDateTime fecha) {
        return actividad(fecha) / getActividadInicial();
    }

    public LocalDateTime getFechaSegura() {
        double semivida = Utilidades.semividas.get(isotopo);
        double tMax = semivida;

        while (porcentajeActividad(fechaEntrega.plusSeconds((long) tMax)) >= 0.1) {
            tMax *= 2;
        }

        double tSeg = Utilidades.biseccion(
                t -> porcentajeActividad(fechaEntrega.plusSeconds((long) t)),
                0.1,
                0,
                tMax
        );

        return fechaEntrega.plusSeconds((long) tSeg);
    }

    public double getCosteRefrigeracion() {
        LocalDateTime fechaSegura = getFechaSegura();
        double tSeg = ChronoUnit.SECONDS.between(fechaEntrega, fechaSegura);
        double eDesintegracion = Utilidades.energias.get(isotopo);

        FuncionUnivariable pEle =
                t -> actividad(fechaEntrega.plusSeconds((long) t)) * eDesintegracion / 4.0;

        double eGastada = Utilidades.integrar(pEle, 0, tSeg);
        return eGastada / 3_600_000.0;
    }

    public String toFactura() {
        return "ID: " + id +
               "\nIsótopo: " + isotopo +
               "\nMasa: " + masa +
               "\nActividad inicial: " + getActividadInicial() +
               "\nFecha de entrega: " + fechaEntrega +
               "\nFecha segura: " + getFechaSegura() +
               "\nCoste de refrigeración: " + getCosteRefrigeracion() + " €";
    }
}
