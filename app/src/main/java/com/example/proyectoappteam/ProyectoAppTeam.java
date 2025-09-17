package com.example.proyectoappteam;

import android.app.Application;
import com.backendless.Backendless;

public class ProyectoAppTeam extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.initApp(this,
                "57CD6FCB-C7DB-4D73-B0D2-B515A0479C81", // Application ID
                "EDDA0ED9-002B-4284-B549-CC7A1919DFB7"); // Android API Key
    }
}