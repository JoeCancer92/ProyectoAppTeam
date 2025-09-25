package com.example.proyectoappteam.clases;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
                List<String> fotosList = Arrays.asList(fotosString.split(","));
                showPhotosDialog(v.getContext(), fotosList);
            });
        } else {
            holder.btnVerFotos.setVisibility(View.GONE);
        }

        cargarDatosUsuario(publicacion.getOwnerId(), holder.tvNombreUsuario, holder.tvCorreoUsuario, holder.ivImagenPerfil);

        // Otros listeners...
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                Log.i(TAG, "Puntuación para " + publicacion.getOwnerId() + ": " + rating);
            }
        });

        holder.btnComentar.setOnClickListener(v -> {
            Log.i(TAG, "Botón de comentar clickeado para: " + publicacion.getOwnerId());
        });
    }

    private void showPhotosDialog(Context context, List<String> fotosList) {
        // Crear un LinearLayout para contener las imágenes
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Añadir una ImageView para cada URL de foto
        for (String url : fotosList) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 500);
            imageView.setLayoutParams(params);
            new DownloadImageTask(imageView).execute(url.trim()); // trim() para eliminar espacios
            layout.addView(imageView);
        }

        // Construir y mostrar el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Fotos de la Publicación")
                .setView(layout)
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
        RatingBar ratingBar;
        Button btnComentar;
        Button btnVerFotos;
        TextView tvEsUrgente; // Nuevo TextView para el estado de urgencia

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagenPerfil = itemView.findViewById(R.id.iv_user_profile);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_publicacion);
            tvNombreUsuario = itemView.findViewById(R.id.tv_user_name);
            tvCorreoUsuario = itemView.findViewById(R.id.tv_user_email);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion_publicacion);
            ratingBar = itemView.findViewById(R.id.rating_bar_publicacion);
            btnComentar = itemView.findViewById(R.id.btn_comentar);
            btnVerFotos = itemView.findViewById(R.id.btn_ver_fotos);
            tvEsUrgente = itemView.findViewById(R.id.tv_es_urgente);
        }
    }
}