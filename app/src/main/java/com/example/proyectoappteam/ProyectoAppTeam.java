package com.example.proyectoappteam;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.rt.data.EventHandler;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.Notificaciones;
import com.example.proyectoappteam.clases.Publicaciones;

public class ProyectoAppTeam extends Application {

    private static final String TAG = "ProyectoAppTeam";
    
    public static final String ACTION_NUEVA_NOTIFICACION = "com.example.proyectoappteam.NUEVA_NOTIFICACION";
    public static final String ACTION_NUEVA_PUBLICACION = "com.example.proyectoappteam.NUEVA_PUBLICACION";

    private EventHandler<Notificaciones> rtNotificacionesHandler;
    private EventHandler<Publicaciones> rtPublicacionesHandler;

    @Override
    public void onCreate() {
        super.onCreate();

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

    public void iniciarListenersEnTiempoReal() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) return;

        detenerListenersEnTiempoReal();

        String userId = currentUser.getObjectId();

        // Listener para Notificaciones
        rtNotificacionesHandler = Backendless.Data.of(Notificaciones.class).rt();
        AsyncCallback<Notificaciones> notifListener = new AsyncCallback<Notificaciones>() {
            @Override
            public void handleResponse(Notificaciones response) {
                Log.i(TAG, "Nueva notificación recibida.");
                Intent intent = new Intent(ACTION_NUEVA_NOTIFICACION);
                LocalBroadcastManager.getInstance(ProyectoAppTeam.this).sendBroadcast(intent);
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de notificaciones: " + fault.getMessage());
            }
        };
        String notifWhereClause = "userReceptor.objectId = '" + userId + "'";
        rtNotificacionesHandler.addCreateListener(notifWhereClause, notifListener);
        Log.i(TAG, "Listener de NOTIFICACIONES en tiempo real INICIADO.");

        // Listener para Publicaciones
        rtPublicacionesHandler = Backendless.Data.of(Publicaciones.class).rt();
        AsyncCallback<Publicaciones> pubListener = new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones nuevaPublicacion) {
                Log.i(TAG, "Nueva publicación recibida. ID: " + nuevaPublicacion.getObjectId());
                Intent intent = new Intent(ACTION_NUEVA_PUBLICACION);
                // CORRECCIÓN: Se añade el ID de la nueva publicación a la señal
                intent.putExtra("NUEVA_PUBLICACION_ID", nuevaPublicacion.getObjectId());
                LocalBroadcastManager.getInstance(ProyectoAppTeam.this).sendBroadcast(intent);
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de publicaciones: " + fault.getMessage());
            }
        };
        rtPublicacionesHandler.addCreateListener(pubListener);
        Log.i(TAG, "Listener de PUBLICACIONES en tiempo real INICIADO.");
    }

    public void detenerListenersEnTiempoReal() {
        if (rtNotificacionesHandler != null) {
            rtNotificacionesHandler.removeCreateListeners();
            rtNotificacionesHandler = null;
            Log.i(TAG, "Listener de NOTIFICACIONES en tiempo real DETENIDO.");
        }
        if (rtPublicacionesHandler != null) {
            rtPublicacionesHandler.removeCreateListeners();
            rtPublicacionesHandler = null;
            Log.i(TAG, "Listener de PUBLICACIONES en tiempo real DETENIDO.");
        }
    }
}