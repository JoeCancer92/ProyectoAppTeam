package com.example.proyectoappteam.fragmentos;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.actividades.PrincipalActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";

    private TextInputEditText inputNombre, inputApellidos, inputCorreo, inputDni, inputFechaNacimiento;
    private Button btnEditarPerfil, btnGuardarCambios, btnCerrarSesion, btnCambiarContrasena, btnCambiarFoto, btnEditarFechaNacimiento, btnCancelar;
    private ImageView imgFotoPerfil;
    private ProgressBar progressBar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Map<String, Object> usuarioData;
    private Uri fotoCamaraUri;
    private File fotoTempFile;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    if (fotoCamaraUri != null) {
                        subirFoto(fotoCamaraUri);
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Toma de foto cancelada o fallida.", Toast.LENGTH_SHORT).show();
                    }
                    if (fotoTempFile != null && fotoTempFile.exists()) {
                        fotoTempFile.delete();
                    }
                }
            });

    public PerfilFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        inputNombre = view.findViewById(R.id.inputNombre);
        inputApellidos = view.findViewById(R.id.inputApellidos);
        inputCorreo = view.findViewById(R.id.inputCorreo);
        inputDni = view.findViewById(R.id.inputDni);
        inputFechaNacimiento = view.findViewById(R.id.inputFechaNacimiento);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        btnEditarFechaNacimiento = view.findViewById(R.id.btnEditarFechaNacimiento);
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        progressBar = view.findViewById(R.id.progressBar);

        // Inicializar el estado de la UI
        updateUIState(false);

        // Cargar los datos del usuario al inicio
        cargarDatosUsuario();

        // Asignar los listeners a los botones
        btnEditarPerfil.setOnClickListener(v -> updateUIState(true));
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> {
            updateUIState(false);
            cargarDatosUsuario();
        });
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnCambiarContrasena.setOnClickListener(v -> mostrarDialogoCambiarContrasena());
        btnCambiarFoto.setOnClickListener(v -> tomarNuevaFoto());
        btnEditarFechaNacimiento.setOnClickListener(v -> showDatePickerDialog());
    }

    /**
     * Carga los datos del usuario de la tabla Usuario y la URL de la foto de la tabla Users.
     */
    private void cargarDatosUsuario() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: No hay usuario logueado. Inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        String whereClause = "ownerId = '" + currentUser.getObjectId() + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("Usuario").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> usuarioList) {
                if (!usuarioList.isEmpty()) {
                    usuarioData = usuarioList.get(0);
                    inputNombre.setText((String) usuarioData.get("nombre"));
                    inputApellidos.setText((String) usuarioData.get("apellidos"));
                    inputDni.setText((String) usuarioData.get("dni"));
                    inputCorreo.setText((String) usuarioData.get("correo"));

                    String fechaNacimientoStr = (String) usuarioData.get("fechanacimiento");
                    if (fechaNacimientoStr != null) {
                        inputFechaNacimiento.setText(fechaNacimientoStr);
                    } else {
                        inputFechaNacimiento.setText("");
                    }

                    // Ahora buscamos la URL de la foto en la propiedad 'urlfoto' del objeto principal del usuario
                    String urlFoto = (String) currentUser.getProperty("urlfoto");

                    Log.d(TAG, "URL de foto recuperada de la tabla Users: " + urlFoto);

                    if (urlFoto != null && !urlFoto.isEmpty()) {
                        downloadAndSetImage(urlFoto);
                    } else {
                        imgFotoPerfil.setImageResource(R.drawable.foto87);
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Datos de perfil no encontrados en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar datos del perfil: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
                Log.e(TAG, "Error al cargar datos del perfil: " + fault.getMessage());
            }
        });
    }

    /**
     * Descarga y muestra la imagen de perfil usando ExecutorService.
     */
    private void downloadAndSetImage(String urlFoto) {
        executorService.execute(() -> {
            Bitmap bitmap = null;
            try (InputStream in = new URL(urlFoto).openStream()) {
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "Error al descargar la imagen. Asegúrate de que la URL sea válida: " + e.getMessage());
            }

            Bitmap finalBitmap = bitmap;
            handler.post(() -> {
                if (finalBitmap != null) {
                    imgFotoPerfil.setImageBitmap(finalBitmap);
                } else {
                    imgFotoPerfil.setImageResource(R.drawable.foto87);
                }
            });
        });
    }

    /**
     * Habilita o deshabilita la edición de los campos y cambia la visibilidad de los botones.
     */
    private void updateUIState(boolean editable) {
        setCamposEditables(editable);

        btnEditarPerfil.setVisibility(editable ? View.GONE : View.VISIBLE);
        btnGuardarCambios.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnCancelar.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnCerrarSesion.setVisibility(editable ? View.GONE : View.VISIBLE);
        btnCambiarContrasena.setVisibility(editable ? View.GONE : View.VISIBLE);
        btnCambiarFoto.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnEditarFechaNacimiento.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void setCamposEditables(boolean editable) {
        inputNombre.setEnabled(editable);
        inputApellidos.setEnabled(editable);
        inputDni.setEnabled(false);
        inputCorreo.setEnabled(false);
        inputFechaNacimiento.setClickable(editable);
        inputFechaNacimiento.setFocusable(editable);
        inputFechaNacimiento.setFocusableInTouchMode(editable);
    }

    /**
     * Muestra el DatePickerDialog para seleccionar la fecha de nacimiento.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        try {
            if (inputFechaNacimiento.getText() != null && !inputFechaNacimiento.getText().toString().isEmpty()) {
                calendar.setTime(dateFormat.parse(inputFechaNacimiento.getText().toString()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear la fecha para el DatePicker. Usando la fecha actual.");
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    inputFechaNacimiento.setText(dateFormat.format(selectedDate.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Muestra un cuadro de diálogo para cambiar la contraseña.
     */
    private void mostrarDialogoCambiarContrasena() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cambiar_contrasena, null);
        builder.setView(dialogView);

        final EditText inputNuevaContrasena = dialogView.findViewById(R.id.inputNuevaContrasena);
        final EditText inputConfirmarContrasena = dialogView.findViewById(R.id.inputConfirmarContrasena);

        builder.setTitle("Cambiar Contraseña")
                .setPositiveButton("Aceptar", (dialog, id) -> {
                    String nuevaContrasena = inputNuevaContrasena.getText().toString();
                    String confirmarContrasena = inputConfirmarContrasena.getText().toString();

                    if (nuevaContrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!nuevaContrasena.equals(confirmarContrasena)) {
                        Toast.makeText(getContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarContrasena(nuevaContrasena);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Solo actualiza la contraseña en la tabla Users y luego cierra la sesión.
     * La sincronización con la tabla Usuario se hará en el próximo inicio de sesión.
     */
    private void actualizarContrasena(String nuevaContrasena) {
        if (getContext() == null) return;

        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error de sesión. Por favor, inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        currentUser.setPassword(nuevaContrasena);
        Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser updatedUser) {
                Log.d(TAG, "Contraseña actualizada en la tabla Users. Cerrando sesión...");
                Toast.makeText(getContext(), "Contraseña actualizada. Vuelve a iniciar sesión por favor", Toast.LENGTH_LONG).show();
                cerrarSesion();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error al actualizar la contraseña en la tabla Users: " + fault.getMessage());
                if (fault.getCode() != null && fault.getCode().equals("3076") || fault.getMessage().contains("Not existing user token")) {
                    Toast.makeText(getContext(), "Sesión expirada. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getActivity(), PrincipalActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Error al cambiar la contraseña: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Inicia el intent de la cámara para tomar una nueva foto.
     */
    private void tomarNuevaFoto() {
        if (getContext() == null) {
            Log.e(TAG, "No se puede iniciar la cámara.");
            return;
        }

        try {
            fotoTempFile = crearArchivoDeFoto();
            fotoCamaraUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.proyectoappteam.fileprovider",
                    fotoTempFile);

            cameraLauncher.launch(fotoCamaraUri);
        } catch (Exception ex) {
            Log.e(TAG, "Error al crear el archivo de la foto: " + ex.getMessage());
            Toast.makeText(getContext(), "Error al crear el archivo de foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoDeFoto() throws java.io.IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Sube la foto seleccionada a Backendless.
     */
    private void subirFoto(Uri fotoUri) {
        if (fotoUri == null || getContext() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No se pudo obtener la URI de la imagen.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        final File finalFotoFile = fotoTempFile;
        String fileName = finalFotoFile.getName();

        Backendless.Files.upload(finalFotoFile, "media", new AsyncCallback<BackendlessFile>() {
            @Override
            public void handleResponse(BackendlessFile uploadedFile) {
                if (finalFotoFile != null && finalFotoFile.exists()) {
                    finalFotoFile.delete();
                }
                String nuevaUrlFoto = uploadedFile.getFileURL();
                actualizarUrlFotoEnUsuario(nuevaUrlFoto);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (finalFotoFile != null && finalFotoFile.exists()) {
                    finalFotoFile.delete();
                }
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al subir la imagen a Backendless: " + fault.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al subir la imagen: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Actualiza la URL de la foto en la tabla de Usuario y en la de Users después de la subida.
     */
    private void actualizarUrlFotoEnUsuario(String urlFoto) {
        if (usuarioData == null || getContext() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: no se pueden actualizar los datos.", Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.GONE);
            return;
        }

        // 1. Actualiza la propiedad 'urlfoto' del usuario en la tabla Users
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        currentUser.setProperty("urlfoto", urlFoto);

        Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                // 2. Si la actualización en la tabla Users es exitosa, actualiza la tabla Usuario
                usuarioData.put("urlfoto", urlFoto);
                Backendless.Data.of("Usuario").save(usuarioData, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map updatedUser) {
                        progressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Foto de perfil actualizada con éxito.", Toast.LENGTH_SHORT).show();
                        }
                        cargarDatosUsuario(); // Refrescar la UI con la nueva foto
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error al actualizar la tabla Usuario: " + fault.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al actualizar la foto: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al actualizar la URL de la foto en la tabla Users: " + fault.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al actualizar la foto: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Guarda los cambios del perfil en la tabla Usuario.
     */
    private void guardarCambios() {
        if (usuarioData == null || getContext() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No se pudieron guardar los cambios. Datos de usuario incompletos.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String nuevoNombre = inputNombre.getText() != null ? inputNombre.getText().toString().trim() : "";
        String nuevoApellidos = inputApellidos.getText() != null ? inputApellidos.getText().toString().trim() : "";
        String fechaNacimientoStr = inputFechaNacimiento.getText() != null ? inputFechaNacimiento.getText().toString().trim() : "";

        if (nuevoNombre.isEmpty() || nuevoApellidos.isEmpty() || fechaNacimientoStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usamos el Map de datos existente y lo actualizamos
        usuarioData.put("nombre", nuevoNombre);
        usuarioData.put("apellidos", nuevoApellidos);
        usuarioData.put("fechanacimiento", fechaNacimientoStr);

        Backendless.Data.of("Usuario").save(usuarioData, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show();
                }
                updateUIState(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al actualizar la tabla Usuario: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
                updateUIState(false);
            }
        });
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    private void cerrarSesion() {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Sesión cerrada con éxito.", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getActivity(), PrincipalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cerrar sesión: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
