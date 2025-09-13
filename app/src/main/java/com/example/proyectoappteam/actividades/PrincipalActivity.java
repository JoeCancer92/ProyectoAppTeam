package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectoappteam.R;

public class PrincipalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        // Ajuste visual para barras del sistema
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Acción: ir al menú principal
        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, MenuPrincipalActivity.class);
            startActivity(intent);
            finish(); // evita volver atrás con el botón físico
        });

        // Acción: ir a pantalla de registro
        TextView textoRegistro = findViewById(R.id.textoRegistro);
        textoRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // Acción: ir a recuperación de contraseña
        TextView textoOlvidaste = findViewById(R.id.textoOlvidePassword);
        textoOlvidaste.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, RecuperarPassActivity.class);
            startActivity(intent);
        });
    }
}