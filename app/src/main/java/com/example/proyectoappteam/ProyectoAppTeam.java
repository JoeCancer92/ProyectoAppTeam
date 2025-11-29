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
                "FF8096CC-3E75-470C-BA38-0B499C3562F5", // Application ID
                "4EE0AA48-E31D-40E1-8F01-1D147B8699DE"  // Android API Key
        );

        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios",    Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones",  Publicaciones.class);
        Backendless.Data.mapTableToClass("Notificaciones", Notificaciones.class);
    }
}