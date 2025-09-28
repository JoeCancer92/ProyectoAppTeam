package com.example.proyectoappteam.clases;

import java.io.Serializable;

/**
 * Modelo para mostrar interacciones en el RecyclerView.
 * Puede tener nombre, correo, comentario y/o calificación.
 */
public class InteraccionItem implements Serializable {

    private String nombre;
    private String correo;
    private String comentario;
    private Integer puntuacion; // null si no tiene calificación

    public InteraccionItem() {}

    // --- Getters & Setters ---

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    @Override
    public String toString() {
        return "InteraccionItem{" +
                "nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", comentario='" + comentario + '\'' +
                ", puntuacion=" + puntuacion +
                '}';
    }
}