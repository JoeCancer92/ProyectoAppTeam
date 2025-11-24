package com.example.proyectoappteam;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.proyectoappteam.clases.Seguridad;

public class SeguridadTest {

    /**
     * Prueba que el metodo hashClave genera el hash SHA-256 esperado
     * para una contraseña conocida.
     */
    @Test
    public void hashClave_ClaveCorrecta_DevuelveHash() {
        // Valor de entrada
        String claveOriginal = "password123";

        // El hash SHA-256 que esperamos como resultado para "password123"
        String hashEsperado = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f";

        // Ejecutamos el metodo que queremos probar
        String hashGenerado = Seguridad.hashClave(claveOriginal);

        // Verificamos que el resultado generado es igual al que esperábamos
        assertEquals(hashEsperado, hashGenerado);
    }

    /**
     * Prueba que el metodo se comporta correctamente cuando recibe una cadena vacía.
     */
    @Test
    public void hashClave_CadenaVacia_DevuelveHash() {
        // El hash SHA-256 esperado para una cadena vacía ""
        String claveOriginal = "";
        String hashEsperado = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        // Ejecutamos el método
        String hashGenerado = Seguridad.hashClave(claveOriginal);

        // Verificamos el resultado
        assertEquals(hashEsperado, hashGenerado);
    }

    /**
     * Prueba cómo reacciona el metodo ante una entrada nula.
     * En este caso, el código original provocaría una NullPointerException.
     * Una buena prueba debe confirmar que se lanza la excepción esperada.
     */
    @Test
    public void hashClave_ClaveNula_LanzaNullPointerException() {
        try {
            Seguridad.hashClave(null);
            // Si la línea anterior NO lanza una excepción, esta línea se ejecutará y la prueba fallará.
            fail("Se esperaba una NullPointerException, pero no fue lanzada.");
        } catch (NullPointerException e) {
            // ¡Éxito! La excepción esperada fue capturada. La prueba pasa.
            assertNotNull(e);
        }
    }
}