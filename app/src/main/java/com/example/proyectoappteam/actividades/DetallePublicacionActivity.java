package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Calificaciones;
import com.example.proyectoappteam.clases.Comentarios;
import com.example.proyectoappteam.clases.InteraccionItem;
import com.example.proyectoappteam.clases.InteraccionesAdapter;
import com.example.proyectoappteam.clases.Publicaciones;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Muestra el detalle de una publicación.
 * - Si eres el dueño: botón "Ver interacciones" → muestra comentarios y calificaciones en tarjetas.
 * - Si no eres el dueño: botón "Contactar al dueño".
 */
public class DetallePublicacionActivity extends AppCompatActivity {

    private static final String TAG = "DetallePublicacionAct";

    // UI
    private TextView tvTitulo, tvUsuarioFecha, tvUrgente, tvCategoria, tvDescripcion, tvUbicacion, tvFotosPlaceholder;
    private Button btnAccion;
    private TextView tvSinInteracciones;
    private RecyclerView rvInteracciones;

    // Datos
    private Publicaciones publicacionActual;
    private final List<InteraccionItem> listaInteracciones = new ArrayList<>();
    private InteraccionesAdapter interaccionesAdapter;

    // Carga asíncrona
    private boolean cargaronComentarios = false;
    private boolean cargaronCalificaciones = false;
    private List<Comentarios> cacheComentarios = new ArrayList<>();
    private List<Calificaciones> cacheCalificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_publicacion);

        // UI
        tvTitulo = findViewById(R.id.tv_detalle_titulo);
        tvUsuarioFecha = findViewById(R.id.tv_detalle_usuario_fecha);
        tvUrgente = findViewById(R.id.tv_detalle_urgente);
        tvCategoria = findViewById(R.id.tv_detalle_categoria);
        tvDescripcion = findViewById(R.id.tv_detalle_descripcion);
        tvUbicacion = findViewById(R.id.tv_detalle_ubicacion);
        tvFotosPlaceholder = findViewById(R.id.tv_fotos_placeholder);
        btnAccion = findViewById(R.id.btn_interaccion);
        tvSinInteracciones = findViewById(R.id.tv_sin_interacciones);
        rvInteracciones = findViewById(R.id.rv_interacciones);

        rvInteracciones.setLayoutManager(new LinearLayoutManager(this));
        interaccionesAdapter = new InteraccionesAdapter(listaInteracciones);
        rvInteracciones.setAdapter(interaccionesAdapter);

        // ID de la publicación
        String publicacionId = getIntent().getStringExtra("PUBLICACION_ID");
        if (publicacionId != null) {
            cargarDetallePublicacion(publicacionId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID de la publicación.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void cargarDetallePublicacion(String objectId) {
        tvDescripcion.setText("Cargando detalles...");

        Backendless.Data.of(Publicaciones.class).findById(objectId, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones publicacion) {
                publicacionActual = publicacion;
                actualizarUI(publicacion);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar la publicación: " + fault.getMessage());
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalles.", Toast.LENGTH_LONG).show();
                tvDescripcion.setText("Error al cargar la publicación.");
            }
        });
    }

    private void actualizarUI(Publicaciones p) {
        // Título y descripción
        tvTitulo.setText(getString(R.string.detalle_publicacion_title));
        tvDescripcion.setText(p.getDescripcion());

        // Categoría
        tvCategoria.setText("Categoría: " + (p.getCategoria() == null ? "Sin categoría" : p.getCategoria()));

        // Urgente
        tvUrgente.setVisibility(p.getEsUrgente() != null && p.getEsUrgente() ? View.VISIBLE : View.GONE);

        // Fecha
        if (p.getCreated() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvUsuarioFecha.setText("Publicado el: " + sdf.format(p.getCreated()));
        } else {
            tvUsuarioFecha.setText("Fecha desconocida");
        }

        // Ubicación
        String ubicacionTexto = (p.getUbicacion() != null && !p.getUbicacion().isEmpty())
                ? p.getUbicacion()
                : String.format(Locale.getDefault(),
                "Coordenadas: Lat %.4f, Lon %.4f", p.getLatitud(), p.getLongitud());
        tvUbicacion.setText("Ubicación: " + ubicacionTexto);

        // Fotos
        cargarImagenesPlaceholder(p.getFotos());

        // Acción según quién es
        BackendlessUser cu = Backendless.UserService.CurrentUser();
        String currentUserId = cu != null ? cu.getObjectId() : null;

        if (currentUserId != null && currentUserId.equals(p.getOwnerId())) {
            btnAccion.setText("Ver interacciones");
            btnAccion.setOnClickListener(v -> cargarInteracciones(p.getObjectId()));
        } else {
            btnAccion.setText("Contactar al dueño");
            btnAccion.setOnClickListener(v -> contactarUsuario(p));
        }
    }

    private void cargarImagenesPlaceholder(String fotosUrls) {
        if (fotosUrls != null && !fotosUrls.isEmpty()) {
            int count = fotosUrls.split(",").length;
            tvFotosPlaceholder.setText(String.format(Locale.getDefault(),
                    "Imágenes adjuntas: %d foto(s)", count));
        } else {
            tvFotosPlaceholder.setText("No hay imágenes adjuntas.");
        }
    }

    private void contactarUsuario(Publicaciones p) {
        String ownerId = p.getOwnerId();
        if (ownerId == null) {
            Toast.makeText(this, "No se pudo obtener el propietario.", Toast.LENGTH_SHORT).show();
            return;
        }

        Backendless.Data.of(BackendlessUser.class).findById(ownerId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser owner) {
                String userEmail = owner.getEmail();
                if (userEmail != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre tu publicación");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(DetallePublicacionActivity.this,
                                "No se encontró app de correo.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(DetallePublicacionActivity.this,
                        "Error al obtener el propietario: " + fault.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Carga comentarios y calificaciones por publicacionId (usando ownerId). */
    private void cargarInteracciones(String publicacionId) {
        cargaronComentarios = false;
        cargaronCalificaciones = false;
        cacheComentarios.clear();
        cacheCalificaciones.clear();
        listaInteracciones.clear();
        interaccionesAdapter.notifyDataSetChanged();
        validarInteracciones();

        // Comentarios
        String whereComentarios = "publicacion.objectId = '" + publicacionId + "'";
        DataQueryBuilder qbComentarios = DataQueryBuilder.create();
        qbComentarios.setWhereClause(whereComentarios);

        Backendless.Data.of(Comentarios.class).find(qbComentarios, new AsyncCallback<List<Comentarios>>() {
            @Override
            public void handleResponse(List<Comentarios> comentarios) {
                cacheComentarios = comentarios != null ? comentarios : new ArrayList<>();
                cargaronComentarios = true;
                intentarUnificarYMostrar();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error cargando comentarios: " + fault.getMessage());
                cargaronComentarios = true;
                intentarUnificarYMostrar();
            }
        });

        // Calificaciones
        String whereCalificaciones = "publicacion.objectId = '" + publicacionId + "'";
        DataQueryBuilder qbCalif = DataQueryBuilder.create();
        qbCalif.setWhereClause(whereCalificaciones);

        Backendless.Data.of(Calificaciones.class).find(qbCalif, new AsyncCallback<List<Calificaciones>>() {
            @Override
            public void handleResponse(List<Calificaciones> calificaciones) {
                cacheCalificaciones = calificaciones != null ? calificaciones : new ArrayList<>();
                cargaronCalificaciones = true;
                intentarUnificarYMostrar();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error cargando calificaciones: " + fault.getMessage());
                cargaronCalificaciones = true;
                intentarUnificarYMostrar();
            }
        });
    }

    /** Une comentarios y calificaciones por usuario y los pasa al adapter. */
    private void intentarUnificarYMostrar() {
        if (!(cargaronComentarios && cargaronCalificaciones)) return;

        // Reunir todos los ownerId
        Set<String> ids = new LinkedHashSet<>();
        for (Comentarios c : cacheComentarios) {
            if (c.getOwnerId() != null && !c.getOwnerId().isEmpty()) ids.add(c.getOwnerId());
        }
        for (Calificaciones cal : cacheCalificaciones) {
            if (cal.getOwnerId() != null && !cal.getOwnerId().isEmpty()) ids.add(cal.getOwnerId());
        }

        if (ids.isEmpty()) {
            construirInteracciones(new LinkedHashMap<>());
            return;
        }

        // Traer Users
        StringBuilder in = new StringBuilder("(");
        int i = 0;
        for (String id : ids) {
            if (i++ > 0) in.append(",");
            in.append("'").append(id).append("'");
        }
        in.append(")");

        DataQueryBuilder qbUsers = DataQueryBuilder.create();
        qbUsers.setWhereClause("objectId IN " + in);

        Backendless.Data.of(BackendlessUser.class).find(qbUsers, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> users) {
                Map<String, BackendlessUser> mapUsers = new LinkedHashMap<>();
                if (users != null) {
                    for (BackendlessUser u : users) {
                        mapUsers.put(u.getObjectId(), u);
                    }
                }
                construirInteracciones(mapUsers);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error cargando Users: " + fault.getMessage());
                construirInteracciones(new LinkedHashMap<>());
            }
        });
    }

    private void construirInteracciones(Map<String, BackendlessUser> mapUsers) {
        Map<String, InteraccionItem> porUsuario = new LinkedHashMap<>();

        // Comentarios
        for (Comentarios c : cacheComentarios) {
            String uid = c.getOwnerId() != null ? c.getOwnerId() : "desconocido";
            InteraccionItem item = porUsuario.get(uid);
            if (item == null) {
                item = crearItemDesdeUser(mapUsers.get(uid));
                porUsuario.put(uid, item);
            }
            if (c.getTexto() != null && !c.getTexto().isEmpty()) {
                item.setComentario(c.getTexto());
            }
        }

        // Calificaciones
        for (Calificaciones cal : cacheCalificaciones) {
            String uid = cal.getOwnerId() != null ? cal.getOwnerId() : "desconocido";
            InteraccionItem item = porUsuario.get(uid);
            if (item == null) {
                item = crearItemDesdeUser(mapUsers.get(uid));
                porUsuario.put(uid, item);
            }
            item.setPuntuacion(cal.getPuntuacion());
        }

        // Pasar al adapter
        listaInteracciones.clear();
        listaInteracciones.addAll(porUsuario.values());
        interaccionesAdapter.notifyDataSetChanged();
        validarInteracciones();
    }

    private InteraccionItem crearItemDesdeUser(BackendlessUser u) {
        InteraccionItem item = new InteraccionItem();
        if (u != null) {
            String nombre = (String) u.getProperty("nombre");
            String apellidos = (String) u.getProperty("apellidos");
            String email = u.getEmail();
            item.setNombre(((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "")).trim());
            item.setCorreo(email);
        } else {
            item.setNombre("Usuario desconocido");
            item.setCorreo("");
        }
        return item;
    }

    private void validarInteracciones() {
        if (listaInteracciones.isEmpty()) {
            tvSinInteracciones.setVisibility(View.VISIBLE);
            rvInteracciones.setVisibility(View.GONE);
        } else {
            tvSinInteracciones.setVisibility(View.GONE);
            rvInteracciones.setVisibility(View.VISIBLE);
        }
    }
}