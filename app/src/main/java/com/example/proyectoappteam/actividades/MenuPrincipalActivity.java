package com.example.proyectoappteam.actividades;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
import com.example.proyectoappteam.fragmentos.InicioFragment;
import com.example.proyectoappteam.fragmentos.MenuFragment;
import com.example.proyectoappteam.fragmentos.NotificacionFragment;
import com.example.proyectoappteam.fragmentos.PerfilFragment;
import com.example.proyectoappteam.fragmentos.PublicarFragment;

public class MenuPrincipalActivity extends AppCompatActivity implements Menu {

    private Fragment[] fragments;
    private MenuFragment menuFragment;
    private LinearLayout contenedorBotonesInferior;

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

        contenedorBotonesInferior = findViewById(R.id.contenedorBotonesInferior);
        if (contenedorBotonesInferior != null) {
            contenedorBotonesInferior.setVisibility(View.GONE); // Ocultar al iniciar
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        menuFragment = new MenuFragment();
        ft.add(R.id.priRelContenedor, menuFragment, "menu");
        ft.commit();

        fragments = new Fragment[]{
                new PerfilFragment(),       // 0
                new InicioFragment(),       // 1
                new PublicarFragment(),     // 2
                new NotificacionFragment()  // 3
        };

        // 🔗 Asignar listeners a botones del menú inferior
        ImageButton lateralBtnMiPerfil = findViewById(R.id.lateralBtnMiPerfil);
        ImageButton lateralBtnInicio = findViewById(R.id.lateralBtnInicio);
        ImageButton lateralBtnPublicar = findViewById(R.id.lateralBtnPublicar);
        ImageButton lateralBtnNotificaciones = findViewById(R.id.lateralBtnNotificaciones);

        if (lateralBtnMiPerfil != null) {
            lateralBtnMiPerfil.setOnClickListener(v -> onClickMenu(0));
        }
        if (lateralBtnInicio != null) {
            lateralBtnInicio.setOnClickListener(v -> onClickMenu(1));
        }
        if (lateralBtnPublicar != null) {
            lateralBtnPublicar.setOnClickListener(v -> onClickMenu(2));
        }
        if (lateralBtnNotificaciones != null) {
            lateralBtnNotificaciones.setOnClickListener(v -> onClickMenu(3));
        }
    }

    @Override
    public void onClickMenu(int id) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        for (Fragment f : fragments) {
            if (f.isAdded()) ft.hide(f);
        }

        if (id == -1) {
            ft.show(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.GONE); // Ocultar si vuelve al menú
            }
        } else {
            Fragment target = fragments[id];
            if (target.isAdded()) {
                ft.show(target);
            } else {
                ft.add(R.id.priRelContenedor, target);
            }
            ft.hide(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.VISIBLE); // Mostrar al navegar
            }
        }

        ft.commit();
    }
}