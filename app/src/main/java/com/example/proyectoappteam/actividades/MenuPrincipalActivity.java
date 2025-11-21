package com.example.proyectoappteam.actividades;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.content.Context; // Necesario para Context y attachBaseContext
import android.content.SharedPreferences; // Necesario para SharedPreferences
import android.content.res.Configuration; // Necesario para Configuration
import java.util.Locale; // Necesario para Locale
// Agrega esta si usaste la versión de LocaleHelper con verificación de SDK
import android.os.Build;

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

import com.example.proyectoappteam.fragmentos.ConfigFragment;
import com.example.proyectoappteam.fragmentos.InicioFragment;
import com.example.proyectoappteam.fragmentos.MenuFragment;
import com.example.proyectoappteam.fragmentos.NotificacionFragment;
import com.example.proyectoappteam.fragmentos.PerfilFragment;
import com.example.proyectoappteam.fragmentos.PublicarFragment;

import com.example.proyectoappteam.clases.LocaleHelper;

public class MenuPrincipalActivity extends AppCompatActivity implements Menu {

    private Fragment[] fragments;
    private MenuFragment menuFragment;
    private LinearLayout contenedorBotonesInferior;
    private ImageButton lastSelectedButton=null;
    public static Usuario usuarioActivo;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, "es"));
    }

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

        // Ocultar botones inferiores al iniciar (AHORA MUESTRA)
        contenedorBotonesInferior = findViewById(R.id.contenedorBotonesInferior);
        if (contenedorBotonesInferior != null) {
            contenedorBotonesInferior.setVisibility(View.VISIBLE);
        }

        // Inicializar fragmento de menú lateral
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        menuFragment = new MenuFragment();
        ft.add(R.id.priRelContenedor, menuFragment, "menu");
        // ft.commit();

        // Inicializar fragmentos principales
        fragments = new Fragment[]{
                new PerfilFragment(),       // 0
                new InicioFragment(),       // 1
                new PublicarFragment(),     // 2
                new NotificacionFragment(),  // 3
                new ConfigFragment()        // 4
        };

        Fragment inicioFragment = fragments[1];
        ft.add(R.id.priRelContenedor, inicioFragment);
        ft.hide(menuFragment);
        ft.commit();

        updateButtonSelection(1);


        // Configurar navegación lateral
        configurarBotonLateral(R.id.lateralBtnMiPerfil, 0);
        configurarBotonLateral(R.id.lateralBtnInicio, 1);
        configurarBotonLateral(R.id.lateralBtnPublicar, 2);
        configurarBotonLateral(R.id.lateralBtnNotificaciones, 3);
        configurarBotonLateral(R.id.lateralBtnConfig, 4);
    }

    private void configurarBotonLateral(int idBoton, int idFragmento) {
        ImageButton boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> onClickMenu(idFragmento));
        }
    }

    // Metodo para obtener la referencia del boton
    private ImageButton getButtonFromId(int idBoton) {
        return findViewById(idBoton);
    }

    // Metodo para gestionar la seleccion de botones
//    private void updateButtonSelection(int idFragmento) {
//        int buttonIds[] = new int[]{
//                R.id.lateralBtnMiPerfil,
//                R.id.lateralBtnInicio,
//                R.id.lateralBtnPublicar,
//                R.id.lateralBtnNotificaciones,
//                R.id.lateralBtnConfig
//        };
//        if (idFragmento >= 0 && idFragmento <= 4) {
//            ImageButton currentButton = getButtonFromId(buttonIds[idFragmento]);
//
//            if (currentButton != null) {
//                if (lastSelectedButton != null) {
//                    lastSelectedButton.setSelected(false);
//                }
//                currentButton.setSelected(true);
//                lastSelectedButton = currentButton;
//            }
//        }else if(idFragmento == 4){
//            // Deselecciona el botón anterior si existe
//            if (lastSelectedButton != null) {
//                lastSelectedButton.setSelected(false);
//                lastSelectedButton = null; // No hay un botón de configuración que mantener seleccionado
//            }
//        }
//    }

    // Metodo para gestionar la seleccion de botones
    private void updateButtonSelection(int idFragmento) {
        // Array que mapea el ID de fragmento (0-4) al ID del botón (lateralBtn...)
        int buttonIds[] = new int[]{
                R.id.lateralBtnMiPerfil,
                R.id.lateralBtnInicio,
                R.id.lateralBtnPublicar,
                R.id.lateralBtnNotificaciones,
                R.id.lateralBtnConfig // ID del botón de Configuración (posición 4)
        };

        // Aseguramos que el ID del fragmento esté dentro del rango del array (0 a 4)
        if (idFragmento >= 0 && idFragmento < buttonIds.length) {

            ImageButton currentButton = getButtonFromId(buttonIds[idFragmento]);

            if (currentButton != null) {
                // 1. Deseleccionar el botón anterior (si existe)
                if (lastSelectedButton != null) {
                    lastSelectedButton.setSelected(false);
                }

                // 2. Seleccionar el botón actual (¡Esto lo "pinta"!)
                currentButton.setSelected(true);

                // 3. Almacenar el botón para la próxima deselección
                lastSelectedButton = currentButton;
            }
        }
        // ¡Se elimina el bloque 'else if (idFragmento == 4)' que causaba la deselección!
    }





    @Override
    public void onClickMenu(int id) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

//        // Ocultar todos los fragmentos activos
        for (Fragment f : fragments) {
            if (f.isAdded() && f.isVisible()) ft.hide(f);
        }

        if (menuFragment.isAdded() && menuFragment.isVisible()) ft.hide(menuFragment);

        if (id == -1) {
            // Mostrar menú lateral
            ft.show(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.GONE);
            }
        } else {
            // Mostrar fragmento seleccionado (incluye id 4 para ConfigFragment)

            // Verifica que el ID esté dentro del límite del array (0 a 4)
            if (id < 0 || id >= fragments.length) {
                // Manejo de error si se presiona un ID fuera de rango
                return;
            }

            Fragment target = fragments[id];
            if (target.isAdded()) {
                ft.show(target);
            } else {
                ft.add(R.id.priRelContenedor, target);
            }
            ft.hide(menuFragment);

//            if (id==4){
//                if (contenedorBotonesInferior != null) {
//                    contenedorBotonesInferior.setVisibility(View.GONE);
//                }
//            }else{
                if(contenedorBotonesInferior != null){
                    contenedorBotonesInferior.setVisibility(View.VISIBLE);
                }
                updateButtonSelection(id);
            }
        ft.commit();
    }
}