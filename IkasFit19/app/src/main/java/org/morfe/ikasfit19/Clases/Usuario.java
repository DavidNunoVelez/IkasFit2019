package org.morfe.ikasfit19.Clases;

public class Usuario {

    private String fecha;
    private int pasosTotales;
    private String id;

    public Usuario() {
    }

    public Usuario(String fecha, int pasosTotales,String id) {
        this.fecha = fecha;
        this.pasosTotales = pasosTotales;
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getPasosTotales() {
        return pasosTotales;
    }

    public void setPasosTotales(int pasosTotales) {
        this.pasosTotales = pasosTotales;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
