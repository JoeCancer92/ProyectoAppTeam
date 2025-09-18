package com.example.proyectoappteam.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Seguridad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecuperarPassActivity extends AppCompatActivity {

    EditText inputCorreoRecuperar, inputNuevaClave, inputConfirmarClave;
    Button btnVerificarCorreo, btnCambiarClave, btnVolverInicio;
    TextView txtMensajeDesarrollo;

    String objectIdUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_pass);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputCorreoRecuperar = findViewById(R.id.inputCorreoRecuperar);
        inputNuevaClave = findViewById(R.id.inputNuevaClave);
        inputConfirmarClave = findViewById(R.id.inputConfirmarClave);
        btnVerificarCorreo = findViewById(R.id.btnVerificarCorreo);
        btnCambiarClave = findViewById(R.id.btnCambiarClave);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);
        txtMensajeDesarrollo = findViewById(R.id.txtMensajeDesarrollo);

        inputNuevaClave.setVisibility(View.GONE);
        inputConfirmarClave.setVisibility(View.GONE);
        btnCambiarClave.setVisibility(View.GONE);
        txtMensajeDesarrollo.setVisibility(View.GONE);

        btnVerificarCorreo.setOnClickListener(v -> {
            String correoIngresado = inputCorreoRecuperar.getText().toString().trim().toLowerCase();

            if (correoIngresado.isEmpty()) {
                Toast.makeText(this, "Error: ingresa un correo válido", Toast.LENGTH_LONG).show();
                return;
            }

            String whereClause = "LOWER(correo) = '" + correoIngresado.replace("'", "\\'") + "'";
            DataQueryBuilder query = DataQueryBuilder.create().setWhereClause(whereClause);

            Backendless.Data.of("Usuario").find(query, new AsyncCallback<List<Map>>() {
                @Override
                public void handleResponse(List<Map> usuarios) {
                    if (!usuarios.isEmpty()) {
                        objectIdUsuario = (String) usuarios.get(0).get("objectId");
                        inputNuevaClave.setVisibility(View.VISIBLE);
                        inputConfirmarClave.setVisibility(View.VISIBLE);
                        btnCambiarClave.setVisibility(View.VISIBLE);
                        txtMensajeDesarrollo.setVisibility(View.GONE);

                        Toast.makeText(RecuperarPassActivity.this, "Correo verificado. Ahora puedes cambiar tu contraseña.", Toast.LENGTH_SHORT).show();
                    } else {
                        objectIdUsuario = null;
                        inputNuevaClave.setVisibility(View.GONE);
                        inputConfirmarClave.setVisibility(View.GONE);
                        btnCambiarClave.setVisibility(View.GONE);
                        txtMensajeDesarrollo.setVisibility(View.GONE);

                        // Aquí se inicia la segunda consulta para obtener la lista del equipo de desarrollo
                        Backendless.Data.of("EquipoDesarrollo").find(DataQueryBuilder.create(), new AsyncCallback<List<Map>>() {
                            @Override
                            public void handleResponse(List<Map> equipo) {
                                StringBuilder mensaje = new StringBuilder();
                                mensaje.append("El correo no se encuentra registrado. Por favor, comuníquese con el equipo de desarrollo de Vecired:\n\n");

                                if (!equipo.isEmpty()) {
                                    for (Map<String, Object> miembro : equipo) {
                                        String nombre = (String) miembro.get("nombre");
                                        String apellidos = (String) miembro.get("apellidos");
                                        String correoMiembro = (String) miembro.get("correo");

                                        mensaje.append(nombre).append(" ").append(apellidos).append(" - ").append(correoMiembro).append("\n");
                                    }
                                } else {
                                    mensaje.append("No se pudo obtener la lista de contactos del equipo de desarrollo.");
                                }

                                // Se crea y muestra el AlertDialog con el mensaje
                                new AlertDialog.Builder(RecuperarPassActivity.this)
                                        .setTitle("Información")
                                        .setMessage(mensaje.toString())
                                        .setPositiveButton("Aceptar", null)
                                        .show();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                new AlertDialog.Builder(RecuperarPassActivity.this)
                                        .setTitle("Error")
                                        .setMessage("El correo no se encuentra registrado. Error al cargar la lista del equipo de desarrollo: " + fault.getMessage())
                                        .setPositiveButton("Aceptar", null)
                                        .show();
                            }
                        });
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(RecuperarPassActivity.this, "Error al verificar correo: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnCambiarClave.setOnClickListener(v -> {
            String nuevaClave = inputNuevaClave.getText().toString().trim();
            String confirmarClave = inputConfirmarClave.getText().toString().trim();

            if (!nuevaClave.equals(confirmarClave)) {
                Toast.makeText(this, getString(R.string.claves_no_coinciden), Toast.LENGTH_SHORT).show();
                return;
            }

            if (objectIdUsuario == null || objectIdUsuario.isEmpty()) {
                Toast.makeText(this, "Error: usuario no válido para actualización", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> usuarioMap = new HashMap<>();
            usuarioMap.put("clave", Seguridad.hashClave(nuevaClave));

            String objectIdWhereClause = "objectId = '" + objectIdUsuario + "'";
            Backendless.Data.of("Usuario").update(objectIdWhereClause, usuarioMap, new AsyncCallback<Integer>() {
                @Override
                public void handleResponse(Integer response) {
                    Toast.makeText(RecuperarPassActivity.this, getString(R.string.contrasena_actualizada), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RecuperarPassActivity.this, PrincipalActivity.class));
                    finish();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(RecuperarPassActivity.this, "Error al actualizar la contraseña: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnVolverInicio.setOnClickListener(v -> {
            startActivity(new Intent(RecuperarPassActivity.this, PrincipalActivity.class));
            finish();
        });
    }
}