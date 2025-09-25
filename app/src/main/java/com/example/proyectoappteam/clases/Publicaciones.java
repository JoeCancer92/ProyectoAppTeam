package com.example.proyectoappteam.clases;

import java.util.Date;

// Esta clase representa una fila en la tabla 'publicaciones' de Backendless.
// Los nombres de las variables deben coincidir con los nombres de las columnas.
public class Publicaciones {

    // Columnas que creaste manualmente
    private String categoria;
    private String descripcion;
    private boolean esUrgente;
    private double latitud;
    private double longitud;
    private String ubicacion; // ¡Nuevo campo añadido!
    private String fotos; // Almacenará el JSON de las URLs de las fotos

    // Columnas que Backendless crea automáticamente
    private String objectId;
    private Date created;
    private String ownerId;
    private Date updated;

    // Constructor vacío requerido por Backendless
    public Publicaciones() {
    }

    // Getters y Setters
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsUrgente() {
        return esUrgente;
    }

    public void setEsUrgente(boolean esUrgente) {
        this.esUrgente = esUrgente;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    // Getter y Setter para el nuevo campo 'ubicacion'
    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}