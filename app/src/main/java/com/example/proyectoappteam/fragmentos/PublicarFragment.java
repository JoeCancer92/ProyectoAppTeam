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

    private List<Uri> selectedPhotoUris = new ArrayList<>();
    private double selectedLatitud = 0.0;
    private double selectedLongitud = 0.0;
    private String selectedAddressName;
    private Uri currentPhotoUri;

    // Variable persistente para categoría
    private String categoriaSeleccionada = "";

    private static final String TAG = "PublicarFragment";

    // Launcher para mapa
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

    // Cámara
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

    // Galería
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

    // Permiso cámara
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publicar, container, false);

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

        //  Inicializar categoría por defecto con el texto del RadioButton
        RadioButton rbDefault = view.findViewById(R.id.rbReportes);
        if (rbDefault != null) {
            rbDefault.setChecked(true);
            categoriaSeleccionada = rbDefault.getText().toString();
            Log.d(TAG, "Categoria inicial asignada: " + categoriaSeleccionada);
        }

        cargarDatosUsuario();
        cargarConsejos();

        // Listener de categorías
        if (rgCategoria != null) {
            rgCategoria.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != -1) {
                    View selectedRadioButton = group.findViewById(checkedId);
                    if (selectedRadioButton instanceof RadioButton) {
                        categoriaSeleccionada = ((RadioButton) selectedRadioButton).getText().toString();
                        Log.d(TAG, "Categoria seleccionada en tiempo real: " + categoriaSeleccionada);
                    }
                } else {
                    categoriaSeleccionada = "";
                }
            });
        }

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

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agregar Foto");
        builder.setItems(new CharSequence[]{"Tomar foto", "Elegir de la galería"},
                (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndTakePicture();
                    } else {
                        openGallery();
                    }
                });
        builder.create().show();
    }

    private void checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
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

    private File createImageFile() throws IOException {
        String uniqueFileName = "IMG_" + UUID.randomUUID().toString();
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(uniqueFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }

    private void cargarDatosUsuario() {
        try {
            BackendlessUser currentUser = Backendless.UserService.CurrentUser();
            if (currentUser != null) {
                if (tvUsuario != null) {
                    String nombre = (String) currentUser.getProperty("nombre");
                    String apellidos = (String) currentUser.getProperty("apellidos");
                    tvUsuario.setText(String.format("Bienvenido, %s %s", nombre, apellidos));
                }
            } else {
                if (tvUsuario != null) {
                    tvUsuario.setText("Usuario no autenticado");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar datos del usuario: " + e.getMessage(), e);
            if (tvUsuario != null) {
                tvUsuario.setText("Error al cargar usuario");
            }
        }
    }

    private void cargarConsejos() {
        if (tipsLayout == null) return;

        tipsLayout.removeAllViews();

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("orden");

        Backendless.Data.of("Consejos").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> consejosData) {
                if (tipsLayout == null) return;

                if (consejosData != null && !consejosData.isEmpty()) {
                    for (Map consejoMap : consejosData) {
                        TextView tvConsejo = new TextView(getContext());
                        String descripcion = (String) consejoMap.get("description");
                        if (descripcion != null) {
                            tvConsejo.setText(descripcion);
                            tvConsejo.setPadding(0, 8, 0, 8);
                            tvConsejo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                            tipsLayout.addView(tvConsejo);
                        }
                    }
                } else {
                    TextView tvNoConsejos = new TextView(getContext());
                    tvNoConsejos.setText("No hay consejos disponibles en este momento.");
                    tvNoConsejos.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                    tipsLayout.addView(tvNoConsejos);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (tipsLayout == null) return;
                TextView tvError = new TextView(getContext());
                tvError.setText("Error al cargar consejos: " + fault.getMessage());
                tvError.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                tipsLayout.addView(tvError);
                Log.e(TAG, "Error loading tips: " + fault.getMessage());
            }
        });
    }

    private void publicarInformacion() {
        if (etDescripcion == null || switchUrgente == null) {
            Toast.makeText(getContext(), "Error: No se pueden obtener los elementos de la UI.", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = categoriaSeleccionada;

        if (descripcion.isEmpty() || categoria.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa la descripción y selecciona una categoría.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublicar.setEnabled(false);

        if (selectedPhotoUris.isEmpty()) {
            guardarPublicacion(new ArrayList<>());
        } else {
            final List<String> uploadedPhotoUrls = new ArrayList<>();
            final int[] fotosSubidasCount = {0};
            final int totalFotos = selectedPhotoUris.size();

            Toast.makeText(getContext(), "Iniciando subida de " + totalFotos + " foto(s)...", Toast.LENGTH_LONG).show();

            for (Uri photoUri : selectedPhotoUris) {
                uploadFileFromUri(photoUri, new AsyncCallback<String>() {
                    @Override
                    public void handleResponse(String uploadedUrl) {
                        uploadedPhotoUrls.add(uploadedUrl);
                        fotosSubidasCount[0]++;
                        if (fotosSubidasCount[0] == totalFotos) {
                            guardarPublicacion(uploadedPhotoUrls);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(TAG, "Error al subir la foto: " + fault.getMessage());
                        Toast.makeText(getContext(), "Error al subir una de las fotos: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        btnPublicar.setEnabled(true);
                    }
                });
            }
        }
    }

    private void uploadFileFromUri(Uri uri, AsyncCallback<String> callback) {
        try {
            File tempFile = createTempFileFromUri(getContext(), uri);
            if (tempFile == null) {
                callback.handleFault(new BackendlessFault("Error: No se pudo crear el archivo temporal."));
                return;
            }

            String filePath = "fotos/" + tempFile.getName();

            Backendless.Files.upload(tempFile, filePath, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile uploadedFile) {
                    tempFile.delete();
                    callback.handleResponse(uploadedFile.getFileURL());
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    tempFile.delete();
                    callback.handleFault(fault);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo desde la URI: " + e.getMessage());
            callback.handleFault(new BackendlessFault("Error de I/O: " + e.getMessage()));
        }
    }

    private File createTempFileFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        String uniqueFileName = "temp_" + UUID.randomUUID().toString() + ".jpg";

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), uniqueFileName);

            fileOutputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            return tempFile;
        } finally {
            if (inputStream != null) inputStream.close();
            if (fileOutputStream != null) fileOutputStream.close();
        }
    }

    private void guardarPublicacion(List<String> uploadedUrls) {
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = categoriaSeleccionada;

        Publicaciones nuevaPublicacion = new Publicaciones();
        nuevaPublicacion.setDescripcion(descripcion);
        nuevaPublicacion.setCategoria(categoria);
        nuevaPublicacion.setEsUrgente(switchUrgente.isChecked());
        nuevaPublicacion.setLatitud(selectedLatitud);
        nuevaPublicacion.setLongitud(selectedLongitud);
        nuevaPublicacion.setUbicacion(selectedAddressName);

        if (!uploadedUrls.isEmpty()) {
            String fotosUrls = String.join(",", uploadedUrls);
            nuevaPublicacion.setFotos(fotosUrls);
        } else {
            nuevaPublicacion.setFotos("");
        }

        Backendless.Data.of(Publicaciones.class).save(nuevaPublicacion, new AsyncCallback<Publicaciones>() {
            @Override
            public void handleResponse(Publicaciones response) {
                Toast.makeText(getContext(), "Publicación guardada exitosamente.", Toast.LENGTH_SHORT).show();
                limpiarCampos();
                btnPublicar.setEnabled(true);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), "Error al guardar: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                btnPublicar.setEnabled(true);
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

        //  Re-seleccionamos la categoría por defecto
        RadioButton rbDefault = requireView().findViewById(R.id.rbReportes);
        if (rbDefault != null) {
            rbDefault.setChecked(true);
            categoriaSeleccionada = rbDefault.getText().toString();
        }
    }
}
