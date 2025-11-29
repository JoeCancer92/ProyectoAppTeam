package com.example.proyectoappteam.fragmentos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.ProyectoAppTeam;
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

    private static final String TAG = "InicioFragment";

    private BroadcastReceiver nuevaPublicacionReceiver;

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

        configurarReceptorDePublicaciones();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(nuevaPublicacionReceiver, new IntentFilter(ProyectoAppTeam.ACTION_NUEVA_PUBLICACION));
        cargarPublicacionesDesdeBackendless();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(nuevaPublicacionReceiver);
    }

    @Override
    public void onCalificacionEnviada() {
        Log.d(TAG, "Calificación enviada, se recargará la lista.");
        cargarPublicacionesDesdeBackendless();
    }

    public void refreshPosts() {
        cargarPublicacionesDesdeBackendless();
    }

    private void configurarReceptorDePublicaciones() {
        nuevaPublicacionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ProyectoAppTeam.ACTION_NUEVA_PUBLICACION.equals(intent.getAction())) {
                    String nuevaPublicacionId = intent.getStringExtra("NUEVA_PUBLICACION_ID");
                    if (nuevaPublicacionId != null) {
                        Log.d(TAG, "Señal de nueva publicación recibida. ID: " + nuevaPublicacionId);
                        // Lógica de actualización quirúrgica
                        anadirNuevaPublicacion(nuevaPublicacionId);
                    }
                }
            }
        };
    }

    // NUEVO: Método para buscar solo la nueva publicación y añadirla al principio
    private void anadirNuevaPublicacion(String publicacionId) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        // Cargar las relaciones necesarias para mostrar la publicación correctamente
        queryBuilder.setRelated(new String[]{"ownerId"});

        Backendless.Data.of(Publicaciones.class).findById(publicacionId, queryBuilder, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones nuevaPublicacion) {
                if (isAdded() && adapter != null && recyclerView != null) {
                    // Evitar duplicados si la publicación ya está en la lista
                    if (!publicacionesList.contains(nuevaPublicacion)) {
                        publicacionesList.add(0, nuevaPublicacion);
                        adapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);
                        Toast.makeText(getContext(), "Hay una nueva publicación", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al buscar la nueva publicación por ID: " + fault.getMessage());
            }
        });
    }

    private void cargarPublicacionesDesdeBackendless() {
        if (!isAdded()) return;
        progressBar.setVisibility(View.VISIBLE);

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setRelated(new String[]{"ownerId"}); // Asegurarse de cargar el owner

        Backendless.Data.of(Publicaciones.class).find(queryBuilder, new AsyncCallback<List<Publicaciones>>() {
            @Override
            public void handleResponse(List<Publicaciones> foundPublicaciones) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (foundPublicaciones != null && !foundPublicaciones.isEmpty()) {
                    publicacionesList.clear();
                    publicacionesList.addAll(foundPublicaciones);
                    adapter.notifyDataSetChanged();
                } else {
                    publicacionesList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "No hay publicaciones para mostrar.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al cargar publicaciones: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al cargar las publicaciones: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
