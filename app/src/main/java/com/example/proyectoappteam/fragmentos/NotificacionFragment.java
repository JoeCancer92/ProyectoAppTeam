package com.example.proyectoappteam.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.proyectoappteam.clases.NotificacionAdapter;
import com.example.proyectoappteam.clases.Notificaciones;
import com.example.proyectoappteam.actividades.DetallePublicacionActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificacionFragment extends Fragment {

    private static final String TAG = "NotificacionFragment";

    private RecyclerView rvNotificaciones;
    private TextView tvVacio;
    // Vistas añadidas para la funcionalidad de refresco
    private ImageButton btnRefresh;
    private ProgressBar pbLoading;

    private NotificacionAdapter adapter;
    private final List<Notificaciones> listaNotificaciones = new ArrayList<>();

    // paginación
    private static final int PAGE_SIZE = 50;
    private int offset = 0;
    private boolean loading = false;
    private boolean noMore = false;

    // listener para paginar; lo guardamos para removerlo en onDestroyView
    private RecyclerView.OnScrollListener scrollListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacion, container, false);

        rvNotificaciones = view.findViewById(R.id.rv_notificaciones);
        tvVacio = view.findViewById(R.id.tv_notificaciones_vacio);
        // Inicializar nuevas vistas
        btnRefresh = view.findViewById(R.id.btn_refresh);
        pbLoading = view.findViewById(R.id.pb_loading);


        // Configuración de Listener para el botón de refresco
        btnRefresh.setOnClickListener(v -> refrescar());

        configurarRecyclerView();
        cargarNotificaciones(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Si quieres refrescar siempre al volver, descomenta:
        // refrescar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rvNotificaciones != null && scrollListener != null) {
            rvNotificaciones.removeOnScrollListener(scrollListener);
        }
        // Limpiamos las referencias para evitar fugas de memoria
        btnRefresh = null;
        pbLoading = null;
        rvNotificaciones = null;
        tvVacio = null;
        adapter = null;
        scrollListener = null;
    }

    /** Llamable desde afuera o por el botón para forzar un refresh */
    public void refrescar() {
        // Muestra el indicador de carga en el encabezado
        if (pbLoading != null && btnRefresh != null) {
            pbLoading.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.GONE);
        }
        cargarNotificaciones(true);
    }

    private void configurarRecyclerView() {
        adapter = new NotificacionAdapter(requireContext(), listaNotificaciones, n -> {
            // Al pulsar: marcar como leída (si no lo está) y abrir detalle
            if (Boolean.TRUE.equals(n.getLeida())) {
                abrirDetalle(n);
                return;
            }
            n.setLeida(true);
            Backendless.Data.of(Notificaciones.class).save(n, new AsyncCallback<Notificaciones>() {
                @Override public void handleResponse(Notificaciones actualizado) {
                    adapter.updateItem(actualizado);
                    abrirDetalle(actualizado);
                    // TIP: aquí puedes disparar la actualización de badge en tu Activity si la tienes
                }
                @Override public void handleFault(BackendlessFault fault) {
                    Log.e(TAG, "No se pudo marcar como leída: " + fault.getMessage());
                    if (isAdded()) Toast.makeText(requireContext(), "Error al marcar como leída", Toast.LENGTH_SHORT).show();
                }
            });
        });

        rvNotificaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotificaciones.setAdapter(adapter);

        scrollListener = new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || loading || noMore) return;
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int lastVisible = lm.findLastVisibleItemPosition();
                if (lastVisible >= listaNotificaciones.size() - 5) {
                    cargarNotificaciones(false);
                }
            }
        };
        rvNotificaciones.addOnScrollListener(scrollListener);
    }

    private void cargarNotificaciones(boolean reset) {
        if (Backendless.UserService.CurrentUser() == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar notificaciones.");
            validarListaVacia();
            // Asegurarse de ocultar el ProgressBar si falla
            onCargaFinalizada();
            return;
        }
        if (loading) return;
        loading = true;
        // Solo mostrar el ProgressBar al inicio de una carga completa (por refresh)
        if (reset) {
            // Se hace la gestión del ProgressBar en refrescar()
            listaNotificaciones.clear();
            adapter.notifyDataSetChanged();
            offset = 0;
            noMore = false;
        }


        String userId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "userReceptor.objectId = '" + userId + "'";

        DataQueryBuilder qb = DataQueryBuilder.create();
        qb.setWhereClause(whereClause);

        // RELACIONES (nombres EXACTOS de la tabla)
        qb.setRelated(new String[]{"publicacionId", "usuarioEmisorId"});
        // Algunos SDK requieren profundidad de relaciones explícita
        qb.setRelationsDepth(1);

        // Orden preferido por timestamp simulado
        qb.setSortBy(Collections.singletonList("timestamposimulado DESC"));
        // Si tuvieras registros viejos sin timestamposimulado y quisieras usar created, cambia a:
        // qb.setSortBy(Collections.singletonList("created DESC"));

        // paginación
        qb.setPageSize(PAGE_SIZE);
        qb.setOffset(offset);

        Backendless.Data.of(Notificaciones.class).find(qb, new AsyncCallback<List<Notificaciones>>() {
            @Override
            public void handleResponse(List<Notificaciones> found) {
                loading = false;

                if (found != null && !found.isEmpty()) {
                    int oldSize = listaNotificaciones.size();
                    listaNotificaciones.addAll(found);
                    adapter.notifyItemRangeInserted(oldSize, found.size());
                    offset += found.size();
                    if (found.size() < PAGE_SIZE) noMore = true;
                } else {
                    noMore = true;
                }

                validarListaVacia();
                onCargaFinalizada();
                Log.i(TAG, "Notificaciones cargadas: " + (found != null ? found.size() : 0));
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                loading = false;
                Log.e(TAG, "Error al cargar notificaciones: " + fault.getMessage());
                if (isAdded()) Toast.makeText(requireContext(), "Error al cargar notificaciones", Toast.LENGTH_SHORT).show();

                validarListaVacia();
                onCargaFinalizada();
            }
        });
    }

    /** Gestiona la visibilidad del ProgressBar y el botón de Refresh al finalizar la carga. */
    private void onCargaFinalizada() {
        if (pbLoading != null && btnRefresh != null) {
            pbLoading.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void validarListaVacia() {
        boolean vacia = listaNotificaciones.isEmpty();
        // Null checks for safety, though onCreateView should ensure they aren't null
        if (tvVacio != null) tvVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
        if (rvNotificaciones != null) rvNotificaciones.setVisibility(vacia ? View.GONE : View.VISIBLE);
    }

    private void abrirDetalle(Notificaciones n) {
        // Verificar si la notificación tiene relación con una publicación
        if (n.getPublicacionId() != null && n.getPublicacionId().getObjectId() != null) {

            // Crear Intent para abrir la actividad de detalle
            Intent i = new Intent(requireContext(), DetallePublicacionActivity.class);

            // Pasar el ID de la publicación
            i.putExtra("PUBLICACION_ID", n.getPublicacionId().getObjectId());

            startActivity(i);

        } else {
            // Mostrar mensaje si la publicación ya no existe
            Toast.makeText(requireContext(),
                    "La publicación ya no está disponible.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}