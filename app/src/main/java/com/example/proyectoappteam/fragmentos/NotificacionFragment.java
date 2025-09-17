package com.example.proyectoappteam.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Menu;

public class NotificacionFragment extends Fragment {

    public NotificacionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacion, container, false);

        Button btnVolver = view.findViewById(R.id.btnVolverMenu);
        btnVolver.setOnClickListener(v -> {
            if (getActivity() instanceof Menu) {
                ((Menu) getActivity()).onClickMenu(-1);
            }
        });

        return view;
    }
}