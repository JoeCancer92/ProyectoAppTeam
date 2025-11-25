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

        suscribirAPublicacionesEnTiempoReal();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rtListenerHandler != null) {
            // CORRECCIÓN: Usar el método correcto para la versión del SDK
            rtListenerHandler.removeCreateListeners();
        }
    }

    @Override
    public void onCalificacionEnviada() {
        Log.d(TAG, "Calificación enviada, refrescando publicaciones.");
        refreshPosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPublicacionesDesdeBackendless();
    }

    public void refreshPosts() {
        cargarPublicacionesDesdeBackendless();
    }

    private void suscribirAPublicacionesEnTiempoReal() {
        rtListenerHandler = Backendless.Data.of(Publicaciones.class).rt();

        AsyncCallback<Publicaciones> createListener = new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones nuevaPublicacion) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded() || adapter == null || recyclerView == null) return;

                        publicacionesList.add(0, nuevaPublicacion);
                        adapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);

                        Toast.makeText(getContext(), "Nueva publicación recibida", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de publicaciones en tiempo real: " + fault.getMessage());
            }
        };

        // CORRECCIÓN: Usar el método correcto para la versión del SDK
        rtListenerHandler.addCreateListener(createListener);
        Log.i(TAG, "Suscrito a publicaciones en tiempo real.");
    }

    private void cargarPublicacionesDesdeBackendless() {
        progressBar.setVisibility(View.VISIBLE);

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");

        Backendless.Data.of(Publicaciones.class).find(queryBuilder, new AsyncCallback<List<Publicaciones>>() {
            @Override
            public void handleResponse(List<Publicaciones> foundPublicaciones) {
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
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al cargar publicaciones: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al cargar las publicaciones: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
