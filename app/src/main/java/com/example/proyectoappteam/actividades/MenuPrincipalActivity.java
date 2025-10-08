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
import com.example.proyectoappteam.clases.Usuario;
import com.example.proyectoappteam.fragmentos.InicioFragment;
import com.example.proyectoappteam.fragmentos.MenuFragment;
import com.example.proyectoappteam.fragmentos.NotificacionFragment;
import com.example.proyectoappteam.fragmentos.PerfilFragment;
import com.example.proyectoappteam.fragmentos.PublicarFragment;

public class MenuPrincipalActivity extends AppCompatActivity implements Menu {

    private Fragment[] fragments;
    private MenuFragment menuFragment;
    private LinearLayout contenedorBotonesInferior;

    public static Usuario usuarioActivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);

        // Aplicar márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recuperar usuario desde el Intent y validar estructura
        Object recibido = getIntent().getSerializableExtra("usuario");
        if (recibido instanceof Usuario) {
            Usuario recibidoUsuario = (Usuario) recibido;
            if (recibidoUsuario.getCorreo() != null && !recibidoUsuario.getCorreo().isEmpty()) {
                usuarioActivo = recibidoUsuario;
            } else {
                usuarioActivo = null; // Usuario inválido para flujos que requieren correo
            }
        } else {
            usuarioActivo = null;
        }

        // Ocultar botones inferiores al iniciar
        contenedorBotonesInferior = findViewById(R.id.contenedorBotonesInferior);
        if (contenedorBotonesInferior != null) {
            contenedorBotonesInferior.setVisibility(View.GONE);
        }

        // Inicializar fragmento de menú lateral
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        menuFragment = new MenuFragment();
        ft.add(R.id.priRelContenedor, menuFragment, "menu");
        ft.commit();

        // Inicializar fragmentos principales
        fragments = new Fragment[]{
                new PerfilFragment(),       // 0
                new InicioFragment(),       // 1
                new PublicarFragment(),     // 2
                new NotificacionFragment()  // 3
        };

        // Configurar navegación lateral
        configurarBotonLateral(R.id.lateralBtnMiPerfil, 0);
        configurarBotonLateral(R.id.lateralBtnInicio, 1);
        configurarBotonLateral(R.id.lateralBtnPublicar, 2);
        configurarBotonLateral(R.id.lateralBtnNotificaciones, 3);
    }

    private void configurarBotonLateral(int idBoton, int idFragmento) {
        ImageButton boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> onClickMenu(idFragmento));
        }
    }

    @Override
    public void onClickMenu(int id) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Ocultar todos los fragmentos activos
        for (Fragment f : fragments) {
            if (f.isAdded()) ft.hide(f);
        }

        if (id == -1) {
            // Mostrar menú lateral
            ft.show(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.GONE);
            }
        } else {
            // Mostrar fragmento seleccionado
            Fragment target = fragments[id];
            if (target.isAdded()) {
                ft.show(target);
            } else {
                ft.add(R.id.priRelContenedor, target);
            }
            ft.hide(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.VISIBLE);
            }
        }

        ft.commit();
    }
}