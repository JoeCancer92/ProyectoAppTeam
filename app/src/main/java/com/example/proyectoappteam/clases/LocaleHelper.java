package com.example.proyectoappteam.clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "AppConfigPrefs";
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String FONT_SIZE = "tamano_fuente";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        int fontSize = getPersistedFontSize(context, 1);
        return setLocale(context, lang, fontSize);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        int fontSize = getPersistedFontSize(context, 1);
        return setLocale(context, lang, fontSize);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language, int fontSize) {
        persist(context, language, fontSize);
        return updateResources(context, language, fontSize);
    }

    private static void persist(Context context, String language, int fontSize) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.putInt(FONT_SIZE, fontSize);
        editor.apply();
    }

    private static Context updateResources(Context context, String language, int fontSize) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        // Apply font scale
        float scale = 1.0f;
        if (fontSize == 0) {
            scale = 0.85f;
        } else if (fontSize == 2) {
            scale = 1.15f;
        }
        configuration.fontScale = scale;

        // Apply locale
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static int getPersistedFontSize(Context context, int defaultSize) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(FONT_SIZE, defaultSize);
    }
}
