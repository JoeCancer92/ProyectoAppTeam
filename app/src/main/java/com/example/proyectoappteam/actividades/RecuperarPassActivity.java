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
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.EmailEnvelope;
import com.backendless.messaging.MessageStatus;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Seguridad;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RecuperarPassActivity extends AppCompatActivity {

    EditText inputCorreoRecuperar, inputCodigoRecuperacion, inputNuevaClave, inputConfirmarClave;
    Button btnVerificarCorreo, btnCambiarClave, btnVolverInicio, btnComprobarClave;
    TextView txtMensajeDesarrollo;
    TextInputLayout layoutCodigoRecuperacion, layoutNuevaClave, layoutConfirmarClave;

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

        // Inicialización de vistas
        inputCorreoRecuperar = findViewById(R.id.inputCorreoRecuperar);
        btnVerificarCorreo = findViewById(R.id.btnVerificarCorreo);
        btnCambiarClave = findViewById(R.id.btnCambiarClave);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);
        btnComprobarClave = findViewById(R.id.btnComprobarClave);
        txtMensajeDesarrollo = findViewById(R.id.txtMensajeDesarrollo);

        layoutCodigoRecuperacion = findViewById(R.id.layoutCodigoRecuperacion);
        inputCodigoRecuperacion = layoutCodigoRecuperacion.findViewById(R.id.inputCodigoRecuperacion);

        layoutNuevaClave = findViewById(R.id.layoutNuevaClave);
        inputNuevaClave = layoutNuevaClave.findViewById(R.id.inputNuevaClave);

        layoutConfirmarClave = findViewById(R.id.layoutConfirmarClave);
        inputConfirmarClave = layoutConfirmarClave.findViewById(R.id.inputConfirmarClave);

        // Ocultar campos de contraseña y botones al inicio
        layoutCodigoRecuperacion.setVisibility(View.GONE);
        btnComprobarClave.setVisibility(View.GONE);
        layoutNuevaClave.setVisibility(View.GONE);
        layoutConfirmarClave.setVisibility(View.GONE);
        btnCambiarClave.setVisibility(View.GONE);
        txtMensajeDesarrollo.setVisibility(View.GONE);

        btnVerificarCorreo.setOnClickListener(v -> {
            String correoIngresado = inputCorreoRecuperar.getText().toString().trim().toLowerCase();
            if (correoIngresado.isEmpty()) {
                Toast.makeText(this, "Error: ingresa un correo válido", Toast.LENGTH_LONG).show();
                return;
            }
            verificarCorreoEnTablas(correoIngresado);
        });

        btnComprobarClave.setOnClickListener(v -> {
            String correoIngresado = inputCorreoRecuperar.getText().toString().trim().toLowerCase();
            String codigoIngresado = inputCodigoRecuperacion.getText().toString().trim();
            if (codigoIngresado.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el código temporal.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Iniciar sesión con la clave temporal para autenticar.
            Backendless.UserService.login(correoIngresado, codigoIngresado, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser loggedInUser) {
                    Toast.makeText(RecuperarPassActivity.this, "Código verificado. Ahora puedes cambiar tu contraseña.", Toast.LENGTH_LONG).show();
                    inputCodigoRecuperacion.setEnabled(false);
                    btnComprobarClave.setEnabled(false);
                    layoutNuevaClave.setVisibility(View.VISIBLE);
                    layoutConfirmarClave.setVisibility(View.VISIBLE);
                    btnCambiarClave.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(RecuperarPassActivity.this, "Código temporal incorrecto. Inténtalo de nuevo.", Toast.LENGTH_LONG).show();
                }
            });
        });

        btnCambiarClave.setOnClickListener(v -> {
            String correoIngresado = inputCorreoRecuperar.getText().toString().trim().toLowerCase();
            String nuevaClave = inputNuevaClave.getText().toString().trim();
            String confirmarClave = inputConfirmarClave.getText().toString().trim();
            if (nuevaClave.isEmpty() || confirmarClave.isEmpty()) {
                Toast.makeText(this, "Por favor, completa los campos de nueva contraseña.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!nuevaClave.equals(confirmarClave)) {
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }
            cambiarContrasenaEnAmbasTablas(correoIngresado, nuevaClave);
        });

        btnVolverInicio.setOnClickListener(v -> {
            startActivity(new Intent(RecuperarPassActivity.this, PrincipalActivity.class));
            finish();
        });
    }

    private void verificarCorreoEnTablas(String correo) {
        String whereClause = "email = '" + correo + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause);
        Backendless.Data.of(BackendlessUser.class).find(queryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> users) {
                if (users != null && !users.isEmpty()) {
                    verificarCorreoEnTablaUsuario(correo);
                } else {
                    mostrarMensajeCorreoNoExiste();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RecuperarPassActivity.this, "Error al verificar el correo en 'Users': " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verificarCorreoEnTablaUsuario(String correo) {
        String whereClause = "correo = '" + correo + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause);
        Backendless.Data.of("Usuario").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> usuarios) {
                if (usuarios != null && !usuarios.isEmpty()) {
                    restaurarContrasenaBackendless(correo);
                } else {
                    mostrarMensajeCorreoNoExiste();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RecuperarPassActivity.this, "Error al verificar el correo en 'Usuario': " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restaurarContrasenaBackendless(String correo) {
        Backendless.UserService.restorePassword(correo, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Toast.makeText(RecuperarPassActivity.this, "Se ha enviado una contraseña temporal a su correo.", Toast.LENGTH_LONG).show();
                inputCorreoRecuperar.setEnabled(false);
                btnVerificarCorreo.setVisibility(View.GONE);
                layoutCodigoRecuperacion.setVisibility(View.VISIBLE);
                btnComprobarClave.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RecuperarPassActivity.this, "Error al enviar la contraseña: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cambiarContrasenaEnAmbasTablas(String correo, String nuevaClave) {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error de sesión. Por favor, reinicie el proceso.", Toast.LENGTH_LONG).show();
            return;
        }

        currentUser.setPassword(nuevaClave);
        Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser updatedUser) {
                Backendless.UserService.login(correo, nuevaClave, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser finalUser) {
                        String whereClause = "ownerId = '" + finalUser.getObjectId() + "'";
                        DataQueryBuilder queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause);

                        Backendless.Data.of("Usuario").find(queryBuilder, new AsyncCallback<List<Map>>() {
                            @Override
                            public void handleResponse(List<Map> usuarioList) {
                                if (usuarioList != null && !usuarioList.isEmpty()) {
                                    Map usuarioMap = usuarioList.get(0);
                                    String claveHasheada = Seguridad.hashClave(nuevaClave);
                                    usuarioMap.put("clave", claveHasheada);

                                    Backendless.Data.of("Usuario").save(usuarioMap, new AsyncCallback<Map>() {
                                        @Override
                                        public void handleResponse(Map updatedMap) {
                                            Toast.makeText(RecuperarPassActivity.this, "Contraseña actualizada con éxito.", Toast.LENGTH_LONG).show();

                                            // Enviar correo de confirmación con plantilla
                                            enviarCorreoConfirmacion(finalUser);

                                            Backendless.UserService.logout(new AsyncCallback<Void>() {
                                                @Override
                                                public void handleResponse(Void aVoid) {
                                                    startActivity(new Intent(RecuperarPassActivity.this, PrincipalActivity.class));
                                                    finish();
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault backendlessFault) {
                                                    startActivity(new Intent(RecuperarPassActivity.this, PrincipalActivity.class));
                                                    finish();
                                                }
                                            });
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            Toast.makeText(RecuperarPassActivity.this, "Error al actualizar la contraseña en la tabla 'Usuario': " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(RecuperarPassActivity.this, "Error: No se encontró el registro de usuario en la tabla 'Usuario'.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(RecuperarPassActivity.this, "Error al buscar el usuario en la tabla 'Usuario': " + fault.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(RecuperarPassActivity.this, "Error al iniciar sesión con la nueva contraseña: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RecuperarPassActivity.this, "Error al actualizar la contraseña en la tabla 'Users': " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**  Metodo para enviar el correo con la plantilla personalizada */
    private void enviarCorreoConfirmacion(BackendlessUser usuario) {
        EmailEnvelope envelope = new EmailEnvelope();

        // Destinatario
        HashSet<String> to = new HashSet<>();
        to.add(usuario.getEmail());
        envelope.setTo(to);

        // Variables que la plantilla usará
        HashMap<String, String> templateValues = new HashMap<>();
        templateValues.put("Users.nombre", (String) usuario.getProperty("nombre"));
        templateValues.put("Users.apellidos", (String) usuario.getProperty("apellidos"));

        // Enviar usando plantilla de Backendless
        Backendless.Messaging.sendEmailFromTemplate(
                "PasswordChangedSuccess",   // Nombre de la plantilla
                envelope,
                templateValues,
                new AsyncCallback<MessageStatus>() {
                    @Override
                    public void handleResponse(MessageStatus response) {
                        android.util.Log.d("RecuperarPass", "Correo de confirmación enviado correctamente.");
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        android.util.Log.e("RecuperarPass", "Error al enviar correo: " + fault.getMessage());
                    }
                });
    }

    private void mostrarMensajeCorreoNoExiste() {
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
