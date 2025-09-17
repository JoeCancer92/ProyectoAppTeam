package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Usuario;
import com.example.proyectoappteam.clases.Seguridad;

import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    EditText inputUsuario, inputPassword;
    Button btnIniciarSesion;
    TextView textoRegistro, textoOlvidePassword;
    CheckBox checkboxRecordarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputUsuario = findViewById(R.id.inputUsuario);
        inputPassword = findViewById(R.id.inputPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        textoRegistro = findViewById(R.id.textoRegistro);
        textoOlvidePassword = findViewById(R.id.textoOlvidePassword);
        checkboxRecordarCuenta = findViewById(R.id.checkboxRecordarCuenta);

        SharedPreferences prefs = getSharedPreferences("VeciRedPrefs", MODE_PRIVATE);
        String correoGuardado = prefs.getString("correoRecordado", "");
        if (!correoGuardado.isEmpty()) {
            inputUsuario.setText(correoGuardado);
            checkboxRecordarCuenta.setChecked(true);
        }

        btnIniciarSesion.setOnClickListener(v -> {
            String correo = inputUsuario.getText().toString().trim();
            String claveOriginal = inputPassword.getText().toString().trim();

            if (correo.isEmpty() || claveOriginal.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //Hashear la contraseña ingresada
            String claveHasheada = Seguridad.hashClave(claveOriginal);

            String whereClause = "correo = '" + correo + "' AND clave = '" + claveHasheada + "'";
            DataQueryBuilder query = DataQueryBuilder.create().setWhereClause(whereClause);

            Backendless.Data.of(Usuario.class).find(query, new AsyncCallback<List<Usuario>>() {
                @Override
                public void handleResponse(List<Usuario> usuarios) {
                    if (!usuarios.isEmpty()) {
                        Usuario usuario = usuarios.get(0);
                        Toast.makeText(PrincipalActivity.this, "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

                        if (checkboxRecordarCuenta.isChecked()) {
                            prefs.edit().putString("correoRecordado", usuario.getCorreo()).apply();
                        } else {
                            prefs.edit().remove("correoRecordado").apply();
                        }

                        startActivity(new Intent(PrincipalActivity.this, MenuPrincipalActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PrincipalActivity.this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(PrincipalActivity.this, "Error de conexión: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        textoRegistro.setOnClickListener(v -> {
            startActivity(new Intent(PrincipalActivity.this, RegistroActivity.class));
        });

        textoOlvidePassword.setOnClickListener(v -> {
            startActivity(new Intent(PrincipalActivity.this, RecuperarPassActivity.class));
        });
    }
}