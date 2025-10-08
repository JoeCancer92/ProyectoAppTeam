package com.example.proyectoappteam.clases;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** Clase de Mapeo para la tabla 'calificaciones' en Backendless. */
public class Calificaciones implements Serializable {

    private String objectId;
    private String ownerId;
    private int puntuacion;
    private Publicaciones publicacion;  // relación 1:1 con Publicaciones
    private Date created;
    private Date updated;

    public Calificaciones() {}

    // ===== Getters y Setters =====
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getOwnerId() { return ownerId; }       // Getter agregado
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; } //  Setter agregado

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    public Publicaciones getPublicacion() { return publicacion; }
    public void setPublicacion(Publicaciones publicacion) { this.publicacion = publicacion; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getUpdated() { return updated; }
    public void setUpdated(Date updated) { this.updated = updated; }

    // ===== Utilidad =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Calificaciones)) return false;
        Calificaciones that = (Calificaciones) o;
        return objectId != null && objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId == null ? 0 : Objects.hash(objectId);
    }

    @Override
    public String toString() {
        return "Calificaciones{" +
                "objectId='" + objectId + '\'' +
                ", puntuacion=" + puntuacion +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}