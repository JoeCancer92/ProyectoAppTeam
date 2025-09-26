package com.example.proyectoappteam.clases;

import java.util.Date;

/**
 * Clase de Mapeo para la tabla 'calificaciones' en Backendless.
 */
public class Calificaciones {

    // Columnas de la tabla 'calificaciones'
    private String objectId;
    private String ownerId;
    private int puntuacion; // Mapea la columna 'puntuacion' (int)
    private Publicaciones publicacion; // Mapea la relación 'publicacion' (rel 1:1)
    private Date created;
    private Date updated;

    // Constructor vacío (requerido por el SDK de Backendless)
    public Calificaciones() {
    }

    // ===================================================================
    // Getters y Setters
    // ===================================================================

    // Puntuacion
    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    // Relación con Publicaciones
    public Publicaciones getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicaciones publicacion) {
        this.publicacion = publicacion;
    }

    // ID del objeto (objectId)
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    // Usuario que califica (ownerId)
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // Fechas automáticas
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
}