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
                "2906A990-ECAF-493D-9F00-E932ADACD43B", // NUEVO Application ID
                "85E30BE6-D555-4E5D-AF57-DCA6C06FF3A5"); // NUEVA Android API Key


        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios", Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones", Publicaciones.class);
    }
}
