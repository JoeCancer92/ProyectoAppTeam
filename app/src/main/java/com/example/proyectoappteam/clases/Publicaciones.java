package com.example.proyectoappteam.clases;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Clase de mapeo para la tabla 'Publicaciones' en Backendless.
 */
public class Publicaciones implements Serializable {

    // ===== Metadatos Backendless =====
    private String objectId;
    private String ownerId;
    private Date created; // datetime
    private Date updated; // datetime

    // ===== Campos =====
    private String descripcion;
    private String ubicacion;
    private String fotos;
    private Boolean esUrgente;
    private Double latitud;
    private Double longitud;
    private String categoria;

    // Agregados / calculados (si tu backend los rellena)
    private Float promedioCalificacion;

    // ===== Relaciones inversas (opcionales) =====
    private List<Comentarios> comentarios;
    // private List<Calificaciones> calificaciones; // añade si existe en tu esquema

    public Publicaciones() {}

    // ===== Getters/Setters con defensas mínimas =====
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Date getCreated() { return created == null ? null : new Date(created.getTime()); }
    public void setCreated(Date created) { this.created = (created == null) ? null : new Date(created.getTime()); }

    public Date getUpdated() { return updated == null ? null : new Date(updated.getTime()); }
    public void setUpdated(Date updated) { this.updated = (updated == null) ? null : new Date(updated.getTime()); }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getFotos() { return fotos; }
    public void setFotos(String fotos) { this.fotos = fotos; }

    public Boolean getEsUrgente() { return esUrgente; }
    public void setEsUrgente(Boolean esUrgente) { this.esUrgente = esUrgente; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Float getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(Float promedioCalificacion) { this.promedioCalificacion = promedioCalificacion; }

    public List<Comentarios> getComentarios() { return comentarios; }
    public void setComentarios(List<Comentarios> comentarios) { this.comentarios = comentarios; }

    // ===== Utilidad =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publicaciones)) return false;
        Publicaciones that = (Publicaciones) o;
        return objectId != null && objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId == null ? 0 : Objects.hash(objectId);
    }

    @Override
    public String toString() {
        return "Publicaciones{" +
                "objectId='" + objectId + '\'' +
                ", descripcion='" + (descripcion != null ? descripcion : "") + '\'' +
                ", categoria='" + (categoria != null ? categoria : "") + '\'' +
                '}';
    }
}