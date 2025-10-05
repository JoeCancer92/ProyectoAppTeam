package com.example.proyectoappteam.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.Notificaciones;
import com.example.proyectoappteam.clases.Publicaciones;

import java.util.Collections;
import java.util.Date;

public class CrearComentarioFragment extends DialogFragment {

    public interface ComentarioListener {
        void onComentarioEnviado();
    }

    private ComentarioListener listener;
    private static final String ARG_PUBLICACION_ID = "publicacionId";
    private String publicacionId;
    private EditText etComentarioTexto;
    private Button btnCancelar;
    private Button btnEnviar;
    private static final String TAG = "CrearComentarioFragment";

    public static CrearComentarioFragment newInstance(String publicacionId) {
        CrearComentarioFragment fragment = new CrearComentarioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PUBLICACION_ID, publicacionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ComentarioListener) {
            listener = (ComentarioListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionId = getArguments().getString(ARG_PUBLICACION_ID);
            Log.i(TAG, "ID de Publicación: " + publicacionId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_crear_comentario, container, false);

        etComentarioTexto = view.findViewById(R.id.et_comentario_texto);
        btnCancelar = view.findViewById(R.id.btn_comentario_cancelar);
        btnEnviar = view.findViewById(R.id.btn_comentario_enviar);

        btnCancelar.setOnClickListener(v -> dismiss());
        btnEnviar.setOnClickListener(v -> enviarComentario());

        return view;
    }

    private void enviarComentario() {
        String textoComentario = etComentarioTexto.getText().toString().trim();

        if (textoComentario.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, escribe un comentario.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (publicacionId == null || publicacionId.isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de publicación no disponible.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "publicacionId es nula o vacía al intentar guardar.");
            return;
        }

        btnEnviar.setEnabled(false);

        Comentarios nuevoComentario = new Comentarios();
        nuevoComentario.setTexto(textoComentario);
        nuevoComentario.setFechaCreacion(new Date());

        Backendless.Data.of(Comentarios.class).save(nuevoComentario, new AsyncCallback<Comentarios>() {
            @Override
            public void handleResponse(Comentarios comentarioGuardado) {
                // Relacionar comentario -> publicación
                Publicaciones publicacionRelacion = new Publicaciones();
                publicacionRelacion.setObjectId(publicacionId);

                Backendless.Data.of(Comentarios.class).setRelation(
                        comentarioGuardado,
                        "publicacion",
                        Collections.singletonList(publicacionRelacion),
                        new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse(Integer response) {
                                Log.i(TAG, "Relación Comentario->Publicación creada");
                                Toast.makeText(getContext(), "Comentario guardado.", Toast.LENGTH_SHORT).show();
                                crearNotificacion(publicacionId, textoComentario);
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.e(TAG, "Error al establecer relación: " + fault.getMessage());
                                Toast.makeText(getContext(), "Comentario guardado pero sin relación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                btnEnviar.setEnabled(true);
                                postSaveSuccess();
                            }
                        }
                );
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al guardar comentario: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al guardar comentario: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
            }
        });
    }

    private void crearNotificacion(String publicacionId, String textoComentario) {
        Backendless.Data.of(Publicaciones.class).findById(publicacionId, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones publicacionCompleta) {
                BackendlessUser usuarioActual = Backendless.UserService.CurrentUser();
                String ownerId = publicacionCompleta.getOwnerId();

                if (usuarioActual == null || ownerId == null) {
                    Log.e(TAG, "Usuario actual o ownerId no disponible.");
                    postSaveSuccess();
                    return;
                }

                if (ownerId.equals(usuarioActual.getObjectId())) {
                    Log.i(TAG, "El emisor es el dueño. No se crea notificación.");
                    postSaveSuccess();
                    return;
                }

                // ===== Crear notificación SIN relaciones =====
                Notificaciones notificacion = new Notificaciones();

                String emisorName = usuarioActual.getEmail();
                Object nombre = usuarioActual.getProperty("nombre") != null
                        ? usuarioActual.getProperty("nombre")
                        : usuarioActual.getProperty("name");
                if (nombre instanceof String && !((String) nombre).isEmpty()) emisorName = (String) nombre;

                notificacion.setMensaje(emisorName + " ha comentado tu publicación.");
                notificacion.setTipoNotificacion("COMENTARIO");
                notificacion.setLeida(false);

                //  EVITAR TRUNCATION: NO guardar timestamp grande (usa created para ordenar)
                notificacion.setTimestamposimulado(0.0);

                Backendless.Data.of(Notificaciones.class).save(notificacion, new AsyncCallback<Notificaciones>() {
                    @Override
                    public void handleResponse(Notificaciones saved) {
                        // ===== Relacionar userReceptor, usuarioEmisorId y publicacionId =====
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
                                                                        Log.i(TAG, "Notificación COMENTARIO creada con relaciones.");
                                                                        postSaveSuccess();
                                                                    }

                                                                    @Override
                                                                    public void handleFault(BackendlessFault f3) {
                                                                        Log.e(TAG, "Relación publicacionId: " + f3.getMessage());
                                                                        postSaveSuccess();
                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault f2) {
                                                        Log.e(TAG, "Relación usuarioEmisorId: " + f2.getMessage());
                                                        postSaveSuccess();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault f1) {
                                        Log.e(TAG, "Relación userReceptor: " + f1.getMessage());
                                        postSaveSuccess();
                                    }
                                });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(TAG, "Error al guardar Notificación: " + fault.getMessage());
                        postSaveSuccess();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar publicación para notificación: " + fault.getMessage());
                Toast.makeText(getContext(), "Comentario guardado pero no se pudo notificar.", Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
                postSaveSuccess();
            }
        });
    }

    private void postSaveSuccess() {
        if (listener != null) {
            listener.onComentarioEnviado();
        }
        btnEnviar.setEnabled(true);
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