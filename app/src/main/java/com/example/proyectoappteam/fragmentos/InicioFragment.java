package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.PublicacionAdapter;
import com.example.proyectoappteam.clases.Publicaciones;

import java.util.ArrayList;
import java.util.List;

// 🚨 Paso 1: Implementar la interfaz del diálogo
public class InicioFragment extends Fragment
        implements CrearCalificacionFragment.CalificacionListener {

    private RecyclerView recyclerView;
    private PublicacionAdapter adapter;
    private List<Publicaciones> publicacionesList;
    private ProgressBar progressBar;
    private ImageButton btnRefresh;

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

        btnRefresh = view.findViewById(R.id.btn_refresh);

        publicacionesList = new ArrayList<>();

        // Usamos getChildFragmentManager() para manejar los diálogos dentro de este Fragmento
        // 🚨 Es crucial que el FragmentManager se pase junto con el listener 'this'
        FragmentManager fragmentManager = getChildFragmentManager();
        adapter = new PublicacionAdapter(publicacionesList, fragmentManager, this); // <-- ¡Modificado!
        recyclerView.setAdapter(adapter);

        // Asignar el listener de click al botón de refrescar
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Actualizando publicaciones...", Toast.LENGTH_SHORT).show();
                refreshPosts();
            });
        }

        return view;
    }

    // 🚨 Paso 2: Implementar el método del Listener
    // Este método se llama automáticamente desde CrearCalificacionFragment cuando se guarda
    @Override
    public void onCalificacionEnviada() {
        Log.d(TAG, "Calificación enviada, refrescando publicaciones.");
        // Forzamos la recarga de la lista para ver el promedio actualizado
        refreshPosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cargar las publicaciones cada vez que el fragmento se hace visible
        cargarPublicacionesDesdeBackendless();
    }

    /**
     * Método público para ser llamado desde la Activity o PublicarFragment
     * para forzar la recarga de la lista.
     */
    public void refreshPosts() {
        cargarPublicacionesDesdeBackendless();
    }

    private void cargarPublicacionesDesdeBackendless() {
        // Mostrar el ProgressBar mientras se cargan los datos
        progressBar.setVisibility(View.VISIBLE);

        // Crear una consulta para obtener las publicaciones
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC"); // Ordenar por fecha de creación descendente

        // Obtener las publicaciones de la tabla "Publicaciones" en Backendless
        Backendless.Data.of(Publicaciones.class).find(queryBuilder, new AsyncCallback<List<Publicaciones>>() {
            @Override
            public void handleResponse(List<Publicaciones> foundPublicaciones) {
                // Ocultar el ProgressBar en caso de éxito
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
