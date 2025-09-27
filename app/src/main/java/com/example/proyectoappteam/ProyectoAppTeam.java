package com.example.proyectoappteam;

import android.app.Application;
import com.backendless.Backendless;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.Publicaciones;

public class ProyectoAppTeam extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicialización con las nuevas credenciales del proyecto.
        Backendless.initApp(this,
                "FF8096CC-3E75-470C-BA38-0B499C3562F5", // NUEVO Application ID
                "4EE0AA48-E31D-40E1-8F01-1D147B8699DE"); // NUEVA Android API Key


        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios", Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones", Publicaciones.class);
    }
}
