package com.example.proyectoappteam.clases;

public class Publicaciones {

    private String objectId;
    private String descripcion;
    private String ubicacion;
    private String ownerId;
    private String fotos; // Campo para la URL de las fotos
    private Boolean esUrgente; // Campo para el estado de urgencia

    // Getters y Setters para cada propiedad
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

    public Boolean getEsUrgente() {
        return esUrgente;
    }

    public void setEsUrgente(Boolean esUrgente) {
        this.esUrgente = esUrgente;
    }

    public void setCategoria(String categoria) {
    }

    public void setLongitud(double selectedLongitud) {
    }

    public void setLatitud(double selectedLatitud) {
    }
}