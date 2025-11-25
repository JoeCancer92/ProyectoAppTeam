package com.example.proyectoappteam.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.ProyectoAppTeam;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.LocaleHelper;
import com.example.proyectoappteam.clases.Seguridad;

import java.util.List;
import java.util.Map;

public class PrincipalActivity extends AppCompatActivity {

    private static final String TAG = "PrincipalActivity";

    EditText inputUsuario, inputPassword;
    Button btnIniciarSesion;
    TextView textoRegistro, textoOlvidePassword;
    CheckBox checkboxRecordarCuenta;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, "es"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences prefs = getSharedPreferences("AppConfigPrefs", MODE_PRIVATE);
        int tema = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(tema);

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

        String correoGuardado = prefs.getString("correoRecordado", "");
        if (!correoGuardado.isEmpty()) {
            inputUsuario.setText(correoGuardado);
            checkboxRecordarCuenta.setChecked(true);
        }

        btnIniciarSesion.setOnClickListener(v -> {
            String correo = inputUsuario.getText().toString().trim();
            String clave = inputPassword.getText().toString().trim();

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Backendless.UserService.login(correo, clave, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser user) {
                    Toast.makeText(PrincipalActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();

                    if (checkboxRecordarCuenta.isChecked()) {
                        prefs.edit().putString("correoRecordado", correo).apply();
                    } else {
                        prefs.edit().remove("correoRecordado").apply();
                    }

                    // NUEVO: Iniciar el listener de notificaciones en tiempo real
                    ((ProyectoAppTeam) getApplication()).iniciarListenerDeNotificaciones();

                    sincronizarContrasena(user, clave);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(PrincipalActivity.this, "Error de autenticación: " + fault.getMessage(), Toast.LENGTH_LONG).show();
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

    private void sincronizarContrasena(BackendlessUser loggedInUser, String password) {
        String whereClause = "ownerId = '" + loggedInUser.getObjectId() + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("Usuario").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> usuarioList) {
                if (!usuarioList.isEmpty()) {
                    Map<String, Object> usuarioData = usuarioList.get(0);
                    String claveUsuario = (String) usuarioData.get("clave");
                    String passwordHash = Seguridad.hashClave(password);

                    if (!passwordHash.equals(claveUsuario)) {
                        usuarioData.put("clave", passwordHash);

                        Backendless.Data.of("Usuario").save(usuarioData, new AsyncCallback<Map>() {
                            @Override
                            public void handleResponse(Map updatedUsuario) {
                                redirigirAMenuPrincipal();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                redirigirAMenuPrincipal();
                            }
                        });
                    } else {
                        redirigirAMenuPrincipal();
                    }
                } else {
                    redirigirAMenuPrincipal();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                redirigirAMenuPrincipal();
            }
        });
    }

    private void redirigirAMenuPrincipal() {
        Intent intent = new Intent(PrincipalActivity.this, MenuPrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
