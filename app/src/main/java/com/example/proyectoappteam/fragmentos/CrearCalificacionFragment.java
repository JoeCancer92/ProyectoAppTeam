package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Publicaciones;
import com.example.proyectoappteam.clases.Notificaciones;

import java.util.Collections;
import java.util.List;

public class CrearCalificacionFragment extends DialogFragment {

    private static final String TAG = "CrearCalifFragment";
    private static final String ARG_PUBLICACION_ID = "publicacionId";

    public interface CalificacionListener {
        void onCalificacionEnviada();
    }

    private CalificacionListener listener;
    private String publicacionId;
    private String calificacionObjectId = null;

    private RatingBar ratingBarInput;
    private Button btnCancelar;
    private Button btnEnviar;

    public void setCalificacionListener(CalificacionListener calificacionListener) {
        this.listener = calificacionListener;
    }

    public static CrearCalificacionFragment newInstance(String publicacionId) {
        CrearCalificacionFragment fragment = new CrearCalificacionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PUBLICACION_ID, publicacionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionId = getArguments().getString(ARG_PUBLICACION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_crear_calificacion, container, false);

        ratingBarInput = view.findViewById(R.id.rating_bar_input);
        btnCancelar = view.findViewById(R.id.btn_calificacion_cancelar);
        btnEnviar = view.findViewById(R.id.btn_calificacion_enviar);

        BackendlessUser cu = Backendless.UserService.CurrentUser();
        if (publicacionId != null && cu != null) {
            cargarCalificacionExistente(publicacionId, cu.getObjectId());
        }

        btnCancelar.setOnClickListener(v -> dismiss());
        btnEnviar.setOnClickListener(v -> enviarCalificacion());

        ratingBarInput.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && rating < 1) ratingBar.setRating(1);
        });

        return view;
    }

    private void cargarCalificacionExistente(String publicacionId, String userId) {
        String whereClause = "ownerId = '" + userId + "' AND publicacion.objectId = '" + publicacionId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Calificaciones.class).find(queryBuilder, new AsyncCallback<List<Calificaciones>>() {
            @Override
            public void handleResponse(List<Calificaciones> foundCalificaciones) {
                if (foundCalificaciones != null && !foundCalificaciones.isEmpty()) {
                    Calificaciones existingRating = foundCalificaciones.get(0);
                    calificacionObjectId = existingRating.getObjectId();
                    ratingBarInput.setRating(existingRating.getPuntuacion());
                    Log.i(TAG, "Calificación existente encontrada: " + existingRating.getPuntuacion());
                } else {
                    // Normal: no se encontró calificación previa.
                    calificacionObjectId = null;
                    ratingBarInput.setRating(0);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // SOLUCIÓN DEFINITIVA: Si la búsqueda falla por CUALQUIER motivo, no molestamos al usuario.
                // Simplemente lo registramos en el log y continuamos como si no hubiera calificación.
                Log.w(TAG, "No se pudo cargar la calificación previa (esto puede ser normal). Causa: " + fault.getMessage());
                calificacionObjectId = null;
                ratingBarInput.setRating(0);
            }
        });
    }

    private void enviarCalificacion() {
        float rating = ratingBarInput.getRating();

        if (rating < 1) {
            if (isAdded()) Toast.makeText(requireContext(), "Selecciona al menos 1 estrella.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (publicacionId == null || publicacionId.isEmpty()) {
            if (isAdded()) Toast.makeText(requireContext(), "Error: publicación no disponible.", Toast.LENGTH_LONG).show();
            return;
        }

        btnEnviar.setEnabled(false);

        Calificaciones calificacionToSave = new Calificaciones();
        calificacionToSave.setPuntuacion((int) rating);

        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser != null) calificacionToSave.setOwnerId(currentUser.getObjectId());

        Backendless.Data.of(Calificaciones.class).save(calificacionToSave, new AsyncCallback<Calificaciones>() {
            @Override
            public void handleResponse(Calificaciones calificacionGuardada) {
                if (calificacionObjectId == null) {
                    // Crear relación con la publicación (solo la primera vez)
                    Publicaciones publicacionRelacion = new Publicaciones();
                    publicacionRelacion.setObjectId(publicacionId);

                    Backendless.Data.of(Calificaciones.class).setRelation(
                            calificacionGuardada,
                            "publicacion",
                            Collections.singletonList(publicacionRelacion),
                            new AsyncCallback<Integer>() {
                                @Override
                                public void handleResponse(Integer response) {
                                    cargarPublicacionYNotificar(calificacionGuardada.getPuntuacion());
                                }
                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.e(TAG, "Error relación calificación-publicación: " + fault.getMessage());
                                    cargarPublicacionYNotificar(calificacionGuardada.getPuntuacion());
                                }
                            }
                    );
                } else {
                    cargarPublicacionYNotificar(calificacionGuardada.getPuntuacion());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al guardar/actualizar calificación: " + fault.getMessage());
                if (isAdded()) Toast.makeText(requireContext(), "Error al guardar calificación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
            }
        });
    }

    private void cargarPublicacionYNotificar(int puntuacion) {
        Backendless.Data.of(Publicaciones.class).findById(publicacionId, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones publicacionCompleta) {
                BackendlessUser usuarioActual = Backendless.UserService.CurrentUser();
                String ownerId = publicacionCompleta.getOwnerId();

                // No notificar autocalificación
                if (usuarioActual != null && ownerId != null && ownerId.equals(usuarioActual.getObjectId())) {
                    Log.i(TAG, "Autocalificación: no se crea notificación.");
                    postSaveSuccess(puntuacion);
                    return;
                }

                if (usuarioActual == null || ownerId == null) {
                    Log.e(TAG, "Usuario actual u ownerId null. No se notifica.");
                    postSaveSuccess(puntuacion);
                    return;
                }

                // 1) Guardar notificación SIN relaciones
                Notificaciones n = new Notificaciones();
                String emisorName = usuarioActual.getEmail();
                Object nombre = usuarioActual.getProperty("nombre") != null
                        ? usuarioActual.getProperty("nombre")
                        : usuarioActual.getProperty("name");
                if (nombre instanceof String && !((String) nombre).isEmpty()) emisorName = (String) nombre;

                n.setMensaje(emisorName + " te ha calificado con " + puntuacion + " estrellas.");
                n.setTipoNotificacion("CALIFICACION");
                n.setLeida(false);

                // usar 0.0 o Backendless "created" para ordenar, no System.currentTimeMillis()
                n.setTimestamposimulado(0.0);

                Backendless.Data.of(Notificaciones.class).save(n, new AsyncCallback<Notificaciones>() {
                    @Override
                    public void handleResponse(Notificaciones saved) {

                        // 2) Relacionar con receptor, emisor y publicación
                        BackendlessUser receptorRef = new BackendlessUser();
                        receptorRef.setProperty("objectId", ownerId);

                        BackendlessUser emisorRef = new BackendlessUser();
                        emisorRef.setProperty("objectId", usuarioActual.getObjectId());

                        Backendless.Data.of(Notificaciones.class).setRelation(
                                saved, "userReceptor",
                                Collections.singletonList(receptorRef),
                                new AsyncCallback<Integer>() {
                                    @Override
                                    public void handleResponse(Integer r1) {
                                        Backendless.Data.of(Notificaciones.class).setRelation(
                                                saved, "usuarioEmisorId",
                                                Collections.singletonList(emisorRef),
                                                new AsyncCallback<Integer>() {
                                                    @Override
                                                    public void handleResponse(Integer r2) {
                                                        Backendless.Data.of(Notificaciones.class).setRelation(
                                                                saved, "publicacionId",
                                                                Collections.singletonList(publicacionCompleta),
                                                                new AsyncCallback<Integer>() {
                                                                    @Override
                                                                    public void handleResponse(Integer r3) {
                                                                        Log.i(TAG, "Notificación CALIFICACION creada con relaciones.");
                                                                        postSaveSuccess(puntuacion);
                                                                    }
                                                                    @Override
                                                                    public void handleFault(BackendlessFault f3) {
                                                                        Log.e(TAG, "Relación publicacionId: " + f3.getMessage());
                                                                        postSaveSuccess(puntuacion);
                                                                    }
                                                                });
                                                    }
                                                    @Override
                                                    public void handleFault(BackendlessFault f2) {
                                                        Log.e(TAG, "Relación usuarioEmisorId: " + f2.getMessage());
                                                        postSaveSuccess(puntuacion);
                                                    }
                                                });
                                    }
                                    @Override
                                    public void handleFault(BackendlessFault f1) {
                                        Log.e(TAG, "Relación userReceptor: " + f1.getMessage());
                                        postSaveSuccess(puntuacion);
                                    }
                                });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(TAG, "Error guardando Notificación: " + fault.getMessage());
                        postSaveSuccess(puntuacion);
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar publicación para notificación: " + fault.getMessage());
                if (isAdded()) Toast.makeText(requireContext(), "Calificación guardada, pero no se notificó.", Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
            }
        });
    }

    private void postSaveSuccess(int puntuacion) {
        if (isAdded()) Toast.makeText(requireContext(), "Calificación guardada/actualizada: " + puntuacion + " ★", Toast.LENGTH_SHORT).show();
        btnEnviar.setEnabled(true);
        if (listener != null) listener.onCalificacionEnviada();
        dismiss();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
