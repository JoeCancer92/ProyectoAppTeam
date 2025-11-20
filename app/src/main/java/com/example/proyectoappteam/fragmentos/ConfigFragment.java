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
import com.example.proyectoappteam.clases.LocaleHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigFragment extends Fragment {

    private Spinner frgCfgCboIdiomas;
    private Spinner frgCfgCboTema;
    private Spinner frgCfgCboTamanoFuente;
    private Button frgCfgBtnAplicar;
    private Button frgCfgBtnRestaurar;

    private static final String PREFS_NAME = "AppConfigPrefs";
    private static final String KEY_IDIOMA_CODE = "Locale.Helper.Selected.Language";
    private static final String KEY_TEMA = "tema";
    private static final String KEY_TAMANO_FUENTE = "tamano_fuente";

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
        frgCfgCboTamanoFuente = view.findViewById(R.id.frgCfgCboTamanoFuente);
        frgCfgBtnAplicar = view.findViewById(R.id.frgCfgBtnAplicar);
        frgCfgBtnRestaurar = view.findViewById(R.id.frgCfgBtnRestaurar);

        // 2. Cargar preferencias guardadas y configurar vistas
        cargarPreferencias();

        // 3. Configurar listeners
        frgCfgBtnAplicar.setOnClickListener(v -> aplicarPreferencias());
        frgCfgBtnRestaurar.setOnClickListener(v -> restaurarPreferencias());
    }

    private SharedPreferences getPrefs() {
        return requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void cargarPreferencias() {
        SharedPreferences prefs = getPrefs();

        // Idioma
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
        int temaPos = 0;
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

        // Tamaño de fuente
        int tamanoFuenteGuardado = prefs.getInt(KEY_TAMANO_FUENTE, 1); // 0: Pequeño, 1: Mediano, 2: Grande
        frgCfgCboTamanoFuente.setSelection(tamanoFuenteGuardado);
    }

    private void aplicarPreferencias() {
        SharedPreferences.Editor editor = getPrefs().edit();

        // Guardar Idioma
        String[] idiomaCodes = getResources().getStringArray(R.array.idiomas_opciones);
        String idiomaSeleccionado = idiomaCodes[frgCfgCboIdiomas.getSelectedItemPosition()];
        LocaleHelper.setLocale(getContext(), idiomaSeleccionado);

        // Guardar y Aplicar Tema Oscuro
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
        AppCompatDelegate.setDefaultNightMode(nuevoModoTema);

        // Guardar Tamaño de Fuente
        int tamanoFuentePos = frgCfgCboTamanoFuente.getSelectedItemPosition();
        editor.putInt(KEY_TAMANO_FUENTE, tamanoFuentePos);

        editor.apply();
        Toast.makeText(getContext(), "Preferencias aplicadas y guardadas.", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MenuPrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void restaurarPreferencias() {
        // Restaurar a valores por defecto
        frgCfgCboIdiomas.setSelection(0); // Español ("es")
        frgCfgCboTema.setSelection(0); // Sistema
        frgCfgCboTamanoFuente.setSelection(1); // Mediano

        // Aplicar los valores restaurados y guardarlos en SharedPreferences
        aplicarPreferencias();
        Toast.makeText(getContext(), "Preferencias restauradas a valores por defecto.", Toast.LENGTH_SHORT).show();
    }
}