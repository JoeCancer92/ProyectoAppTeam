package com.example.proyectoappteam.clases;

/**
 * Clase modelo para la tabla 'consejos' en Backendless.
 * Esta clase representa cada consejo que se muestra en la aplicacion.
 */
public class Consejos {

    // Backendless asigna automaticamente el objectId y la version
    private String objectId;
    private int version;
    private String descripcion;
    private int orden;

    // Se requiere un constructor publico vacio para el deserializador de Backendless
    public Consejos() {
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
