package com.example.proyectoappteam;

import com.example.proyectoappteam.clases.Comentarios;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComentariosTest {

    /**
     * Prueba que el método toString() genera una cadena con el formato esperado,
     * incluyendo el objectId y el texto del comentario.
     */
    @Test
    public void toString_ConDatos_GeneraFormatoCorrecto() {
        // 1. Crear un objeto Comentarios y asignarle datos
        Comentarios comentario = new Comentarios();
        comentario.setObjectId("comment-001");
        comentario.setTexto("Este es un comentario de prueba.");

        // 2. Definir la cadena de texto esperada
        String stringEsperado = "Comentarios{objectId='comment-001', texto='Este es un comentario de prueba.'}";

        // 3. Llamar al método toString() y verificar el resultado
        assertEquals("El formato de toString() no es el esperado.", stringEsperado, comentario.toString());
    }

    /**
     * Prueba el comportamiento del método toString() cuando los campos son nulos.
     */
    @Test
    public void toString_ConDatosNulos_GeneraFormatoCorrecto() {
        // 1. Crear un objeto sin asignarle datos
        Comentarios comentario = new Comentarios();

        // 2. Definir la cadena esperada para campos nulos
        String stringEsperado = "Comentarios{objectId='null', texto='null'}";

        // 3. Verificar el resultado
        assertEquals("El formato de toString() con valores nulos no es el esperado.", stringEsperado, comentario.toString());
    }
}
