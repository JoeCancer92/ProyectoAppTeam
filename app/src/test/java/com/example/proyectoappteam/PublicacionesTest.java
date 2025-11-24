package com.example.proyectoappteam;

import com.example.proyectoappteam.clases.Publicaciones;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

public class PublicacionesTest {

    /**
     * Prueba que los setters para objetos Date (como setCreated) crean una
     * copia defensiva para proteger el estado interno del objeto Publicaciones.
     */
    @Test
    public void setCreated_CreaCopiaDefensiva_ProtegeDeModificacionesExternas() {
        // 1. Crear una publicación y una fecha original
        Publicaciones publicacion = new Publicaciones();
        Date fechaOriginal = new Date();
        long tiempoOriginal = fechaOriginal.getTime();

        // 2. Asignar la fecha a la publicación
        publicacion.setCreated(fechaOriginal);

        // 3. Modificar la fecha original DESPUÉS de haberla asignado.
        // Si no hubiera copia defensiva, esto también cambiaría la fecha dentro del objeto.
        fechaOriginal.setTime(tiempoOriginal + 10000); // Añadimos 10 segundos

        // 4. Obtener la fecha de la publicación y verificar
        Date fechaEnPublicacion = publicacion.getCreated();

        // Se comprueba que no son el mismo objeto en memoria
        assertNotSame("Los objetos Date no deberían ser la misma instancia.", fechaOriginal, fechaEnPublicacion);

        // Se comprueba que el tiempo de la fecha interna no ha cambiado
        assertEquals("El tiempo de la fecha interna no debería haber cambiado.", tiempoOriginal, fechaEnPublicacion.getTime());
    }
}
