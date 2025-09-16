package com.example.proyectoappteam.actividades;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectoappteam.R;

import java.util.Calendar;

public class RegistroActivity extends AppCompatActivity {

    EditText inputFechaNac, inputNombre, inputApellidos, inputCorreo, inputClave, inputClave2;
    Button btnSeleccionarFecha, btnVolverInicio, btnVerTerminos, btnEnviarRegistro, btnTomarFoto;
    CheckBox chkTerminos;
    ImageView imgFotoPerfil;
    Calendar calendario;
    static final int REQUEST_FOTO = 1001;
    static final int PERMISO_CAMARA = 200;

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

        // Solicitar permiso de cámara si no está concedido
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
            String terminos = "TÉRMINOS Y CONDICIONES – VeciRed\n\n" +
                    "1. Finalidad institucional del aplicativo:\n" +
                    "VeciRed es una plataforma comunitaria diseñada para fortalecer la comunicación entre vecinos, facilitar la publicación de eventos relevantes, recibir notificaciones institucionales y gestionar el perfil de cada usuario con trazabilidad.\n\n" +
                    "2. Acceso y registro:\n" +
                    "Solo usuarios autorizados por la comunidad o institución pueden registrarse. El registro implica la aceptación de estos términos y el compromiso de uso responsable.\n\n" +
                    "3. Publicaciones comunitarias:\n" +
                    "Los usuarios pueden crear publicaciones relacionadas con eventos, avisos, ventas o solicitudes. Todo contenido debe respetar las normas de convivencia y estar alineado a los objetivos comunitarios.\n\n" +
                    "4. Notificaciones institucionales:\n" +
                    "La aplicación permite recibir notificaciones sobre noticias, alertas o reacciones a publicaciones. Estas notificaciones son gestionadas por el sistema y pueden incluir información relevante para la comunidad.\n\n" +
                    "5. Gestión de perfil:\n" +
                    "Cada usuario puede configurar su perfil, actualizar datos personales y cerrar sesión de forma segura. La información registrada será utilizada únicamente para fines comunitarios y no será compartida con terceros.\n\n" +
                    "6. Moderación y responsabilidad:\n" +
                    "VeciRed se reserva el derecho de moderar contenido que infrinja las normas comunitarias. El uso indebido del sistema puede conllevar suspensión temporal o definitiva del acceso.\n\n" +
                    "7. Aceptación de condiciones:\n" +
                    "Al utilizar VeciRed, el usuario acepta estos términos y se compromete a respetar la lógica institucional, la trazabilidad de sus acciones y la convivencia digital.";

            new android.app.AlertDialog.Builder(RegistroActivity.this)
                    .setTitle("Términos y Condiciones")
                    .setMessage(terminos)
                    .setPositiveButton("Aceptar", null)
                    .show();
        });

        btnTomarFoto.setOnClickListener(v -> {
            Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(tomarFoto, REQUEST_FOTO);
        });

        btnVolverInicio.setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, PrincipalActivity.class));
            finish();
        });
    }

    private void validarCampos() {
        boolean fotoTomada = imgFotoPerfil.getDrawable() != null;
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
                validarCampos();
            } else {
                Toast.makeText(this, "No se recibió imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}