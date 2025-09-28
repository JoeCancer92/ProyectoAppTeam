package com.example.proyectoappteam.clases;

import com.backendless.BackendlessUser;
import java.util.Date;

public class Notificaciones {

    // ===== METADATOS (Backendless) =====
    private String objectId;        // string_id
    private String ownerId;         // string
    private Date created;           // datetime
    private Date updated;           // datetime

    // ===== CAMPOS =====
    private String tipoNotificacion;    // <-- N mayúscula (igual que en la tabla)
    private String mensaje;             // string
    private Boolean leida = false;      // boolean
    private double timestamposimulado;  // decimal (double)

    // ===== RELACIONES 1:1 (nombres EXACTOS de la tabla) =====
    private BackendlessUser usuarioEmisorId;  // rel 1:1 -> Users
    private BackendlessUser userReceptor;     // rel 1:1 -> Users
    private Publicaciones publicacionId;      // rel 1:1 -> Publicaciones

    // ===== Constructor vacío (requerido por Backendless) =====
    public Notificaciones() {}

    // ===== GETTERS / SETTERS =====
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getUpdated() { return updated; }
    public void setUpdated(Date updated) { this.updated = updated; }

    public String getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }

    public double getTimestamposimulado() { return timestamposimulado; }
    public void setTimestamposimulado(double timestamposimulado) { this.timestamposimulado = timestamposimulado; }

    public BackendlessUser getUsuarioEmisorId() { return usuarioEmisorId; }
    public void setUsuarioEmisorId(BackendlessUser usuarioEmisorId) { this.usuarioEmisorId = usuarioEmisorId; }

    public BackendlessUser getUserReceptor() { return userReceptor; }
    public void setUserReceptor(BackendlessUser userReceptor) { this.userReceptor = userReceptor; }

    public Publicaciones getPublicacionId() { return publicacionId; }
    public void setPublicacionId(Publicaciones publicacionId) { this.publicacionId = publicacionId; }
}