package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.NotificacionAdapter;
import com.example.proyectoappteam.clases.Notificaciones;

import java.util.List;

// ESTE ES EL ARCHIVO CORRECTO (SINGULAR)
public class NotificacionFragment extends Fragment {

    private RecyclerView rvNotificaciones;
    private NotificacionAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageButton btnRefresh;

    public NotificacionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Usando TU layout original: fragment_notificacion.xml
        return inflater.inflate(R.layout.fragment_notificacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Usando los IDs de TU layout
        rvNotificaciones = view.findViewById(R.id.rv_notificaciones);
        progressBar = view.findViewById(R.id.pb_loading);
        tvEmpty = view.findViewById(R.id.tv_notificaciones_vacio);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        rvNotificaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        // Listener para tu botón de refrescar
        btnRefresh.setOnClickListener(v -> cargarNotificaciones());

        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        // Lógica de UI para la carga
        btnRefresh.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvNotificaciones.setVisibility(View.GONE);

        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Error: Usuario no encontrado.");
            return;
        }

        String userId = currentUser.getObjectId();
        String whereClause = "userReceptor.objectId = '" + userId + "'";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setRelationsDepth(1);

        Backendless.Data.of(Notificaciones.class).find(queryBuilder, new AsyncCallback<List<Notificaciones>>() {
            @Override
            public void handleResponse(List<Notificaciones> response) {
                progressBar.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.VISIBLE);
                if (response == null || response.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    // Usando tu adaptador
                    adapter = new NotificacionAdapter(getContext(), response);
                    rvNotificaciones.setAdapter(adapter);
                    rvNotificaciones.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressBar.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar notificaciones.");
                Toast.makeText(getContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
