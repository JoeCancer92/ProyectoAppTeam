package com.example.proyectoappteam.clases;

import java.util.Date; // IMPORTANTE: Necesario para manejar las fechas de Backendless

/**
 * Clase de Mapeo para la tabla 'Publicaciones' en Backendless.
 */
public class Publicaciones {

    // Propiedades internas del objeto (deben coincidir con el Backendless Schema)
    private String objectId;
    private String descripcion;
    private String ubicacion;
    private String ownerId;
    private String fotos;
    private Boolean esUrgente;
    private Double latitud;
    private Double longitud;

    private String categoria;

    // *******************************************************************
    // PROPIEDADES DE FECHA/HORA AUTOMÁTICAS DE BACKENDLESS (DATETIME)
    // *******************************************************************
    private Date created; // Columna 'created' de Backendless
    private Date updated; // Columna 'updated' de Backendless
    // *******************************************************************

    // Constructor vacío (requerido por el SDK de Backendless)
    public Publicaciones() {
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA FECHA (created)
    // ===================================================================

    // Necesario para que PublicacionAdapter pueda leer la fecha.
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA LA CATEGORÍA
    // ===================================================================

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA LATITUD Y LONGITUD
    // ===================================================================

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(double selectedLatitud) {
        this.latitud = selectedLatitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(double selectedLongitud) {
        this.longitud = selectedLongitud;
    }

    // ===================================================================
    // OTROS GETTERS Y SETTERS
    // ===================================================================

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
}