package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectoappteam.R;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Ajuste visual para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Acción: volver al inicio
        Button btnVolverInicio = findViewById(R.id.btnVolverInicio);
        btnVolverInicio.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroActivity.this, PrincipalActivity.class);
            startActivity(intent);
            finish(); // evita volver atrás con el botón físico
        });
    }
}