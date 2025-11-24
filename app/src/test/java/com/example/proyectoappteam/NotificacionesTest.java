package com.example.proyectoappteam;

import com.example.proyectoappteam.clases.Notificaciones;
import org.junit.Test;
import static org.junit.Assert.*;

public class NotificacionesTest {

    /**
     * Prueba que el constructor por defecto inicializa el objeto
     * en un estado predecible y correcto.
     */
    @Test
    public void constructor_EstadoInicial_EsCorrecto() {
        // 1. Crear una nueva instancia de Notificaciones sin llamar a ningún método
        Notificaciones notificacion = new Notificaciones();

        // 2. Verificar que el campo 'leida' tiene su valor predeterminado 'false'
        assertFalse("Una nueva notificación debería estar marcada como no leída por defecto.", notificacion.getLeida());

        // 3. Verificar que los campos de tipo Objeto son nulos por defecto
        assertNull("El mensaje debería ser nulo al crear la notificación.", notificacion.getMensaje());
        assertNull("El usuario emisor debería ser nulo al crear la notificación.", notificacion.getUsuarioEmisorId());
    }
}
