package com.example.proyectoappteam;

import com.example.proyectoappteam.clases.Usuario;

import org.junit.Test;
import static org.junit.Assert.*;

public class UsuarioTest {    /**
 * Prueba que los métodos getters y setters de la clase Usuario
 * funcionan correctamente al asignar y recuperar valores.
 */
@Test
public void usuario_GettersAndSetters_FuncionanCorrectamente() {
    // 1. Crear una instancia de la clase a probar
    Usuario usuario = new Usuario();

    // 2. Definir los valores de prueba
    String nombre = "Juan";
    String apellidos = "Pérez";
    String correo = "juan.perez@example.com";
    String dni = "12345678A";

    // 3. Usar los métodos setters para asignar los valores
    usuario.setNombre(nombre);
    usuario.setApellidos(apellidos);
    usuario.setCorreo(correo);
    usuario.setDni(dni);

    // 4. Usar los métodos getters y verificar que devuelven los valores correctos
    assertEquals("El nombre no coincide", nombre, usuario.getNombre());
    assertEquals("Los apellidos no coinciden", apellidos, usuario.getApellidos());
    assertEquals("El correo no coincide", correo, usuario.getCorreo());
    assertEquals("El DNI no coincide", dni, usuario.getDni());
}
}
