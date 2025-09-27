package com.example.proyectoappteam.clases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.proyectoappteam.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private final List<Comentarios> comentariosList;
    private final Context context;
    private static final String TAG = "ComentarioAdapter";

    public ComentarioAdapter(Context context, List<Comentarios> comentariosList) {
        this.context = context;
        this.comentariosList = comentariosList;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentarios comentario = comentariosList.get(position);

        // 1. Mostrar el texto del comentario
        holder.tvTextoComentario.setText(comentario.getTexto());

        // 2. Formatear y mostrar la fecha
        Date fechaCreacion = comentario.getFechaCreacion();
        if (fechaCreacion != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(fechaCreacion));
        } else {
            holder.tvFecha.setText("Fecha no disponible");
        }

        // 3. Cargar datos del usuario (Nombre, Correo y Foto)
        // Usamos el ownerId del comentario para buscar los datos del usuario.
        cargarDatosUsuario(comentario.getOwnerId(), holder.tvNombreUsuario, holder.tvCorreoUsuario, holder.ivImagenPerfil);
    }

    @Override
    public int getItemCount() {
        return comentariosList.size();
    }

    /**
     * Busca los datos completos del usuario (nombre, correo, foto) usando el ownerId.
     */
    private void cargarDatosUsuario(String userId, TextView tvNombre, TextView tvCorreo, ImageView ivFoto) {
        if (userId == null) {
            tvNombre.setText("Usuario Desconocido");
            tvCorreo.setText("");
            ivFoto.setImageResource(R.drawable.ic_default_profile);
            return;
        }

        // BackendlessUser es la clase del sistema para la tabla Usuario
        Backendless.Data.of(BackendlessUser.class).findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                if (user != null) {
                    String nombre = (String) user.getProperty("nombre");
                    String apellidos = (String) user.getProperty("apellidos");
                    String email = user.getEmail();

                    // Mostrar Nombre y Apellido
                    tvNombre.setText(String.format("%s %s", nombre != null ? nombre : "", apellidos != null ? apellidos : ""));
                    tvCorreo.setText(email != null ? email : "");

                    // Cargar imagen de perfil (reutilizando la lógica de descarga de imágenes)
                    String fotoUrl = (String) user.getProperty("urlfoto");
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        new DownloadImageTask(ivFoto).execute(fotoUrl);
                    } else {
                        ivFoto.setImageResource(R.drawable.ic_default_profile);
                    }
                } else {
                    tvNombre.setText("Usuario Desconocido");
                    tvCorreo.setText("");
                    ivFoto.setImageResource(R.drawable.ic_default_profile);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al cargar datos del usuario del comentario: " + fault.getMessage());
                tvNombre.setText("Error al cargar usuario");
                tvCorreo.setText("");
                ivFoto.setImageResource(R.drawable.ic_default_profile);
            }
        });
    }

    // Tarea asíncrona para descargar la imagen de perfil (Puedes reutilizar la clase interna de PublicacionAdapter)
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


    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagenPerfil;
        TextView tvNombreUsuario;
        TextView tvCorreoUsuario;
        TextView tvTextoComentario;
        TextView tvFecha;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagenPerfil = itemView.findViewById(R.id.iv_comment_user_profile);
            tvNombreUsuario = itemView.findViewById(R.id.tv_comment_user_name);
            tvCorreoUsuario = itemView.findViewById(R.id.tv_comment_user_email);
            tvTextoComentario = itemView.findViewById(R.id.tv_comment_text);
            tvFecha = itemView.findViewById(R.id.tv_comment_date);
        }
    }
}