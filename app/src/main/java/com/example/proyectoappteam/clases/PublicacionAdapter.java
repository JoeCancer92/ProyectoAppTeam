package com.example.proyectoappteam.clases;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent; // <--- NUEVA IMPORTACIÓN NECESARIA
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast; // Importar Toast para mensajes al usuario
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder; // Importar DataQueryBuilder para buscar calificaciones
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.actividades.PhotoSliderActivity;

// *******************************************************************
// IMPORTACIONES NECESARIAS PARA MANEJAR Y FORMATEAR LA FECHA
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
// *******************************************************************
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private static final String TAG = "PublicacionAdapter";
    private List<Publicaciones> publicaciones;

    public PublicacionAdapter(List<Publicaciones> publicaciones) {
        this.publicaciones = publicaciones;
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

        holder.tvDescripcion.setText(publicacion.getDescripcion());
        holder.tvUbicacion.setText(publicacion.getUbicacion());

        // *******************************************************************
        // LÓGICA DE LA FECHA
        // *******************************************************************
        Date fechaCreacion = publicacion.getCreated();
        Log.d("FECHA_DEBUG", "Fecha de publicación: " + (fechaCreacion != null ? fechaCreacion.toString() : "Fecha NULA"));

        if (fechaCreacion != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFechaPublicacion.setText(sdf.format(fechaCreacion));
        } else {
            holder.tvFechaPublicacion.setText("Fecha no disponible");
        }
        // *******************************************************************

        // Manejar el estado de urgencia
        if (publicacion.getEsUrgente() != null && publicacion.getEsUrgente()) {
            holder.tvEsUrgente.setVisibility(View.VISIBLE);
        } else {
            holder.tvEsUrgente.setVisibility(View.GONE);
        }

        // *******************************************************************
        // Lógica para el botón "Ver Fotos" (Se mantiene)
        // *******************************************************************
        if (publicacion.getFotos() != null && !publicacion.getFotos().isEmpty()) {
            holder.btnVerFotos.setVisibility(View.VISIBLE);
            holder.btnVerFotos.setOnClickListener(v -> {
                String fotosString = publicacion.getFotos();
                Intent intent = new Intent(v.getContext(), PhotoSliderActivity.class);
                intent.putExtra(PhotoSliderActivity.EXTRA_PHOTO_URLS, fotosString);
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnVerFotos.setVisibility(View.GONE);
        }
        // *******************************************************************

        // *******************************************************************
        // LÓGICA PARA EL BOTÓN "VER UBICACIÓN" (NUEVO CÓDIGO)
        // *******************************************************************
        // El botón debe ser visible solo si hay coordenadas válidas
        if (publicacion.getLatitud() != null && publicacion.getLongitud() != null &&
                publicacion.getLatitud() != 0.0 && publicacion.getLongitud() != 0.0) {

            holder.btnVerUbicacion.setVisibility(View.VISIBLE);
            holder.btnVerUbicacion.setOnClickListener(v -> {
                // Crea un Intent para lanzar MapsActivity
                Intent mapIntent = new Intent(v.getContext(), com.example.proyectoappteam.actividades.MapsActivity.class);

                // Pasar las coordenadas y la ubicación/descripción al Intent
                mapIntent.putExtra("latitud", publicacion.getLatitud());
                mapIntent.putExtra("longitud", publicacion.getLongitud());

                // Pasar la ubicación o descripción como título del marcador
                String markerTitle = publicacion.getUbicacion() != null && !publicacion.getUbicacion().isEmpty() ?
                        publicacion.getUbicacion() :
                        publicacion.getDescripcion();
                mapIntent.putExtra("markerTitle", markerTitle);

                // Lanza la nueva Activity
                v.getContext().startActivity(mapIntent);
            });
        } else {
            // Oculta el botón si la publicación no tiene ubicación
            holder.btnVerUbicacion.setVisibility(View.GONE);
        }
        // *******************************************************************

        cargarDatosUsuario(publicacion.getOwnerId(), holder.tvNombreUsuario, holder.tvCorreoUsuario, holder.ivImagenPerfil);


        // *******************************************************************
        // LÓGICA DE CALIFICACIÓN: Cargar la calificación y configurar el Listener
        // *******************************************************************
        final String currentUserId = Backendless.UserService.CurrentUser() != null ? Backendless.UserService.CurrentUser().getObjectId() : null;

        if (currentUserId != null) {
            // 1. Cargar la calificación existente (o 0) para esta publicación
            cargarCalificacionExistente(publicacion.getObjectId(), currentUserId, holder.ratingBar, holder.itemView.getContext());

            // 2. Configurar el Listener para guardar/actualizar la calificación
            holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    guardarCalificacion(publicacion, currentUserId, rating, holder.itemView.getContext());
                    Log.i(TAG, "Nueva calificación enviada para " + publicacion.getObjectId() + ": " + rating);
                }
            });
            holder.ratingBar.setIsIndicator(false); // Permite al usuario interactuar
        } else {
            // Usuario no logueado: RatingBar solo lectura y rating 0
            holder.ratingBar.setRating(0);
            holder.ratingBar.setIsIndicator(true);
            Log.w(TAG, "RatingBar deshabilitado: No hay usuario logueado.");
        }
        // *******************************************************************

        holder.btnComentar.setOnClickListener(v -> {
            Log.i(TAG, "Botón de comentar clickeado para: " + publicacion.getOwnerId());
            // Lógica para abrir la pantalla de comentarios aquí
        });
    }

    // *******************************************************************
    // MÉTODOS DE CALIFICACIÓN (se mantienen igual)
    // *******************************************************************

    private void cargarCalificacionExistente(String publicacionId, String userId, RatingBar ratingBar, Context context) {
        // ... (código existente) ...
        String whereClause = "ownerId = '" + userId + "' AND publicacion.objectId = '" + publicacionId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Calificaciones.class).find(queryBuilder, new AsyncCallback<List<Calificaciones>>() {
            @Override
            public void handleResponse(List<Calificaciones> foundCalificaciones) {
                if (foundCalificaciones != null && !foundCalificaciones.isEmpty()) {
                    Calificaciones calificacion = foundCalificaciones.get(0);
                    ratingBar.setRating(calificacion.getPuntuacion());
                } else {
                    ratingBar.setRating(0);
                }
                ratingBar.setIsIndicator(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar calificación existente: " + fault.getMessage());
                Toast.makeText(context, "Error al cargar tu calificación: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                ratingBar.setIsIndicator(true);
            }
        });
    }

    private void guardarCalificacion(Publicaciones publicacion, String userId, float rating, Context context) {

        String whereClause = "ownerId = '" + userId + "' AND publicacion.objectId = '" + publicacion.getObjectId() + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Calificaciones.class).find(queryBuilder, new AsyncCallback<List<Calificaciones>>() {
            @Override
            public void handleResponse(List<Calificaciones> existingCalificaciones) {
                Calificaciones calificacionToSave;
                if (existingCalificaciones != null && !existingCalificaciones.isEmpty()) {
                    calificacionToSave = existingCalificaciones.get(0);
                } else {
                    calificacionToSave = new Calificaciones();
                    calificacionToSave.setPublicacion(publicacion);
                    // *******************************************************************
                    // LÍNEA DE LÓGICA FALTANTE:
                    // Asignar el ownerId explícitamente al crear una nueva calificación
                    calificacionToSave.setOwnerId(userId); // <--- ¡CORREGIDO!
                    // *******************************************************************
                }
                calificacionToSave.setPuntuacion((int) rating);
                Backendless.Data.of(Calificaciones.class).save(calificacionToSave, new AsyncCallback<Calificaciones>() {
                    @Override
                    public void handleResponse(Calificaciones savedCalificacion) {
                        Log.i(TAG, "Calificación guardada/actualizada exitosamente.");
                        Toast.makeText(context, "Calificación guardada: " + (int)rating + " estrellas.", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(TAG, "Error al guardar calificación: " + fault.getMessage());
                        Toast.makeText(context, "Error al guardar calificación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error de búsqueda antes de guardar: " + fault.getMessage());
                Toast.makeText(context, "Error de conexión al calificar: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return publicaciones.size();
    }


    private void cargarDatosUsuario(String userId, TextView tvNombreUsuario, TextView tvCorreoUsuario, ImageView ivImagenPerfil) {
        // ... (código existente) ...
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
        RatingBar ratingBar;

        public ImageButton btnComentar;
        public ImageButton btnVerFotos;
        public ImageButton btnVerUbicacion;

        TextView tvEsUrgente;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagenPerfil = itemView.findViewById(R.id.iv_user_profile);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_publicacion);
            tvNombreUsuario = itemView.findViewById(R.id.tv_user_name);
            tvCorreoUsuario = itemView.findViewById(R.id.tv_user_email);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion_publicacion);
            tvFechaPublicacion = itemView.findViewById(R.id.tv_fecha_publicacion);
            ratingBar = itemView.findViewById(R.id.rating_bar_publicacion);

            btnComentar = itemView.findViewById(R.id.btn_comentar);
            btnVerFotos = itemView.findViewById(R.id.btn_ver_fotos);
            btnVerUbicacion = itemView.findViewById(R.id.btn_ver_ubicacion);

            tvEsUrgente = itemView.findViewById(R.id.tv_es_urgente);
        }
    }
}