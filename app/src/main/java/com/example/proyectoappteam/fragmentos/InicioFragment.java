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
    private BroadcastReceiver publicacionReceiver;

    public static final String ACTION_NUEVA_PUBLICACION = "com.example.proyectoappteam.NUEVA_PUBLICACION";
    private static final String TAG = "InicioFragment";

    public InicioFragment() {
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
        cargarPublicacionesDesdeBackendless();
        suscribirAPublicacionesEnTiempoReal();
        configurarReceptorDePublicacionesLocales();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(publicacionReceiver, new IntentFilter(ACTION_NUEVA_PUBLICACION));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(publicacionReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rtListenerHandler != null) {
            rtListenerHandler.removeCreateListeners();
        }
    }

    @Override
    public void onCalificacionEnviada() {
        refreshPosts();
    }

    public void refreshPosts() {
        cargarPublicacionesDesdeBackendless();
    }

    private void configurarReceptorDePublicacionesLocales() {
        publicacionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Si la señal es de una nueva publicación, refrescamos la lista.
                if (ACTION_NUEVA_PUBLICACION.equals(intent.getAction())) {
                    refreshPosts();
                }
            }
        };
    }

    private void suscribirAPublicacionesEnTiempoReal() {
        rtListenerHandler = Backendless.Data.of(Publicaciones.class).rt();

        AsyncCallback<Publicaciones> createListener = new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones nuevaPublicacion) {
                if (isAdded()) { // Nos aseguramos que el fragmento siga vivo
                    getActivity().runOnUiThread(() -> agregarNuevaPublicacion(nuevaPublicacion));
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener RT: " + fault.getMessage());
            }
        };

        rtListenerHandler.addCreateListener(createListener);
        Log.i(TAG, "Suscrito a publicaciones en tiempo real.");
    }

    private void agregarNuevaPublicacion(Publicaciones publicacion) {
        if (adapter == null || recyclerView == null || publicacionesList.contains(publicacion)) {
            return; // Evita duplicados
        }
        publicacionesList.add(0, publicacion);
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
        Toast.makeText(getContext(), "Nueva publicación recibida", Toast.LENGTH_SHORT).show();
    }

    private void cargarPublicacionesDesdeBackendless() {
        progressBar.setVisibility(View.VISIBLE);

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setRelationsDepth(1); // Cargar datos del owner

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
                if(isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar las publicaciones: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
