package com.example.proyectoappteam.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Menu;

public class InicioFragment extends Fragment {

    private Menu menuCallback;

    public InicioFragment() {
        // Constructor vacío requerido por el sistema
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //Validación institucional del contexto
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
        //Inflado del layout institucional
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Botón institucional para volver al menú principal
        Button btnVolver = view.findViewById(R.id.btnVolverMenu);
        btnVolver.setOnClickListener(v -> {
            if (menuCallback != null) {
                menuCallback.onClickMenu(-1); // -1 activa el menú institucional desde la actividad
            }
        });
    }
}