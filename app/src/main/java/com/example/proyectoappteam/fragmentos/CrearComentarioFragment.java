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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Comentarios;
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
        } else {
            Log.e(TAG, context.toString() + " debe implementar ComentarioListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicacionId = getArguments().getString(ARG_PUBLICACION_ID);
            Log.i(TAG, "ID de Publicación recibida en onCreate: " + publicacionId);
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
            Toast.makeText(getContext(), "Error fatal: ID de publicación no disponible para guardar.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "publicacionId es nula o vacía al intentar guardar.");
            return;
        }

        btnEnviar.setEnabled(false);

        Comentarios nuevoComentario = new Comentarios();
        nuevoComentario.setTexto(textoComentario);
        nuevoComentario.setFechaCreacion(new Date());

        Publicaciones publicacionRelacion = new Publicaciones();
        publicacionRelacion.setObjectId(publicacionId);

        // Guardar comentario sin relación
        Backendless.Data.of(Comentarios.class).save(nuevoComentario, new AsyncCallback<Comentarios>() {
            @Override
            public void handleResponse(Comentarios comentarioGuardado) {
                // Establecer relación manualmente
                Backendless.Data.of(Comentarios.class).setRelation(
                        comentarioGuardado,
                        "publicacion",
                        Collections.singletonList(publicacionRelacion),
                        new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse(Integer response) {
                                Toast.makeText(getContext(), "Comentario y relación guardados correctamente.", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Relación establecida con publicación ID: " + publicacionId);

                                if (listener != null) {
                                    listener.onComentarioEnviado();
                                }

                                dismiss();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.e(TAG, "Error al establecer relación: " + fault.getMessage());
                                Toast.makeText(getContext(), "Comentario guardado pero sin relación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                btnEnviar.setEnabled(true);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}