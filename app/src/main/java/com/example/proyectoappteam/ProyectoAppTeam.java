package com.example.proyectoappteam;

import android.app.Application;
import com.backendless.Backendless;

public class ProyectoAppTeam extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicialización con las nuevas credenciales del proyecto.
        // Esto resuelve el error de "Maximum request per day limit has been reached".
        Backendless.initApp(this,
                "FF8096CC-3E75-470C-BA38-0B499C3562F5", // NUEVO Application ID
                "4EE0AA48-E31D-40E1-8F01-1D147B8699DE"); // NUEVA Android API Key
    }
}
