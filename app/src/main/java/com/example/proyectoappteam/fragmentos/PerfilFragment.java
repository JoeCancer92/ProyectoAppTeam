package com.example.proyectoappteam.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.actividades.PrincipalActivity;
import com.example.proyectoappteam.clases.Menu;

public class PerfilFragment extends Fragment {

    public PerfilFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Lógica para el botón de "Volver al Menú"
        Button btnVolver = view.findViewById(R.id.btnVolverMenu);
        btnVolver.setOnClickListener(v -> {
            if (getActivity() instanceof Menu) {
                ((Menu) getActivity()).onClickMenu(-1);
            }
        });

        // Lógica para el botón de "Cerrar Sesión"
        Button btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            Backendless.UserService.logout(new AsyncCallback<Void>() {
                @Override
                public void handleResponse(Void response) {
                    Toast.makeText(getContext(), "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show();

                    // Redirigir al usuario a la actividad principal
                    Intent intent = new Intent(getActivity(), PrincipalActivity.class);
                    // Estas banderas previenen que el usuario regrese con el botón "atrás"
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(getContext(), "Error al cerrar sesión: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }
}