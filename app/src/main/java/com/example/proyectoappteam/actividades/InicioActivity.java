package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectoappteam.R;

public class InicioActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView logoApp = findViewById(R.id.logoApp);
        progressBar = findViewById(R.id.progressBar);

        // Configurar ProgressBar en modo determinate (barra de progreso que avanza de 0 a 100)
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#032E45")));

        // Animación del logo
        logoApp.setAlpha(0f);
        logoApp.animate()
                .alpha(1f)
                .setDuration(1000);

        // Simulación de carga del ProgressBar
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 2; // Incrementa de 2 en 2
                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(50); // Velocidad de carga (más alto = más lento)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Cuando termine la "carga", pasar a la siguiente actividad
            runOnUiThread(() -> {
                Intent intent = new Intent(InicioActivity.this, PrincipalActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}
