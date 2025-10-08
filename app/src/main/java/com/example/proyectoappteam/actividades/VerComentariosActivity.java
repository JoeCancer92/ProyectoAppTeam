package com.example.proyectoappteam.actividades;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.ComentarioAdapter;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.fragmentos.CrearComentarioFragment;

import java.util.ArrayList;
import java.util.List;

public class VerComentariosActivity extends AppCompatActivity
        implements CrearComentarioFragment.ComentarioListener {

    public static final String EXTRA_PUBLICACION_ID = "publicacionId";
    private RecyclerView recyclerView;
    private ComentarioAdapter adapter;
    private List<Comentarios> comentariosList;
    private ProgressBar progressBar;
    private static final String TAG = "ComentariosActivity";
    private String currentPublicacionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_comentarios);

        recyclerView = findViewById(R.id.recycler_view_comentarios);
        progressBar = findViewById(R.id.progressBar_comentarios);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        comentariosList = new ArrayList<>();

        adapter = new ComentarioAdapter(this, comentariosList);
        recyclerView.setAdapter(adapter);

        // Obtener la ID de la publicación del Intent
        currentPublicacionId = getIntent().getStringExtra(EXTRA_PUBLICACION_ID);

        if (currentPublicacionId != null && !currentPublicacionId.isEmpty()) {
            Log.i(TAG, "ID de Publicación recibida: " + currentPublicacionId);
            cargarComentarios(currentPublicacionId);
        } else {
            Toast.makeText(this, "Error: ID de publicación no encontrada.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Metodo de la interfaz: Se llama desde el Fragmento para forzar la recarga
    @Override
    public void onComentarioEnviado() {
        Log.i(TAG, "Notificación de envío recibida. Recargando comentarios...");
        if (currentPublicacionId != null && !currentPublicacionId.isEmpty()) {
            cargarComentarios(currentPublicacionId);
        }
    }

    private void cargarComentarios(String publicacionId) {
        progressBar.setVisibility(View.VISIBLE);

        // CORRECCIÓN CRÍTICA DE CONSULTA (WHERE CLAUSE):
        // Se usa 'publicacion.objectId' para filtrar correctamente por el puntero de relación.
        String whereClause = "publicacion.objectId = '" + publicacionId + "'";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setSortBy("created ASC");
        queryBuilder.setRelationsDepth(1); // Para cargar datos relacionados (como el usuario que comentó)

        Backendless.Data.of(Comentarios.class).find(queryBuilder, new AsyncCallback<List<Comentarios>>() {
            @Override
            public void handleResponse(List<Comentarios> foundComentarios) {
                progressBar.setVisibility(View.GONE);

                if (foundComentarios != null && !foundComentarios.isEmpty()) {
                    comentariosList.clear();
                    comentariosList.addAll(foundComentarios);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "Comentarios encontrados: " + foundComentarios.size());
                } else {
                    Log.i(TAG, "Comentarios encontrados: 0. Mostrando mensaje de vacío.");
                    comentariosList.clear();
                    adapter.notifyDataSetChanged();
                    // Puedes mostrar un mensaje visible si es necesario
                    // Toast.makeText(VerComentariosActivity.this, "No hay comentarios aún para esta publicación.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al cargar comentarios (FAULT): " + fault.getMessage());
                Toast.makeText(VerComentariosActivity.this, "Error al cargar comentarios: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}