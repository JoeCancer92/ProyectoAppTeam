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
// CORRECCIÓN DEFINITIVA: Se importa la clase EventHandler desde el paquete correcto 'rt.data'.
import com.backendless.rt.data.EventHandler;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.NotificacionAdapter;
import com.example.proyectoappteam.clases.Notificaciones;
import com.example.proyectoappteam.actividades.DetallePublicacionActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificacionFragment extends Fragment {

    private static final String TAG = "NotificacionFragment";

    private RecyclerView rvNotificaciones;
    private TextView tvVacio;
    private ImageButton btnRefresh;
    private ProgressBar pbLoading;

    private NotificacionAdapter adapter;
    private final List<Notificaciones> listaNotificaciones = new ArrayList<>();

    private static final int PAGE_SIZE = 50;
    private int offset = 0;
    private boolean loading = false;
    private boolean noMore = false;
    private RecyclerView.OnScrollListener scrollListener;

    private EventHandler<Notificaciones> rtListenerHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacion, container, false);

        rvNotificaciones = view.findViewById(R.id.rv_notificaciones);
        tvVacio = view.findViewById(R.id.tv_notificaciones_vacio);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        pbLoading = view.findViewById(R.id.pb_loading);

        btnRefresh.setOnClickListener(v -> refrescar());

        configurarRecyclerView();
        cargarNotificaciones(true);

        suscribirANotificacionesEnTiempoReal();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rvNotificaciones != null && scrollListener != null) {
            rvNotificaciones.removeOnScrollListener(scrollListener);
        }

        if (rtListenerHandler != null) {
            // CORRECCIÓN: El método correcto para esta clase es 'removeCreateListeners'.
            rtListenerHandler.removeCreateListeners();
        }

        btnRefresh = null;
        pbLoading = null;
        rvNotificaciones = null;
        tvVacio = null;
        adapter = null;
        scrollListener = null;
    }

    public void refrescar() {
        if (pbLoading != null && btnRefresh != null) {
            pbLoading.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.GONE);
        }
        cargarNotificaciones(true);
    }

    private void configurarRecyclerView() {
        adapter = new NotificacionAdapter(requireContext(), listaNotificaciones, n -> {
            if (Boolean.TRUE.equals(n.getLeida())) {
                abrirDetalle(n);
                return;
            }
            n.setLeida(true);
            Backendless.Data.of(Notificaciones.class).save(n, new AsyncCallback<Notificaciones>() {
                @Override public void handleResponse(Notificaciones actualizado) {
                    adapter.updateItem(actualizado);
                    abrirDetalle(actualizado);
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
            onCargaFinalizada();
            return;
        }
        if (loading) return;
        loading = true;
        if (reset) {
            listaNotificaciones.clear();
            adapter.notifyDataSetChanged();
            offset = 0;
            noMore = false;
        }

        String userId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "userReceptor.objectId = '" + userId + "'";

        DataQueryBuilder qb = DataQueryBuilder.create();
        qb.setWhereClause(whereClause);
        qb.setRelated(new String[]{"publicacionId", "usuarioEmisorId"});
        qb.setRelationsDepth(1);
        qb.setSortBy("timestamposimulado DESC");

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

    private void suscribirANotificacionesEnTiempoReal() {
        String userId = Backendless.UserService.CurrentUser() != null ? Backendless.UserService.CurrentUser().getObjectId() : null;
        if (userId == null) {
            Log.e(TAG, "No se puede suscribir a tiempo real: usuario no logueado.");
            return;
        }

        rtListenerHandler = Backendless.Data.of(Notificaciones.class).rt();

        AsyncCallback<Notificaciones> createListener = new AsyncCallback<Notificaciones>() {
            @Override
            public void handleResponse(Notificaciones nuevaNotificacion) {
                if (getActivity() != null) {
                    // Filtramos en el cliente para asegurarnos que la notificación es para nosotros
                    String currentUserId = Backendless.UserService.CurrentUser().getObjectId();
                    if (nuevaNotificacion.getUserReceptor() != null && currentUserId.equals(nuevaNotificacion.getUserReceptor().getObjectId())) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            recargarNotificacionYAnadir(nuevaNotificacion.getObjectId());
                        });
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error en listener de tiempo real (create): " + fault.getMessage());
            }
        };

        // CORRECCIÓN: Se usa el método addCreateListener, que es el correcto.
        String whereClause = "userReceptor.objectId = '" + userId + "'";
        rtListenerHandler.addCreateListener(whereClause, createListener);

        Log.i(TAG, "Suscrito a notificaciones en tiempo real.");
    }

    private void recargarNotificacionYAnadir(String notificacionId) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.addRelated("publicacionId");
        queryBuilder.addRelated("usuarioEmisorId");

        Backendless.Data.of(Notificaciones.class).findById(notificacionId, queryBuilder, new AsyncCallback<Notificaciones>() {
            @Override
            public void handleResponse(Notificaciones notificacionCompleta) {
                if (isAdded() && adapter != null && rvNotificaciones != null && notificacionCompleta != null) {
                    listaNotificaciones.add(0, notificacionCompleta);
                    adapter.notifyItemInserted(0);
                    rvNotificaciones.scrollToPosition(0);
                    validarListaVacia();

                    Toast.makeText(getContext(), "¡Tienes una nueva notificación!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al recargar la notificación en tiempo real para obtener sus detalles: " + fault.getMessage());
            }
        });
    }

    private void onCargaFinalizada() {
        if (pbLoading != null && btnRefresh != null) {
            pbLoading.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void validarListaVacia() {
        boolean vacia = listaNotificaciones.isEmpty();
        if (tvVacio != null) tvVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
        if (rvNotificaciones != null) rvNotificaciones.setVisibility(vacia ? View.GONE : View.VISIBLE);
    }

    private void abrirDetalle(Notificaciones n) {
        if (n.getPublicacionId() != null && n.getPublicacionId().getObjectId() != null) {
            Intent i = new Intent(requireContext(), DetallePublicacionActivity.class);
            i.putExtra("PUBLICACION_ID", n.getPublicacionId().getObjectId());
            startActivity(i);
        } else {
            Toast.makeText(requireContext(), "La publicación ya no está disponible.", Toast.LENGTH_SHORT).show();
        }
    }
}