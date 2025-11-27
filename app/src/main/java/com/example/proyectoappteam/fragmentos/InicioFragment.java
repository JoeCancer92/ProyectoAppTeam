package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.PublicacionAdapter;
import com.example.proyectoappteam.clases.Publicaciones;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment
        implements CrearCalificacionFragment.CalificacionListener {

    private RecyclerView recyclerView;
    private PublicacionAdapter adapter;
    private List<Publicaciones> publicacionesList;
    private ProgressBar progressBar;

    // Listener de Backendless para tiempo real
    private EventHandler<Publicaciones> rtListenerHandler;

    private static final String TAG = "InicioFragment";

    public InicioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_publicaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBar);

        publicacionesList = new ArrayList<>();

        FragmentManager fragmentManager = getChildFragmentManager();
        adapter = new PublicacionAdapter(publicacionesList, fragmentManager, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Cargamos los datos iniciales y luego nos suscribimos a los cambios.
        cargarPublicacionesDesdeBackendless();
        suscribirAPublicacionesEnTiempoReal();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar al volver a la pantalla es una buena práctica por si algo cambió offline
        cargarPublicacionesDesdeBackendless();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // MUY IMPORTANTE: Desuscribirse para evitar fugas de memoria
        if (rtListenerHandler != null) {
            rtListenerHandler.removeCreateListeners();
        }
    }

    @Override
    public void onCalificacionEnviada() {
        // Cuando se envía una calificación, refrescamos por si cambió el promedio
        cargarPublicacionesDesdeBackendless();
    }

    private void suscribirAPublicacionesEnTiempoReal() {
        if (rtListenerHandler != null) return; // Ya estamos suscritos

        rtListenerHandler = Backendless.Data.of(Publicaciones.class).rt();

        AsyncCallback<Publicaciones> createListener = new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones nuevaPublicacion) {
                // Nos aseguramos de estar en el hilo de la UI y que el fragmento esté vivo
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Log.i(TAG, "Nueva publicación recibida en tiempo real: " + nuevaPublicacion.getObjectId());
                        // Verificamos si la publicación ya existe en la lista para evitar duplicados
                        boolean yaExiste = false;
                        for (Publicaciones p : publicacionesList) {
                            if (p.getObjectId().equals(nuevaPublicacion.getObjectId())) {
                                yaExiste = true;
                                break;
                            }
                        }

                        if (!yaExiste) {
                            publicacionesList.add(0, nuevaPublicacion); // Añadir al principio
                            adapter.notifyItemInserted(0);
                            recyclerView.scrollToPosition(0);
                            Toast.makeText(getContext(), "Hay una nueva publicación", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de publicaciones en tiempo real: " + fault.getMessage());
            }
        };

        rtListenerHandler.addCreateListener(createListener);
        Log.i(TAG, "Suscrito a nuevas publicaciones en tiempo real.");
    }

    private void cargarPublicacionesDesdeBackendless() {
        progressBar.setVisibility(View.VISIBLE);

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setRelationsDepth(1); // Para cargar info del usuario de la publicación

        Backendless.Data.of(Publicaciones.class).find(queryBuilder, new AsyncCallback<List<Publicaciones>>() {
            @Override
            public void handleResponse(List<Publicaciones> foundPublicaciones) {
                progressBar.setVisibility(View.GONE);
                if (isAdded() && foundPublicaciones != null) {
                    publicacionesList.clear();
                    publicacionesList.addAll(foundPublicaciones);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressBar.setVisibility(View.GONE);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar las publicaciones: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
