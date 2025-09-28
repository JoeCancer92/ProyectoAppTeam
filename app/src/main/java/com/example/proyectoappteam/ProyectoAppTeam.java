package com.example.proyectoappteam;

import android.app.Application;

import com.backendless.Backendless;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.Publicaciones;
import com.example.proyectoappteam.clases.Notificaciones;

public class ProyectoAppTeam extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa Backendless una sola vez aquí
        Backendless.initApp(
                this,
                "2906A990-ECAF-493D-9F00-E932ADACD43B", // Application ID
                "85E30BE6-D555-4E5D-AF57-DCA6C06FF3A5"  // Android API Key
        );

        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios",    Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones",  Publicaciones.class);
        Backendless.Data.mapTableToClass("Notificaciones", Notificaciones.class);
    }
}