package com.example.proyectoappteam.clases;

import java.util.Date;

/**
 * Clase modelo para la tabla 'comentarios' en Backendless.
 * Incluye la relación a la Publicación y la fecha de creación personalizada.
 */
public class Comentarios {

    // Campos de metadatos de Backendless
    private String objectId;
    private String ownerId;
    private Date created;
    private Date updated;

    // Campos de la tabla 'comentarios'
    private String texto;
    private Date fechaCreacion;

    // Relación a la tabla 'publicaciones' (Relación 1:1)
    // 🚨 CORRECCIÓN FINAL: Debe ser el tipo de clase Publicaciones para que Backendless
    // pueda serializarlo correctamente como una relación, asumiendo que el mapeo
    // explícito en ProyectoAppTeam.java ya fue añadido.
    private Publicaciones publicacion;

    // Constructor vacío (necesario para Backendless)
    public Comentarios() {
    }

    // --- Getters y Setters ---

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Publicaciones getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicaciones publicacion) {
        this.publicacion = publicacion;
    }
}