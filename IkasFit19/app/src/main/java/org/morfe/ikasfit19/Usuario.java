package org.morfe.ikasfit19;

import java.util.Date;

public class Usuario {

    private long pasosTotales;
    private long pasosParcial;
    private Date fecha;
    private String id;


    public Usuario() {
    }

    public long getPasosTotales() {
        return pasosTotales;
    }

    public void setPasosTotales(long pasosTotales) {
        this.pasosTotales = pasosTotales;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPasosParcial() {
        return pasosParcial;
    }

    public void setPasosParcial(long pasosParcial) {
        this.pasosParcial = pasosParcial;
    }
}
