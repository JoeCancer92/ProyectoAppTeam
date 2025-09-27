package com.example.proyectoappteam.clases;

import java.util.Date;
import java.util.List;

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

    // 🚨 PROPIEDAD AÑADIDA: Promedio de calificaciones
    // Backendless devolverá el promedio de la relación 'calificaciones' en este campo.
    private Float promedioCalificacion;

    // *******************************************************************
    // PROPIEDADES DE FECHA/HORA AUTOMÁTICAS DE BACKENDLESS (DATETIME)
    // *******************************************************************
    private Date created; // Columna 'created' de Backendless
    private Date updated; // Columna 'updated' de Backendless

    // *******************************************************************
    // RELACIONES INVERSAS
    // *******************************************************************
    private List<Comentarios> comentarios;
    // Nota: Puedes añadir List<Calificaciones> calificaciones si necesitas la lista de objetos Calificaciones
    // private List<Calificaciones> calificaciones;

    // *******************************************************************
    // Constructor vacío (requerido por el SDK de Backendless)
    // *******************************************************************
    public Publicaciones() {
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA EL PROMEDIO DE CALIFICACIÓN 🚨
    // ===================================================================

    public Float getPromedioCalificacion() {
        return promedioCalificacion;
    }

    public void setPromedioCalificacion(Float promedioCalificacion) {
        this.promedioCalificacion = promedioCalificacion;
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA COMENTARIOS (Relación Inversa)
    // ===================================================================

    public List<Comentarios> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentarios> comentarios) {
        this.comentarios = comentarios;
    }

    // ===================================================================
    // GETTERS Y SETTERS PARA FECHA (created/updated)
    // ===================================================================

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
    // (Asegúrate de que los setters aceptan Double para coincidir con el campo)
    // ===================================================================

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double selectedLatitud) { // Cambiado a Double
        this.latitud = selectedLatitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double selectedLongitud) { // Cambiado a Double
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