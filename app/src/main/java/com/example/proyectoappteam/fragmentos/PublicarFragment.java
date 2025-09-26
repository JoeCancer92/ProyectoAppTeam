package com.example.proyectoappteam.fragmentos;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.example.proyectoappteam.actividades.MapsActivity;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Publicaciones;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublicarFragment extends Fragment {

    private TextView tvUsuario;
    private RadioGroup rgCategoria;
    private com.google.android.material.textfield.TextInputEditText etDescripcion;
    private TextView tvUbicacionSeleccionada;
    private SwitchCompat switchUrgente;
    private Button btnPublicar;
    private Button btnCancelarPublic;
    private Button btnAbrirMapa;
    private Button btnAgregarFotos;
    private LinearLayout tipsLayout;
    // ELIMINADA: private TextView tvEmail;

    private List<Uri> selectedPhotoUris = new ArrayList<>();
    private double selectedLatitud = 0.0;
    private double selectedLongitud = 0.0;
    private String selectedAddressName;
    private Uri currentPhotoUri;

    // *** SOLUCIÓN 1: Variable para persistir el valor de la categoría ***
    private String categoriaSeleccionada = "";

    // TAG de depuración para Logcat
    private static final String TAG = "PublicarFragment";

    // ActivityResultLauncher para el mapa
    private final ActivityResultLauncher<Intent> mapActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLatitud = data.getDoubleExtra("lat", 0.0);
                    selectedLongitud = data.getDoubleExtra("lng", 0.0);
                    selectedAddressName = data.getStringExtra("nombre_lugar");

                    if (selectedAddressName != null && !selectedAddressName.isEmpty()) {
                        tvUbicacionSeleccionada.setText(selectedAddressName);
                    } else {
                        String ubicacion = String.format("Lat: %.4f, Long: %.4f", selectedLatitud, selectedLongitud);
                        tvUbicacionSeleccionada.setText(ubicacion);
                    }
                }
            });

    // ActivityResultLauncher para tomar fotos con la cámara
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    if (currentPhotoUri != null) {
                        selectedPhotoUris.add(currentPhotoUri);
                        Toast.makeText(getContext(), "Foto tomada y lista para subir.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No se pudo tomar la foto.", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher para seleccionar fotos de la galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedPhotoUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        selectedPhotoUris.add(imageUri);
                    }
                    Toast.makeText(getContext(), selectedPhotoUris.size() + " foto(s) seleccionada(s).", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher para solicitar permisos
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePicture();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado. No se puede tomar la foto.", Toast.LENGTH_SHORT).show();
                }
            });


    public PublicarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publicar, container, false);

        // Bind UI elements to variables
        tvUsuario = view.findViewById(R.id.Usuario);
        rgCategoria = view.findViewById(R.id.rgCategoria);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        tvUbicacionSeleccionada = view.findViewById(R.id.tvUbicacionSeleccionada);
        switchUrgente = view.findViewById(R.id.switchUrgente);
        btnPublicar = view.findViewById(R.id.btnPublicar);
        btnCancelarPublic = view.findViewById(R.id.btnCancelarPublic);
        btnAbrirMapa = view.findViewById(R.id.btnAbrirMapa);
        btnAgregarFotos = view.findViewById(R.id.btnAgregarFotos);
        tipsLayout = view.findViewById(R.id.tips_layout);
        // ELIMINADO: tvEmail = view.findViewById(R.id.user_id_display);

        // *** INICIALIZACIÓN DE CATEGORÍA POR DEFECTO ***
        // Esto asegura que 'categoriaSeleccionada' tiene un valor válido al iniciar,
        // eliminando la posibilidad de que se quede vacía si el RadioGroup falla en el estado inicial.
        RadioButton rbDefault = view.findViewById(R.id.rbObjetosPerdidos);
        if (rbDefault != null) {
            rbDefault.setChecked(true);
            categoriaSeleccionada = rbDefault.getText().toString(); // Asigna "Objetos Perdidos"
            Log.d(TAG, "Categoria inicial asignada: " + categoriaSeleccionada);
        }
        // ************************************************

        // Call methods to load data
        cargarDatosUsuario();
        cargarConsejos();

        // *** SOLUCIÓN 2: Implementar el Listener para capturar la categoría inmediatamente al cambio ***
        if (rgCategoria != null) {
            rgCategoria.setOnCheckedChangeListener((group, checkedId) -> {
                // Solo se ejecuta si hay una selección válida
                if (checkedId != -1) {
                    View selectedRadioButton = group.findViewById(checkedId);
                    if (selectedRadioButton instanceof RadioButton) {
                        // Actualiza la variable de clase inmediatamente
                        categoriaSeleccionada = ((RadioButton) selectedRadioButton).getText().toString();
                        Log.d(TAG, "Categoria seleccionada en tiempo real: " + categoriaSeleccionada);
                    }
                } else {
                    // Aunque la inicialización previene esto, es buena práctica manejarlo
                    categoriaSeleccionada = "";
                }
            });
        }
        // *********************************************************************************

        // Set up OnClickListeners for the buttons
        if (btnCancelarPublic != null) {
            btnCancelarPublic.setOnClickListener(v -> limpiarCampos());
        }

        if (btnPublicar != null) {
            btnPublicar.setOnClickListener(v -> publicarInformacion());
        }

        if (btnAbrirMapa != null) {
            btnAbrirMapa.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                mapActivityResultLauncher.launch(intent);
            });
        }

        if (btnAgregarFotos != null) {
            btnAgregarFotos.setOnClickListener(v -> showImagePickerOptions());
        }

        return view;
    }

    /**
     * Muestra un diálogo para que el usuario elija entre tomar una foto o seleccionar de la galería.
     */
    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agregar Foto");
        builder.setItems(new CharSequence[]{"Tomar foto", "Elegir de la galería"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndTakePicture();
                            break;
                        case 1:
                            openGallery();
                            break;
                    }
                });
        builder.create().show();
    }

    /**
     * Revisa si se tiene permiso para usar la cámara y, si no, lo solicita.
     */
    private void checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    /**
     * Inicia la actividad de la cámara para tomar una foto.
     */
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error al crear el archivo de la imagen: " + ex.getMessage());
            Toast.makeText(getContext(), "Error al crear el archivo para la foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.proyectoappteam.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            takePictureLauncher.launch(currentPhotoUri);
        }
    }

    /**
     * Crea un archivo de imagen para la foto tomada por la cámara.
     */
    private File createImageFile() throws IOException {
        String uniqueFileName = "IMG_" + UUID.randomUUID().toString(); // Nombre único
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(uniqueFileName, ".jpg", storageDir);
    }

    /**
     * Inicia la actividad para seleccionar una o varias fotos de la galería.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }


    /**
     * Carga el nombre y apellido del usuario logueado desde Backendless y lo muestra en la UI.
     */
    private void cargarDatosUsuario() {
        try {
            BackendlessUser currentUser = Backendless.UserService.CurrentUser();
            if (currentUser != null) {
                if (tvUsuario != null) {
                    String nombre = (String) currentUser.getProperty("nombre");
                    String apellidos = (String) currentUser.getProperty("apellidos");
                    // Ahora solo muestra el nombre y apellido
                    tvUsuario.setText(String.format("Bienvenido, %s %s", nombre, apellidos));
                }
                // ELIMINADO: Lógica relacionada con tvEmail
            } else {
                if (tvUsuario != null) {
                    tvUsuario.setText("Usuario no autenticado");
                }
                // ELIMINADO: Lógica relacionada con tvEmail
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar datos del usuario: " + e.getMessage(), e);
            if (tvUsuario != null) {
                tvUsuario.setText("Error al cargar usuario");
            }
            // ELIMINADO: Lógica relacionada con tvEmail
        }
    }

    /**
     * Carga los consejos de la base de datos de Backendless y los muestra en la UI.
     */
    private void cargarConsejos() {
        if (tipsLayout == null) {
            Log.e(TAG, "Error: tipsLayout es null. No se pueden cargar los consejos.");
            return;
        }

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("orden");

        Backendless.Data.of("Consejos").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> consejosData) {
                if (tipsLayout == null) return;
                tipsLayout.removeAllViews();
                if (consejosData != null && !consejosData.isEmpty()) {
                    for (Map consejoMap : consejosData) {
                        TextView tvConsejo = new TextView(getContext());
                        String descripcion = (String) consejoMap.get("description");
                        if (descripcion != null) {
                            tvConsejo.setText(descripcion);
                            tvConsejo.setPadding(0, 8, 0, 8);
                            tipsLayout.addView(tvConsejo);
                        }
                    }
                } else {
                    TextView tvNoConsejos = new TextView(getContext());
                    tvNoConsejos.setText("No hay consejos disponibles en este momento.");
                    tipsLayout.addView(tvNoConsejos);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (tipsLayout == null) return;
                TextView tvError = new TextView(getContext());
                tvError.setText("Error al cargar consejos: " + fault.getMessage());
                tipsLayout.addView(tvError);
                Log.e(TAG, "Error loading tips: " + fault.getMessage());
            }
        });
    }

    /**
     * Inicia el proceso de publicación, subiendo las fotos y luego guardando la publicación.
     */
    private void publicarInformacion() {
        if (etDescripcion == null || switchUrgente == null) {
            Toast.makeText(getContext(), "Error: No se pueden obtener los elementos de la interfaz de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = etDescripcion.getText().toString().trim();

        // Usamos la variable de clase 'categoriaSeleccionada'
        String categoria = categoriaSeleccionada;

        if (descripcion.isEmpty() || categoria.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa la descripción y selecciona una categoría.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "FALLO AL PUBLICAR: Categoria vacia o Descripcion vacia.");
            return;
        }

        if (selectedPhotoUris.isEmpty()) {
            // Si no hay fotos, guardar la publicación directamente
            guardarPublicacion(new ArrayList<>());
        } else {
            // Si hay fotos, iniciar el proceso de subida
            final List<String> uploadedPhotoUrls = new ArrayList<>();
            final int[] fotosSubidasCount = {0};
            final int totalFotos = selectedPhotoUris.size();

            // Bucle para subir cada foto de la lista
            for (Uri photoUri : selectedPhotoUris) {
                uploadFileFromUri(photoUri, new AsyncCallback<String>() {
                    @Override
                    public void handleResponse(String uploadedUrl) {
                        uploadedPhotoUrls.add(uploadedUrl);
                        fotosSubidasCount[0]++;
                        // Comprobar si todas las fotos han sido subidas
                        if (fotosSubidasCount[0] == totalFotos) {
                            guardarPublicacion(uploadedPhotoUrls);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(TAG, "Error al subir la foto: " + fault.getMessage());
                        Toast.makeText(getContext(), "Error al subir una de las fotos: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    /**
     * Sube un archivo a Backendless.
     */
    private void uploadFileFromUri(Uri uri, AsyncCallback<String> callback) {
        try {
            // Crear un archivo temporal a partir del contenido de la URI
            File tempFile = createTempFileFromUri(getContext(), uri);
            if (tempFile == null) {
                callback.handleFault(new BackendlessFault("Error: No se pudo crear el archivo temporal."));
                return;
            }

            // Define la ruta en Backendless donde se guardará el archivo
            String filePath = "fotos/" + tempFile.getName();

            // Utilizar el método de subida para objetos File
            Backendless.Files.upload(tempFile, filePath, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile uploadedFile) {
                    tempFile.delete(); // Eliminar el archivo temporal
                    callback.handleResponse(uploadedFile.getFileURL());
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    tempFile.delete(); // Eliminar el archivo temporal incluso si falla
                    callback.handleFault(fault);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo desde la URI: " + e.getMessage());
            callback.handleFault(new BackendlessFault("Error de I/O: " + e.getMessage()));
        }
    }

    /**
     * Crea un archivo temporal a partir de una URI de contenido.
     */
    private File createTempFileFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        // Generar un nombre de archivo único con un UUID
        String uniqueFileName = "temp_" + UUID.randomUUID().toString() + ".jpg";

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            File tempFile = new File(context.getCacheDir(), uniqueFileName);

            fileOutputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            return tempFile;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /**
     * Intenta obtener el nombre del archivo de una URI.
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    /**
     * Guarda la publicación en la base de datos de Backendless.
     */
    private void guardarPublicacion(List<String> uploadedUrls) {
        String descripcion = etDescripcion.getText().toString().trim();

        // *** SOLUCIÓN 3: Usar la variable de clase que garantiza el valor ***
        String categoria = categoriaSeleccionada;

        // REGISTRO DE DEPURACIÓN FINAL
        Log.d(TAG, "Categoria final a guardar: '" + categoria + "'");


        Publicaciones nuevaPublicacion = new Publicaciones();
        nuevaPublicacion.setDescripcion(descripcion);
        nuevaPublicacion.setCategoria(categoria);
        nuevaPublicacion.setEsUrgente(switchUrgente.isChecked());
        nuevaPublicacion.setLatitud(selectedLatitud);
        nuevaPublicacion.setLongitud(selectedLongitud);
        nuevaPublicacion.setUbicacion(selectedAddressName);

        // Concatenar las URLs de las fotos
        if (!uploadedUrls.isEmpty()) {
            String fotosUrls = String.join(",", uploadedUrls);
            nuevaPublicacion.setFotos(fotosUrls);
        } else {
            nuevaPublicacion.setFotos("");
        }

        // Guardar el objeto en la base de datos
        Backendless.Data.of(Publicaciones.class).save(nuevaPublicacion, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones response) {
                Toast.makeText(getContext(), "Publicación guardada exitosamente.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Publicación guardada con objectId: " + response.getObjectId());
                limpiarCampos();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), "Error al guardar: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error saving the post: " + fault.getMessage());
            }
        });
    }

    private void limpiarCampos() {
        if (etDescripcion != null) etDescripcion.setText("");
        if (rgCategoria != null) rgCategoria.clearCheck();
        if (switchUrgente != null) switchUrgente.setChecked(false);
        if (tvUbicacionSeleccionada != null) tvUbicacionSeleccionada.setText(R.string.ubicacion_seleccionada_hint);
        selectedPhotoUris.clear();
        selectedLatitud = 0.0;
        selectedLongitud = 0.0;
        selectedAddressName = null;

        // **Importante:** Limpiar y reasignar el valor del nuevo String Resource
        // Esto es crucial para que el RadioButton por defecto tenga el valor correcto
        categoriaSeleccionada = getString(R.string.objetos_perdidos);

        // Opcional: Re-seleccionar visualmente el botón (aunque el código de onCreateView ya lo hace)
        if (rgCategoria != null) {
            RadioButton rbDefault = requireView().findViewById(R.id.rbObjetosPerdidos);
            if (rbDefault != null) {
                rbDefault.setChecked(true);
            }
        }
    }
}