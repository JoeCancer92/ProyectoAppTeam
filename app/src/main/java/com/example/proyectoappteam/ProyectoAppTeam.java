package com.example.proyectoappteam;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.rt.data.EventHandler;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.Notificaciones;
import com.example.proyectoappteam.clases.Publicaciones;

public class ProyectoAppTeam extends Application {

    private static final String TAG = "ProyectoAppTeam";
    // Acción para la comunicación interna entre el listener y la Activity
    public static final String ACTION_NUEVA_NOTIFICACION = "com.example.proyectoappteam.NUEVA_NOTIFICACION";

    // Guardamos el handler para poder removerlo al cerrar sesión
    private EventHandler<Notificaciones> rtListenerHandler;

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

    /**
     * Inicia el listener de notificaciones en tiempo real para el usuario logueado.
     * Debe llamarse después de un login exitoso.
     */
    public void iniciarListenerDeNotificaciones() {
        String userId = Backendless.UserService.CurrentUser() != null ? Backendless.UserService.CurrentUser().getObjectId() : null;
        if (userId == null) {
            Log.e(TAG, "No se puede iniciar el listener: usuario no logueado.");
            return;
        }

        // Si ya existe un listener, lo detenemos primero para evitar duplicados
        if (rtListenerHandler != null) {
            detenerListenerDeNotificaciones();
        }

        rtListenerHandler = Backendless.Data.of(Notificaciones.class).rt();

        AsyncCallback<Notificaciones> createListener = new AsyncCallback<Notificaciones>() {
            @Override
            public void handleResponse(Notificaciones nuevaNotificacion) {
                Log.i(TAG, "¡Nueva notificación recibida en tiempo real!");

                // Enviamos una "señal" a cualquier parte de la app que esté escuchando
                Intent intent = new Intent(ACTION_NUEVA_NOTIFICACION);
                LocalBroadcastManager.getInstance(ProyectoAppTeam.this).sendBroadcast(intent);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en el listener de notificaciones: " + fault.getMessage());
            }
        };

        // Condición: solo escuchar notificaciones creadas para el usuario actual.
        String whereClause = "userReceptor.objectId = '" + userId + "'";
        rtListenerHandler.addCreateListener(whereClause, createListener);

        Log.i(TAG, "Listener de notificaciones en tiempo real INICIADO para el usuario: " + userId);
    }

    /**
     * Detiene el listener de notificaciones en tiempo real.
     * Debe llamarse al cerrar sesión para liberar recursos.
     */
    public void detenerListenerDeNotificaciones() {
        if (rtListenerHandler != null) {
            rtListenerHandler.removeCreateListeners();
            rtListenerHandler = null;
            Log.i(TAG, "Listener de notificaciones en tiempo real DETENIDO.");
        }
    }
}
