package com.example.proyectoappteam.clases; // O el paquete donde tengas tus clases de utilidad

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "AppConfigPrefs";
    private static final String KEY_IDIOMA_CODE = "idioma_code";
    private static final String DEFAULT_LANGUAGE = "es"; // Idioma por defecto

    /**
     * Aplica el idioma guardado al contexto de la aplicación.
     * Este método debe ser llamado en el attachBaseContext de la Activity.
     */
    public static Context onAttach(Context context) {
        String language = getPersistedData(context);
        return setLocale(context, language);
    }

    /**
     * Devuelve el código de idioma persistente de SharedPreferences.
     */
    public static String getPersistedData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_IDIOMA_CODE, DEFAULT_LANGUAGE);
    }

    /**
     * Establece el nuevo idioma en el contexto.
     */
    public static Context setLocale(Context context, String language) {
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();

        // Usa métodos modernos para actualizar la configuración
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            // Métodos antiguos para versiones anteriores a N (Android 7.0)
            configuration.locale = locale;
            context.getResources().updateConfiguration(configuration,
                    context.getResources().getDisplayMetrics());
            return context;
        }
    }
}