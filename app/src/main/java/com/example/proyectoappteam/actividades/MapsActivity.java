package com.example.proyectoappteam.actividades;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.example.proyectoappteam.R;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnSelectLocation;
    private Button btnCloseMap; // Nuevo botón para cerrar el mapa
    private LatLng selectedLocation;
    private String selectedAddressName;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean isViewingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnCloseMap = findViewById(R.id.btnCloseMap); // Enlazar el nuevo botón

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitud") && intent.hasExtra("longitud")) {
            isViewingLocation = true;
            // Ocultar el botón de seleccionar y mostrar el de cerrar
            btnSelectLocation.setVisibility(Button.GONE);
            btnCloseMap.setVisibility(Button.VISIBLE);
        } else {
            // Mostrar el botón de seleccionar y ocultar el de cerrar
            btnSelectLocation.setVisibility(Button.VISIBLE);
            btnCloseMap.setVisibility(Button.GONE);
        }

        btnSelectLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", selectedLocation.latitude);
                resultIntent.putExtra("lng", selectedLocation.longitude);
                resultIntent.putExtra("nombre_lugar", selectedAddressName);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, selecciona una ubicación en el mapa.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el nuevo botón de cerrar
        btnCloseMap.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (isViewingLocation) {
            // Lógica para el modo "Ver Ubicación"
            Intent intent = getIntent();
            double lat = intent.getDoubleExtra("latitud", 0.0);
            double lng = intent.getDoubleExtra("longitud", 0.0);
            String title = intent.getStringExtra("markerTitle");

            if (lat != 0.0 && lng != 0.0) {
                LatLng location = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(location).title(title));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } else {
                Toast.makeText(this, "Ubicación no válida.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Lógica para el modo "Seleccionar Ubicación"
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    LatLng defaultLocation = new LatLng(-12.0464, -77.0428);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                }
            });

            mMap.setOnMapClickListener(latLng -> {
                selectedLocation = latLng;
                fetchAddressUsingGeocoder(latLng);
            });
        }
    }

    private void fetchAddressUsingGeocoder(LatLng latLng) {
        mMap.clear();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedAddressName = address.getAddressLine(0);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(selectedAddressName != null ? selectedAddressName : "Ubicación Seleccionada");
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {
                selectedAddressName = "Ubicación Desconocida";
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("Ubicación Desconocida");
                mMap.addMarker(markerOptions);
            }
        } catch (IOException e) {
            Log.e("MapsActivity", "Error de Geocodificación: " + e.getMessage());
            selectedAddressName = "Ubicación Seleccionada";
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Ubicación Seleccionada");
            mMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_LONG).show();
            }
        }
    }
}
