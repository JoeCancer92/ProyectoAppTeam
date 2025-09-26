package com.example.proyectoappteam.clases;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Importación de la ImageView nativa
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectoappteam.R;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {

    private final List<String> photoUrls;

    public PhotoPagerAdapter(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_slide, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String url = photoUrls.get(position);
        // Usamos la clase asíncrona para descargar y mostrar la imagen
        new DownloadImageTask(holder.photoView, holder.progressBar).execute(url);
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoView; // Ahora es ImageView
        ProgressBar progressBar;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.iv_slide_photo);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    // Clase AsyncTask adaptada para cargar imágenes en ImageView
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView photoView; // Ahora es ImageView
        ProgressBar progressBar;

        public DownloadImageTask(ImageView photoView, ProgressBar progressBar) {
            this.photoView = photoView;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0].trim();
            Bitmap mIcon11 = null;
            try {
                URL url = new URL(urldisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e("PhotoAdapter", "Error al descargar imagen: " + e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                photoView.setImageBitmap(result);
            } else {
                // Asegúrate de que este drawable exista para el caso de error
                photoView.setImageResource(R.drawable.ic_default_profile);
            }
        }
    }
}