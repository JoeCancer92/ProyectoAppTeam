package com.example.proyectoappteam.actividades;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.Menu;
import com.example.proyectoappteam.fragmentos.EventosFragment;
import com.example.proyectoappteam.fragmentos.MapasFragment;
import com.example.proyectoappteam.fragmentos.PerfilFragment;
import com.example.proyectoappteam.fragmentos.PreferenciasFragment;

public class MenuPrincipalActivity extends AppCompatActivity implements Menu {

    Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fragments = new Fragment[]{
                new PerfilFragment(),
                new EventosFragment(),
                new MapasFragment(),
                new PreferenciasFragment()
        };

        onClickMenu(0); // Carga inicial
    }

    @Override
    public void onClickMenu(int id) {
        if (id < 0 || id >= fragments.length) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.priRelContenedor, fragments[id]);
        ft.commit();
    }
}