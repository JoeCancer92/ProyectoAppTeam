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
import com.example.proyectoappteam.clases.Publicaciones;

public class ProyectoAppTeam extends Application {

    private static final String TAG = "ProyectoAppTeam";
    
    // --- Constantes de Acciones para Broadcasts ---
    public static final String ACTION_NUEVA_NOTIFICACION = "com.example.proyectoappteam.NUEVA_NOTIFICACION";
    public static final String ACTION_RECARGAR_FEED = "com.example.proyectoappteam.RECARGAR_FEED";

    private EventHandler<Comentarios> comentariosListenerHandler;
    private EventHandler<Calificaciones> calificacionesListenerHandler;

    private boolean hayNotificacionesNuevas = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.initApp(
                this,
                "FF8096CC-3E75-470C-BA38-0B499C3562F5", // Application ID
                "4EE0AA48-E31D-40E1-8F01-1D147B8699DE"  // Android API Key
        );

        Backendless.Data.mapTableToClass("calificaciones", Calificaciones.class);
        Backendless.Data.mapTableToClass("comentarios",    Comentarios.class);
        Backendless.Data.mapTableToClass("publicaciones",  Publicaciones.class);
    }

    public void iniciarListenersDeActividad() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) return;

        detenerListenerDeNotificaciones();

        String userId = currentUser.getObjectId();

        comentariosListenerHandler = Backendless.Data.of(Comentarios.class).rt();
        String whereClauseComentarios = "publicacion.ownerId = '" + userId + "' AND ownerId != '" + userId + "'";
        
        AsyncCallback<Comentarios> comentarioCallback = new AsyncCallback<Comentarios>() {
            @Override
            public void handleResponse(Comentarios nuevoComentario) {
                hayNotificacionesNuevas = true;
                enviarNotificacionBroadcast();
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de comentarios: " + fault.getMessage());
            }
        };
        comentariosListenerHandler.addCreateListener(whereClauseComentarios, comentarioCallback);

        calificacionesListenerHandler = Backendless.Data.of(Calificaciones.class).rt();
        String whereClauseCalificaciones = "publicacion.ownerId = '" + userId + "' AND ownerId != '" + userId + "'";
        
        AsyncCallback<Calificaciones> calificacionCallback = new AsyncCallback<Calificaciones>() {
            @Override
            public void handleResponse(Calificaciones nuevaCalificacion) {
                hayNotificacionesNuevas = true;
                enviarNotificacionBroadcast();
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de calificaciones: " + fault.getMessage());
            }
        };
        calificacionesListenerHandler.addCreateListener(whereClauseCalificaciones, calificacionCallback);
    }

    public void detenerListenerDeNotificaciones() {
        if (comentariosListenerHandler != null) {
            comentariosListenerHandler.removeCreateListeners();
            comentariosListenerHandler = null;
        }
        if (calificacionesListenerHandler != null) {
            calificacionesListenerHandler.removeCreateListeners();
            calificacionesListenerHandler = null;
        }
    }

    private void enviarNotificacionBroadcast() {
        Intent intent = new Intent(ACTION_NUEVA_NOTIFICACION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean hayNotificacionesNuevas() {
        return hayNotificacionesNuevas;
    }

    public void setHayNotificacionesNuevas(boolean hayNotificacionesNuevas) {
        this.hayNotificacionesNuevas = hayNotificacionesNuevas;
    }
}
