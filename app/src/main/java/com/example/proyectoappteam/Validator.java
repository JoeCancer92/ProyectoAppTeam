package com.example.proyectoappteam;

import java.util.regex.Pattern;

public class Validator {

    // Expresión regular para validar correos. Es la misma que usa Android internamente.
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    );

    /**
     * Comprueba si un texto tiene el formato de un correo electrónico válido.
     * @param email el texto a validar.
     * @return true si el correo es válido, false en caso contrario.
     */
    public boolean isValidEmail(CharSequence email) {
        if (email == null) {
            return false;
        }
        // Ahora usamos nuestro propio Pattern de Java, que no depende de Android.
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }
}
