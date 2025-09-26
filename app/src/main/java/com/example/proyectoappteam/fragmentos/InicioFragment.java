package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Importar ImageButton
import android.widget.ProgressBar;
import android.widget.TextView; // Importar TextView (para el título si se necesita)
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.PublicacionAdapter;
import com.example.proyectoappteam.clases.Publicaciones;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private RecyclerView recyclerView;
    private PublicacionAdapter adapter;
    private List<Publicaciones> publicacionesList;
    private ProgressBar progressBar;
    private ImageButton btnRefresh; // Declaración del ImageButton

    // Si necesitas referenciar el título para algo, aunque no es necesario para el refresh
    // private TextView tvNoticiasComunidad;

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

        // 1. Enlazar el botón de refrescar
        btnRefresh = view.findViewById(R.id.btn_refresh);

        // Inicializar el adaptador con una lista vacía
        publicacionesList = new ArrayList<>();
        adapter = new PublicacionAdapter(publicacionesList);
        recyclerView.setAdapter(adapter);

        // 2. Asignar el listener de click al botón
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Actualizando publicaciones...", Toast.LENGTH_SHORT).show();
                refreshPosts(); // Llama al método de recarga
            });
        }

        return view;
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
                    // Limpiar la lista actual y añadir las nuevas publicaciones
                    publicacionesList.clear();
                    publicacionesList.addAll(foundPublicaciones);
                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged();
                } else {
                    // Limpiar la lista y mostrar mensaje si no hay publicaciones
                    publicacionesList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "No hay publicaciones para mostrar.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Ocultar el ProgressBar en caso de error
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al cargar publicaciones: " + fault.getMessage());
                Toast.makeText(getContext(), "Error al cargar las publicaciones: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}