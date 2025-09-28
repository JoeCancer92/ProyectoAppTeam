package com.example.proyectoappteam.clases;

import com.backendless.BackendlessUser;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** Clase modelo para la tabla 'comentarios' en Backendless. */
public class Comentarios implements Serializable {

    // Metadatos
    private String objectId;
    private String ownerId;  // ✅ Faltaba este campo
    private Date created;
    private Date updated;

    // Campos
    private String texto;
    private Date fechaCreacion;

    // Relaciones
    private Publicaciones publicacion;
    private BackendlessUser owner; // Relación directa con el usuario creador

    public Comentarios() {}

    // ====== Getters / Setters ======
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getOwnerId() { return ownerId; }         // ✅ Necesario para el adapter
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getUpdated() { return updated; }
    public void setUpdated(Date updated) { this.updated = updated; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Publicaciones getPublicacion() { return publicacion; }
    public void setPublicacion(Publicaciones publicacion) { this.publicacion = publicacion; }

    public BackendlessUser getOwner() { return owner; }    // Para cargar el usuario completo
    public void setOwner(BackendlessUser owner) { this.owner = owner; }

    // ====== Utilidad ======
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comentarios)) return false;
        Comentarios that = (Comentarios) o;
        return objectId != null && objectId.equals(that.objectId);
    }

    @Override public int hashCode() { return objectId == null ? 0 : Objects.hash(objectId); }

    @Override public String toString() {
        return "Comentarios{objectId='" + objectId + "', texto='" + texto + "'}";
    }
}