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
                "57CD6FCB-C7DB-4D73-B0D2-B515A0479C81", // Application ID
                "EDDA0ED9-002B-4284-B549-CC7A1919DFB7"  // Android API Key
        );

        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios",    Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones",  Publicaciones.class);
        Backendless.Data.mapTableToClass("Notificaciones", Notificaciones.class);
    }
}