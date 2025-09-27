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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Publicaciones;

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

        if (publicacionId != null && Backendless.UserService.CurrentUser() != null) {
            cargarCalificacionExistente(publicacionId, Backendless.UserService.CurrentUser().getObjectId());
        }

        btnCancelar.setOnClickListener(v -> dismiss());
        btnEnviar.setOnClickListener(v -> enviarCalificacion());

        ratingBarInput.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && rating < 1) {
                ratingBar.setRating(1);
            }
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
                    Log.i(TAG, "Calificación existente cargada: " + existingRating.getPuntuacion());
                } else {
                    calificacionObjectId = null;
                    ratingBarInput.setRating(0);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar calificación existente: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al cargar tu calificación previa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCalificacion() {
        float rating = ratingBarInput.getRating();

        if (rating < 1) {
            Toast.makeText(getContext(), "Por favor, selecciona al menos 1 estrella.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (publicacionId == null || publicacionId.isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de publicación no disponible.", Toast.LENGTH_LONG).show();
            return;
        }

        btnEnviar.setEnabled(false);

        Calificaciones calificacionToSave = new Calificaciones();
        calificacionToSave.setPuntuacion((int) rating);

        if (Backendless.UserService.CurrentUser() != null) {
            calificacionToSave.setOwnerId(Backendless.UserService.CurrentUser().getObjectId());
        }

        if (calificacionObjectId != null) {
            calificacionToSave.setObjectId(calificacionObjectId);
        }

        Backendless.Data.of(Calificaciones.class).save(calificacionToSave, new AsyncCallback<Calificaciones>() {
            @Override
            public void handleResponse(Calificaciones calificacionGuardada) {
                if (calificacionObjectId == null) {
                    Publicaciones publicacionRelacion = new Publicaciones();
                    publicacionRelacion.setObjectId(publicacionId);

                    Backendless.Data.of(Calificaciones.class).setRelation(
                            calificacionGuardada,
                            "publicacion",
                            Collections.singletonList(publicacionRelacion),
                            new AsyncCallback<Integer>() {
                                @Override
                                public void handleResponse(Integer response) {
                                    postSaveSuccess(calificacionGuardada.getPuntuacion());
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.e(TAG, "Error al establecer relación de calificación: " + fault.getMessage());
                                    postSaveSuccess(calificacionGuardada.getPuntuacion());
                                    btnEnviar.setEnabled(true);
                                }
                            }
                    );
                } else {
                    postSaveSuccess(calificacionGuardada.getPuntuacion());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al guardar/actualizar calificación: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al guardar calificación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
            }
        });
    }

    private void postSaveSuccess(int puntuacion) {
        Toast.makeText(getContext(), "Calificación guardada/actualizada: " + puntuacion + " estrellas.", Toast.LENGTH_SHORT).show();
        if (listener != null) {
            listener.onCalificacionEnviada();
        }
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