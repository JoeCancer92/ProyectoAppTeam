package com.example.proyectoappteam.fragmentos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Menu;

public class MenuFragment extends Fragment {

    private Menu menuCallback;

    public MenuFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Menu) {
            menuCallback = (Menu) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " debe implementar la interfaz Menu para navegación modular.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarBoton(view, R.id.fragImbMiPerfil, 0);
        configurarBoton(view, R.id.fragImbInicio, 1);
        configurarBoton(view, R.id.fragImbPublicar, 2);
        configurarBoton(view, R.id.fragImbNotificaciones, 3);
    }

    private void configurarBoton(View view, int botonId, int menuId) {
        ImageButton boton = view.findViewById(botonId);
        if (boton != null) {
            boton.setOnClickListener(v -> {
                if (menuCallback != null) menuCallback.onClickMenu(menuId);
            });
        }
    }
}