package com.example.proyectoappteam.actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.widget.ImageButton; // Importar la clase ImageButton
import android.widget.TextView;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.PhotoPagerAdapter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PhotoSliderActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_URLS = "extra_photo_urls";

    private ViewPager2 photoViewPager;
    private TextView positionIndicator;
    private ImageButton closeButton; // Declaración de la variable para el botón

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar la barra de acción para que la vista de fotos sea a pantalla completa
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Enlazar con el layout XML que creaste: activity_photo_slider.xml
        setContentView(R.layout.activity_photo_slider);

        photoViewPager = findViewById(R.id.photo_viewpager);
        positionIndicator = findViewById(R.id.tv_position_indicator);
        closeButton = findViewById(R.id.btn_close_slider); // Enlazar el ImageButton con su ID en el XML

        // 1. Obtener la lista de URLs (viene como un String separado por comas)
        String photoUrlsString = getIntent().getStringExtra(EXTRA_PHOTO_URLS);

        if (photoUrlsString == null || photoUrlsString.isEmpty()) {
            finish(); // Cierra la actividad si no hay URLs válidas
            return;
        }

        // Convertir el String separado por comas a una Lista de URLs
        List<String> photoUrls = Arrays.asList(photoUrlsString.split(","));

        // 2. Configurar el adaptador PhotoPagerAdapter
        PhotoPagerAdapter adapter = new PhotoPagerAdapter(photoUrls);
        photoViewPager.setAdapter(adapter);

        // 3. Configurar el indicador de posición para que se actualice al deslizar
        photoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int total = photoUrls.size();
                // Actualizar el texto del indicador (ej: 1 / 5)
                positionIndicator.setText(String.format(Locale.getDefault(), "%d / %d", position + 1, total));
            }
        });

        // Inicializar el indicador al cargar la actividad (muestra "1 / N")
        if (!photoUrls.isEmpty()) {
            positionIndicator.setText(String.format(Locale.getDefault(), "%d / %d", 1, photoUrls.size()));
        }

        // 4. Agregar la funcionalidad al botón de cerrar
        closeButton.setOnClickListener(v -> {
            finish(); // Cierra la actividad y regresa a la pantalla anterior
        });
    }
}