package com.example.proyectoappteam.actividades;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.messaging.EmailEnvelope;
import com.backendless.messaging.MessageStatus;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Seguridad;
import com.example.proyectoappteam.clases.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    EditText inputFechaNac, inputNombre, inputApellidos, inputCorreo, inputClave, inputClave2, inputDni;
    Button btnSeleccionarFecha, btnVolverInicio, btnVerTerminos, btnEnviarRegistro, btnTomarFoto;
    CheckBox chkTerminos;
    ImageView imgFotoPerfil;
    Calendar calendario;
    static final int REQUEST_FOTO = 1001;
    static final int PERMISO_CAMARA = 200;

    private boolean fotoTomadaCorrectamente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputFechaNac = findViewById(R.id.inputFechaNac);
        inputNombre = findViewById(R.id.inputNombre);
        inputApellidos = findViewById(R.id.inputApellidos);
        inputCorreo = findViewById(R.id.inputCorreo);
        inputClave = findViewById(R.id.inputClave);
        inputClave2 = findViewById(R.id.inputClave2);
        inputDni = findViewById(R.id.inputDni);

        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);
        btnVerTerminos = findViewById(R.id.btnVerTerminos);
        btnEnviarRegistro = findViewById(R.id.btnEnviarRegistro);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        chkTerminos = findViewById(R.id.chkTerminos);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        calendario = Calendar.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        }

        btnEnviarRegistro.setEnabled(false);

        chkTerminos.setOnCheckedChangeListener((buttonView, isChecked) -> validarCampos());
        inputNombre.setOnFocusChangeListener((v, hasFocus) -> validarCampos());
        inputApellidos.setOnFocusChangeListener((v, hasFocus) -> validarCampos());
        inputCorreo.setOnFocusChangeListener((v, hasFocus) -> validarCampos());
        inputClave.setOnFocusChangeListener((v, hasFocus) -> validarCampos());
        inputClave2.setOnFocusChangeListener((v, hasFocus) -> validarCampos());
        inputDni.setOnFocusChangeListener((v, hasFocus) -> validarCampos());

        inputFechaNac.setKeyListener(null);
        inputFechaNac.setFocusable(false);
        inputFechaNac.setClickable(false);
        inputFechaNac.setCursorVisible(false);
        inputFechaNac.setLongClickable(false);

        btnSeleccionarFecha.setOnClickListener(v -> {
            int año = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int día = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog selectorFecha = new DatePickerDialog(
                    RegistroActivity.this,
                    (view, añoSel, mesSel, diaSel) -> {
                        String fecha = String.format("%02d/%02d/%04d", diaSel, mesSel + 1, añoSel);
                        inputFechaNac.setText(fecha);
                        validarCampos();
                    },
                    año, mes, día
            );
            selectorFecha.show();
        });

        btnVerTerminos.setOnClickListener(v -> {
            mostrarTerminosYCondiciones();
        });

        btnTomarFoto.setOnClickListener(v -> {
            Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(tomarFoto, REQUEST_FOTO);
        });

        btnVolverInicio.setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, PrincipalActivity.class));
            finish();
        });

        btnEnviarRegistro.setOnClickListener(v -> {
            String dni = inputDni.getText().toString().trim();
            verificarDniExistente(dni);
        });
    }

    private void verificarDniExistente(String dni) {
        String whereClause = "dni = '" + dni + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(BackendlessUser.class).find(queryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> usuarios) {
                if (usuarios != null && !usuarios.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "El DNI ya está registrado.", Toast.LENGTH_LONG).show();
                    return;
                }

                Backendless.Data.of("Usuario").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> usuariosUsuario) {
                        if (usuariosUsuario != null && !usuariosUsuario.isEmpty()) {
                            Toast.makeText(RegistroActivity.this, "El DNI ya está registrado.", Toast.LENGTH_LONG).show();
                        } else {
                            procesarRegistro();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(RegistroActivity.this, "Error al verificar el DNI en tabla 'Usuario': " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RegistroActivity.this, "Error al verificar el DNI en tabla 'Users': " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void procesarRegistro() {
        Bitmap fotoBitmap = ((BitmapDrawable) imgFotoPerfil.getDrawable()).getBitmap();
        try {
            File archivo = convertirBitmapAFile(fotoBitmap);

            Backendless.Files.upload(archivo, "/fotos_perfil", true, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile backendlessFile) {
                    String urlFoto = backendlessFile.getFileURL();
                    String dni = inputDni.getText().toString().trim();
                    String claveOriginal = inputClave.getText().toString().trim();

                    String claveHasheada = Seguridad.hashClave(claveOriginal);

                    // 1. Crear el objeto BackendlessUser para la tabla `Users`
                    BackendlessUser nuevoBackendlessUser = new BackendlessUser();
                    nuevoBackendlessUser.setEmail(inputCorreo.getText().toString().trim());
                    nuevoBackendlessUser.setPassword(claveOriginal);

                    nuevoBackendlessUser.setProperty("dni", dni);
                    nuevoBackendlessUser.setProperty("nombre", inputNombre.getText().toString().trim());
                    nuevoBackendlessUser.setProperty("apellidos", inputApellidos.getText().toString().trim());
                    nuevoBackendlessUser.setProperty("fechanacimiento", inputFechaNac.getText().toString().trim());
                    nuevoBackendlessUser.setProperty("urlfoto", urlFoto);

                    Backendless.UserService.register(nuevoBackendlessUser, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser registeredUser) {
                            // 2. Si el registro en `Users` es exitoso, ahora guarda en tu tabla personalizada.
                            Usuario nuevoUsuario = new Usuario();
                            nuevoUsuario.setNombre(inputNombre.getText().toString().trim());
                            nuevoUsuario.setApellidos(inputApellidos.getText().toString().trim());
                            nuevoUsuario.setCorreo(inputCorreo.getText().toString().trim());
                            nuevoUsuario.setFechaNacimiento(inputFechaNac.getText().toString().trim());
                            nuevoUsuario.setUrlFoto(urlFoto);
                            nuevoUsuario.setDni(dni);
                            nuevoUsuario.setClave(claveHasheada);

                            nuevoUsuario.setOwnerId(registeredUser.getObjectId());

                            // 3. Guardar los datos en la tabla `Usuario`.
                            Backendless.Data.of(Usuario.class).save(nuevoUsuario, new AsyncCallback<Usuario>() {
                                @Override
                                public void handleResponse(Usuario response) {
                                    // 4. Si el guardado en ambas tablas fue exitoso, envía el correo de bienvenida.
                                    enviarCorreoBienvenida(inputCorreo.getText().toString().trim(), inputNombre.getText().toString().trim());

                                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistroActivity.this, PrincipalActivity.class));
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.e("RegistroActivity", "Error al guardar información adicional: " + fault.getMessage());
                                    Toast.makeText(RegistroActivity.this, "Error al guardar información adicional: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                    // Si falla la segunda parte del registro, elimina el usuario de la tabla `Users`.
                                    Backendless.Data.of(BackendlessUser.class).remove(registeredUser, new AsyncCallback<Long>() {
                                        @Override
                                        public void handleResponse(Long response) {}
                                        @Override
                                        public void handleFault(BackendlessFault fault) {}
                                    });
                                }
                            });
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e("RegistroActivity", "Error al registrar en Backendless: " + fault.getMessage());
                            Toast.makeText(RegistroActivity.this, "Error al registrar en Backendless: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void handleFault(BackendlessFault fault) {
                    Log.e("RegistroActivity", "Error al subir foto: " + fault.getMessage());
                    Toast.makeText(RegistroActivity.this, "Error al subir foto: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            Log.e("RegistroActivity", "Error al procesar imagen: " + e.getMessage());
            Toast.makeText(RegistroActivity.this, "Error al procesar imagen", Toast.LENGTH_LONG).show();
        }
    }

    // Metodo para enviar el correo de bienvenida
    private void enviarCorreoBienvenida(String email, String nombre) {
        String templateName = "User Made Registration";

        List<String> recipients = new ArrayList<>();
        recipients.add(email);

        EmailEnvelope emailEnvelope = new EmailEnvelope();

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("nombre", nombre);

        Backendless.Messaging.sendEmailFromTemplate(
                templateName,
                emailEnvelope,
                placeholders,
                new AsyncCallback<MessageStatus>() {
                    @Override
                    public void handleResponse(MessageStatus response) {
                        Log.d("RegistroActivity", "Correo de bienvenida enviado con éxito");
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("RegistroActivity", "Error al enviar correo de bienvenida: " + fault.getMessage());
                    }
                });
    }

    private void mostrarTerminosYCondiciones() {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("orden ASC");

        Backendless.Data.of("TerminosCondiciones").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> terminos) {
                if (!terminos.isEmpty()) {
                    StringBuilder contenidoCompleto = new StringBuilder();
                    for (Map<String, Object> registroTerminos : terminos) {
                        String contenido = (String) registroTerminos.get("contenido");
                        if (contenido != null) {
                            contenidoCompleto.append(contenido).append("\n\n");
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegistroActivity.this);
                    builder.setTitle("Términos y Condiciones");
                    builder.setMessage(contenidoCompleto.toString());
                    builder.setPositiveButton("Cerrar", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.show();
                } else {
                    Toast.makeText(RegistroActivity.this, "Error: No se encontraron los términos y condiciones.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(RegistroActivity.this, "Error al cargar los términos: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File convertirBitmapAFile(Bitmap bitmap) throws IOException {
        File archivo = new File(getCacheDir(), "fotoPerfil.jpg");
        FileOutputStream fos = new FileOutputStream(archivo);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return archivo;
    }

    private void validarCampos() {
        boolean fotoTomada = fotoTomadaCorrectamente;
        boolean fechaValida = !inputFechaNac.getText().toString().trim().isEmpty();
        boolean nombreValido = !inputNombre.getText().toString().trim().isEmpty();
        boolean apellidosValidos = !inputApellidos.getText().toString().trim().isEmpty();
        boolean correoValido = !inputCorreo.getText().toString().trim().isEmpty();
        boolean claveValida = !inputClave.getText().toString().trim().isEmpty();
        boolean clave2Valida = !inputClave2.getText().toString().trim().isEmpty();
        boolean dniValido = !inputDni.getText().toString().trim().isEmpty();
        boolean clavesCoinciden = inputClave.getText().toString().equals(inputClave2.getText().toString());
        boolean terminosAceptados = chkTerminos.isChecked();

        boolean habilitar = fotoTomada && fechaValida && nombreValido && apellidosValidos &&
                correoValido && claveValida && clave2Valida && dniValido && clavesCoinciden && terminosAceptados;

        btnEnviarRegistro.setEnabled(habilitar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FOTO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                Bitmap foto = (Bitmap) extras.get("data");
                imgFotoPerfil.setImageBitmap(foto);
                fotoTomadaCorrectamente = true;
                validarCampos();
            } else {
                Toast.makeText(this, "No se recibió imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}