package com.example.proyectoappteam;

import com.example.proyectoappteam.clases.Calificaciones;
import org.junit.Test;
import static org.junit.Assert.*;

public class CalificacionesTest {

    /**
     * Prueba que dos objetos Calificaciones con el mismo objectId son considerados iguales.
     */
    @Test
    public void equals_MismoObjectId_DevuelveTrue() {
        // 1. Crear dos objetos con el mismo ID
        String objectId = "123-abc";
        Calificaciones calificacion1 = new Calificaciones();
        calificacion1.setObjectId(objectId);

        Calificaciones calificacion2 = new Calificaciones();
        calificacion2.setObjectId(objectId);

        // 2. Verificar que el método equals devuelve true
        assertTrue("Los objetos deberían ser iguales si tienen el mismo objectId", calificacion1.equals(calificacion2));
    }

    /**
     * Prueba que dos objetos Calificaciones con diferente objectId son considerados diferentes.
     */
    @Test
    public void equals_DiferenteObjectId_DevuelveFalse() {
        // 1. Crear dos objetos con IDs diferentes
        Calificaciones calificacion1 = new Calificaciones();
        calificacion1.setObjectId("123-abc");

        Calificaciones calificacion2 = new Calificaciones();
        calificacion2.setObjectId("456-def");

        // 2. Verificar que el método equals devuelve false
        assertFalse("Los objetos no deberían ser iguales si tienen diferente objectId", calificacion1.equals(calificacion2));
    }

    /**
     * Prueba que el método hashCode devuelve el mismo valor para dos objetos
     * que son considerados iguales.
     */
    @Test
    public void hashCode_ObjetosIguales_DevuelveMismoHashCode() {
        // 1. Crear dos objetos iguales
        String objectId = "123-abc";
        Calificaciones calificacion1 = new Calificaciones();
        calificacion1.setObjectId(objectId);

        Calificaciones calificacion2 = new Calificaciones();
        calificacion2.setObjectId(objectId);

        // 2. Verificar que sus hash codes son idénticos
        assertEquals("Los hash codes deberían ser iguales para objetos iguales", calificacion1.hashCode(), calificacion2.hashCode());
    }
}