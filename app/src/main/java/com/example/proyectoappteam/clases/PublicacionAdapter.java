package com.example.proyectoappteam.clases;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.FragmentManager;
import com.example.proyectoappteam.fragmentos.CrearComentarioFragment;
import com.example.proyectoappteam.fragmentos.CrearCalificacionFragment;
import com.example.proyectoappteam.fragmentos.CrearCalificacionFragment.CalificacionListener;
import com.example.proyectoappteam.actividades.MapsActivity;
import com.example.proyectoappteam.actividades.VerComentariosActivity;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.actividades.PhotoSliderActivity;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private static final String TAG = "PublicacionAdapter";
    private List<Publicaciones> publicaciones;
    private final FragmentManager fragmentManager;
    private final CalificacionListener calificacionListener;

    /**
     * Constructor modificado para recibir FragmentManager y CalificacionListener.
     */
    public PublicacionAdapter(List<Publicaciones> publicaciones, FragmentManager fragmentManager, CalificacionListener listener) {
        this.publicaciones = publicaciones;
        this.fragmentManager = fragmentManager;
        this.calificacionListener = listener;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicaciones publicacion = publicaciones.get(position);
        final Context context = holder.itemView.getContext();

        holder.tvDescripcion.setText(publicacion.getDescripcion());
        holder.tvUbicacion.setText(publicacion.getUbicacion());

        // LÓGICA DE LA FECHA
        Date fechaCreacion = publicacion.getCreated();
        if (fechaCreacion != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFechaPublicacion.setText(sdf.format(fechaCreacion));
        } else {
            holder.tvFechaPublicacion.setText("Fecha no disponible");
        }

        // Manejar el estado de urgencia
        if (publicacion.getEsUrgente() != null && publicacion.getEsUrgente()) {
            holder.tvEsUrgente.setVisibility(View.VISIBLE);
        } else {
            holder.tvEsUrgente.setVisibility(View.GONE);
        }

        // Lógica para el botón "Ver Fotos"
        if (publicacion.getFotos() != null && !publicacion.getFotos().isEmpty()) {
            holder.btnVerFotos.setVisibility(View.VISIBLE);
            holder.btnVerFotos.setOnClickListener(v -> {
                String fotosString = publicacion.getFotos();
                Intent intent = new Intent(context, PhotoSliderActivity.class);
                intent.putExtra(PhotoSliderActivity.EXTRA_PHOTO_URLS, fotosString);
                context.startActivity(intent);
            });
        } else {
            holder.btnVerFotos.setVisibility(View.GONE);
        }

        // LÓGICA PARA EL BOTÓN "VER UBICACIÓN"
        if (publicacion.getLatitud() != null && publicacion.getLongitud() != null &&
                publicacion.getLatitud() != 0.0 && publicacion.getLongitud() != 0.0) {

            holder.btnVerUbicacion.setVisibility(View.VISIBLE);
            holder.btnVerUbicacion.setOnClickListener(v -> {
                Intent mapIntent = new Intent(context, MapsActivity.class);
                mapIntent.putExtra("latitud", publicacion.getLatitud());
                mapIntent.putExtra("longitud", publicacion.getLongitud());
                String markerTitle = publicacion.getUbicacion() != null && !publicacion.getUbicacion().isEmpty() ?
                        publicacion.getUbicacion() :
                        publicacion.getDescripcion();
                mapIntent.putExtra("markerTitle", markerTitle);
                context.startActivity(mapIntent);
            });
        } else {
            holder.btnVerUbicacion.setVisibility(View.GONE);
        }

        cargarDatosUsuario(publicacion.getOwnerId(), holder.tvNombreUsuario, holder.tvCorreoUsuario, holder.ivImagenPerfil);




        final String currentUserId = Backendless.UserService.CurrentUser() != null ? Backendless.UserService.CurrentUser().getObjectId() : null;

        if (currentUserId != null) {
            holder.btnCalificar.setVisibility(View.VISIBLE);

            // Asignamos el listener del diálogo al NUEVO BOTÓN
            holder.btnCalificar.setOnClickListener(v -> {
                Log.i(TAG, "Botón CALIFICAR clickeado para mostrar diálogo.");
                mostrarDialogoCalificacion(publicacion.getObjectId(), context);
            });

        } else {
            // Si no hay usuario logueado, ocultamos el botón de calificar
            holder.btnCalificar.setVisibility(View.GONE);
            Log.w(TAG, "Botón Calificar deshabilitado: No hay usuario logueado.");
        }
        // *******************************************************************

        // LÓGICA PARA ABRIR EL DIÁLOGO DE CREAR COMENTARIO (btnComentar)
        holder.btnComentar.setOnClickListener(v -> {
            String publicacionId = publicacion.getObjectId();
            Log.i(TAG, "Botón de comentar clickeado para Publicación ID: " + publicacionId);

            if (fragmentManager != null && publicacionId != null && !publicacionId.isEmpty()) {
                CrearComentarioFragment dialog = CrearComentarioFragment.newInstance(publicacionId);
                dialog.show(fragmentManager, "CrearComentarioDialog");
            } else {
                Toast.makeText(context, "Error: No se puede comentar (Falta FragmentManager o Publicación ID).", Toast.LENGTH_LONG).show();
            }
        });

        // LÓGICA PARA EL BOTÓN DE VER COMENTARIOS (btnVerComentarios)
        holder.btnVerComentarios.setOnClickListener(v -> {
            String publicacionId = publicacion.getObjectId();
            Log.i(TAG, "Botón de ver comentarios clickeado para Publicación ID: " + publicacionId);

            if (publicacionId != null && !publicacionId.isEmpty()) {
                Intent intent = new Intent(context, VerComentariosActivity.class);
                // Usamos la constante definida en VerComentariosActivity para la clave
                intent.putExtra(VerComentariosActivity.EXTRA_PUBLICACION_ID, publicacionId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Error: ID de publicación no disponible para ver comentarios.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra el diálogo de calificación.
     * @param publicacionId El objectId de la publicación a calificar.
     */
    private void mostrarDialogoCalificacion(String publicacionId, Context context) {
        if (fragmentManager == null || publicacionId == null || calificacionListener == null) {
            Toast.makeText(context, "Error al lanzar la calificación. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una nueva instancia del fragmento de diálogo, pasándole el ID
        CrearCalificacionFragment dialogFragment = CrearCalificacionFragment.newInstance(publicacionId);

        // Asignar el listener del InicioFragment al diálogo para la recarga
        dialogFragment.setCalificacionListener(calificacionListener);

        // Mostrar el diálogo usando el FragmentManager
        dialogFragment.show(fragmentManager, "CrearCalificacionTag");
        Log.d(TAG, "Lanzando diálogo de calificación para ID: " + publicacionId);
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }


    private void cargarDatosUsuario(String userId, TextView tvNombreUsuario, TextView tvCorreoUsuario, ImageView ivImagenPerfil) {
        Backendless.Data.of(BackendlessUser.class).findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                if (user != null) {
                    String nombre = (String) user.getProperty("nombre");
                    String apellidos = (String) user.getProperty("apellidos");
                    String email = user.getEmail();
                    tvNombreUsuario.setText(String.format("%s %s", nombre, apellidos));
                    tvCorreoUsuario.setText(email);
                    String fotoUrl = (String) user.getProperty("urlfoto");
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        new DownloadImageTask(ivImagenPerfil).execute(fotoUrl);
                    } else {
                        ivImagenPerfil.setImageResource(R.drawable.ic_default_profile);
                    }
                } else {
                    tvNombreUsuario.setText("Usuario Desconocido");
                    tvCorreoUsuario.setText("");
                    ivImagenPerfil.setImageResource(R.drawable.ic_default_profile);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar datos del usuario: " + fault.getMessage());
                tvNombreUsuario.setText("Error");
                tvCorreoUsuario.setText("");
                ivImagenPerfil.setImageResource(R.drawable.ic_default_profile);
            }
        });
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                URL url = new URL(urldisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
            } else {
                bmImage.setImageResource(R.drawable.ic_default_profile);
            }
        }
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagenPerfil;
        TextView tvDescripcion;
        TextView tvNombreUsuario;
        TextView tvCorreoUsuario;
        TextView tvUbicacion;
        public TextView tvFechaPublicacion;
        // ELIMINADO: RatingBar ratingBar;
        // ELIMINADO: public TextView tvPromedio;

        public ImageButton btnComentar;
        public ImageButton btnVerFotos;
        public ImageButton btnVerUbicacion;
        public ImageButton btnVerComentarios;
        public ImageButton btnCalificar;

        TextView tvEsUrgente;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagenPerfil = itemView.findViewById(R.id.iv_user_profile);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_publicacion);
            tvNombreUsuario = itemView.findViewById(R.id.tv_user_name);
            tvCorreoUsuario = itemView.findViewById(R.id.tv_user_email);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion_publicacion);
            tvFechaPublicacion = itemView.findViewById(R.id.tv_fecha_publicacion);


            btnComentar = itemView.findViewById(R.id.btn_comentar);
            btnVerFotos = itemView.findViewById(R.id.btn_ver_fotos);
            btnVerUbicacion = itemView.findViewById(R.id.btn_ver_ubicacion);
            btnCalificar = itemView.findViewById(R.id.btn_calificar);

            btnVerComentarios = itemView.findViewById(R.id.btn_ver_comentarios);

            tvEsUrgente = itemView.findViewById(R.id.tv_es_urgente);
        }
    }
}