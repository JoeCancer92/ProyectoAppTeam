package com.example.proyectoappteam.actividades;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Usuario;
import com.example.proyectoappteam.clases.Seguridad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    EditText inputFechaNac, inputNombre, inputApellidos, inputCorreo, inputClave, inputClave2;
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
            Bitmap fotoBitmap = ((BitmapDrawable) imgFotoPerfil.getDrawable()).getBitmap();

            try {
                File archivo = convertirBitmapAFile(fotoBitmap);

                Backendless.Files.upload(archivo, "/fotos_perfil", true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile backendlessFile) {
                        String urlFoto = backendlessFile.getFileURL();

                        Usuario nuevoUsuario = new Usuario();
                        nuevoUsuario.setNombre(inputNombre.getText().toString().trim());
                        nuevoUsuario.setApellidos(inputApellidos.getText().toString().trim());
                        nuevoUsuario.setCorreo(inputCorreo.getText().toString().trim());
                        nuevoUsuario.setFechaNacimiento(inputFechaNac.getText().toString().trim());
                        nuevoUsuario.setUrlFoto(urlFoto);

                        // Aplicar hashing automático
                        String claveOriginal = inputClave.getText().toString().trim();
                        String claveHasheada = Seguridad.hashClave(claveOriginal);
                        nuevoUsuario.setClave(claveHasheada);

                        Backendless.Data.of(Usuario.class).save(nuevoUsuario, new AsyncCallback<Usuario>() {
                            @Override
                            public void handleResponse(Usuario response) {
                                Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistroActivity.this, PrincipalActivity.class));
                                finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(RegistroActivity.this, "Error al guardar usuario: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(RegistroActivity.this, "Error al subir foto: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (IOException e) {
                Toast.makeText(RegistroActivity.this, "Error al procesar imagen", Toast.LENGTH_LONG).show();
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
        // AHORA USA LA VARIABLE BOOLEANA EN LUGAR DE imgFotoPerfil.getDrawable() != null
        boolean fotoTomada = fotoTomadaCorrectamente;
        boolean fechaValida = !inputFechaNac.getText().toString().trim().isEmpty();
        boolean nombreValido = !inputNombre.getText().toString().trim().isEmpty();
        boolean apellidosValidos = !inputApellidos.getText().toString().trim().isEmpty();
        boolean correoValido = !inputCorreo.getText().toString().trim().isEmpty();
        boolean claveValida = !inputClave.getText().toString().trim().isEmpty();
        boolean clave2Valida = !inputClave2.getText().toString().trim().isEmpty();
        boolean clavesCoinciden = inputClave.getText().toString().equals(inputClave2.getText().toString());
        boolean terminosAceptados = chkTerminos.isChecked();

        boolean habilitar = fotoTomada && fechaValida && nombreValido && apellidosValidos &&
                correoValido && claveValida && clave2Valida && clavesCoinciden && terminosAceptados;

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
                // AÑADE ESTA LÍNEA PARA ACTIVAR EL INDICADOR DE FOTO
                fotoTomadaCorrectamente = true;
                validarCampos();
            } else {
                Toast.makeText(this, "No se recibió imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}