package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectoappteam.R;

public class InicioActivity extends AppCompatActivity {

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
        ProgressBar progressBar = findViewById(R.id.progressBar);

        logoApp.setAlpha(0f);
        logoApp.animate()
                .alpha(1f)
                .setDuration(1500)
                .withEndAction(() -> {
                    Intent intent = new Intent(InicioActivity.this, PrincipalActivity.class);
                    startActivity(intent);
                    finish();
                });

        progressBar.setIndeterminate(true);
    }
}