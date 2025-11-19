package com.example.proyectoappteam.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.proyectoappteam.R;
import com.example.proyectoappteam.actividades.MenuPrincipalActivity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigFragment extends Fragment {

    private Spinner frgCfgCboIdiomas;
    private Spinner frgCfgCboTema;
    private CheckBox frgCfgChkNotificaciones;
    private SeekBar frgCfgBarSonido;
    private Button frgCfgBtnAplicar;
    private Button frgCfgBtnRestaurar;

    private static final String PREFS_NAME = "AppConfigPrefs";
    private static final String KEY_IDIOMA_CODE = "idioma_code";
    private static final String KEY_TEMA = "tema";
    private static final String KEY_NOTIFICACIONES = "notificaciones";
    private static final String KEY_SONIDO = "volumen_sonido";

    public ConfigFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar vistas
        frgCfgCboIdiomas = view.findViewById(R.id.frgCfgCboIdiomas);
        frgCfgCboTema = view.findViewById(R.id.frgCfgCboTema);
        frgCfgChkNotificaciones = view.findViewById(R.id.frgCfgChkNotificaciones);
        frgCfgBarSonido = view.findViewById(R.id.frgCfgBarSonido);
        frgCfgBtnAplicar = view.findViewById(R.id.frgCfgBtnAplicar);
        frgCfgBtnRestaurar = view.findViewById(R.id.frgCfgBtnRestaurar);

        // 2. Cargar preferencias guardadas y configurar vistas
        cargarPreferencias();

        // 3. Configurar listeners
        frgCfgBtnAplicar.setOnClickListener(v -> aplicarPreferencias());
        frgCfgBtnRestaurar.setOnClickListener(v -> restaurarPreferencias());

        // Listener para la barra de sonido
        frgCfgBarSonido.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Puedes mostrar el valor del volumen en un Toast o TextView si lo deseas
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private SharedPreferences getPrefs() {
        return requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void cargarPreferencias() {
        SharedPreferences prefs = getPrefs();

        // Idioma (usando los códigos de idioma del array en strings.xml)
        String idiomaGuardado = prefs.getString(KEY_IDIOMA_CODE, "es");
        String[] idiomaCodes = getResources().getStringArray(R.array.idiomas_opciones);
        int idiomaPos = 0;
        for (int i = 0; i < idiomaCodes.length; i++) {
            if (idiomaCodes[i].equals(idiomaGuardado)) {
                idiomaPos = i;
                break;
            }
        }
        frgCfgCboIdiomas.setSelection(idiomaPos);

        // Tema
        int temaGuardado = prefs.getInt(KEY_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int temaPos = 0; // 0: Sistema, 1: Claro, 2: Oscuro (según el array de strings)
        switch (temaGuardado) {
            case AppCompatDelegate.MODE_NIGHT_NO: // Tema Claro
                temaPos = 1;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES: // Tema Oscuro
                temaPos = 2;
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM: // Sistema
            default:
                temaPos = 0;
                break;
        }
        frgCfgCboTema.setSelection(temaPos);

        // Notificaciones
        boolean notifHabilitadas = prefs.getBoolean(KEY_NOTIFICACIONES, true);
        frgCfgChkNotificaciones.setChecked(notifHabilitadas);

        // Sonido
        int volumenSonido = prefs.getInt(KEY_SONIDO, 75); // Valor por defecto: 75
        frgCfgBarSonido.setProgress(volumenSonido);
    }

    // Metodo clave para aplicar los cambios y guardarlos
    private void aplicarPreferencias() {
        SharedPreferences.Editor editor = getPrefs().edit();

        // 1. Guardar Idioma y aplicarlo (requiere reiniciar Activity)
        String[] idiomaCodes = getResources().getStringArray(R.array.idiomas_opciones);
        String idiomaSeleccionado = idiomaCodes[frgCfgCboIdiomas.getSelectedItemPosition()];
        editor.putString(KEY_IDIOMA_CODE, idiomaSeleccionado);
        //cambiarIdioma(idiomaSeleccionado); // Aplica el cambio y notifica al usuario

        // 2. Guardar Notificaciones
        boolean notifEstado = frgCfgChkNotificaciones.isChecked();
        editor.putBoolean(KEY_NOTIFICACIONES, notifEstado);

        // 3. Guardar Sonido
        int volumen = frgCfgBarSonido.getProgress();
        editor.putInt(KEY_SONIDO, volumen);
        // La lógica para cambiar el volumen del sistema se haría aquí usando AudioManager

        // 4. Guardar y Aplicar Tema Oscuro
        int temaPos = frgCfgCboTema.getSelectedItemPosition();
        int nuevoModoTema;
        if (temaPos == 1) { // Claro
            nuevoModoTema = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (temaPos == 2) { // Oscuro
            nuevoModoTema = AppCompatDelegate.MODE_NIGHT_YES;
        } else { // Sistema (por defecto)
            nuevoModoTema = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        editor.putInt(KEY_TEMA, nuevoModoTema);
        // Aplicar el tema inmediatamente (no requiere reiniciar la Activity)
        AppCompatDelegate.setDefaultNightMode(nuevoModoTema);


        editor.apply();
        Toast.makeText(getContext(), "Preferencias aplicadas y guardadas.", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            // 1. Obtiene el Intent para reiniciar la actividad actual
            Intent intent = getActivity().getIntent();

            // 2. Cierra la actividad actual
            getActivity().finish();

            // 3. Inicia la actividad de nuevo. Al reiniciarse, cargará el nuevo Locale guardado.
            startActivity(intent);
        }

    }

    // Función para cambiar el Locale (Idioma)
//    private void cambiarIdioma(String langCode) {
//        Locale locale = new Locale(langCode);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//
//        // Actualiza el contexto y el contexto de la aplicación
//        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
//        requireActivity().getApplicationContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
//
//        Toast.makeText(getContext(), "Idioma cambiado a: " + langCode + ". Reinicie la aplicación para ver todos los cambios.", Toast.LENGTH_LONG).show();
//    }

    private void restaurarPreferencias() {
        // Restaurar a valores por defecto
        frgCfgCboIdiomas.setSelection(0); // Español ("es")
        frgCfgCboTema.setSelection(0); // Sistema
        frgCfgChkNotificaciones.setChecked(true);
        frgCfgBarSonido.setProgress(75);

        // Aplicar los valores restaurados y guardarlos en SharedPreferences
        aplicarPreferencias();
        Toast.makeText(getContext(), "Preferencias restauradas a valores por defecto.", Toast.LENGTH_SHORT).show();
    }



}