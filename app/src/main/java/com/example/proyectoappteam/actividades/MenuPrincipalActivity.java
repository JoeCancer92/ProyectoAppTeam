package com.example.proyectoappteam.actividades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.proyectoappteam.ProyectoAppTeam;
import com.example.proyectoappteam.R;
import com.example.proyectoappteam.clases.LocaleHelper;
import com.example.proyectoappteam.clases.Menu;
import com.example.proyectoappteam.clases.Usuario;
import com.example.proyectoappteam.fragmentos.ConfigFragment;
import com.example.proyectoappteam.fragmentos.InicioFragment;
import com.example.proyectoappteam.fragmentos.MenuFragment;
import com.example.proyectoappteam.fragmentos.NotificacionFragment;
import com.example.proyectoappteam.fragmentos.PerfilFragment;
import com.example.proyectoappteam.fragmentos.PublicarFragment;

public class MenuPrincipalActivity extends AppCompatActivity implements Menu {

    private Fragment[] fragments;
    private MenuFragment menuFragment;
    private LinearLayout contenedorBotonesInferior;
    private ImageButton lastSelectedButton = null;
    public static Usuario usuarioActivo;

    private ImageView notificacionBadge;
    private BroadcastReceiver notificacionReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, "es"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("AppConfigPrefs", MODE_PRIVATE);
        int tema = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(tema);

        setContentView(R.layout.activity_menu_principal);

        Object recibido = getIntent().getSerializableExtra("usuario");
        if (recibido instanceof Usuario) {
            usuarioActivo = (Usuario) recibido;
            // CORRECCIÓN: Se llama al método con el nombre correcto
            ((ProyectoAppTeam) getApplication()).iniciarListenersEnTiempoReal();
        } else {
            usuarioActivo = null;
        }

        contenedorBotonesInferior = findViewById(R.id.contenedorBotonesInferior);
        notificacionBadge = findViewById(R.id.badge_notificacion);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        menuFragment = new MenuFragment();
        ft.add(R.id.priRelContenedor, menuFragment, "menu");

        fragments = new Fragment[]{
                new PerfilFragment(),
                new InicioFragment(),
                new PublicarFragment(),
                new NotificacionFragment(),
                new ConfigFragment()
        };

        Fragment inicioFragment = fragments[1];
        ft.add(R.id.priRelContenedor, inicioFragment);
        ft.hide(menuFragment);
        ft.commit();

        updateButtonSelection(1);

        configurarBotonLateral(R.id.lateralBtnMiPerfil, 0);
        configurarBotonLateral(R.id.lateralBtnInicio, 1);
        configurarBotonLateral(R.id.lateralBtnPublicar, 2);
        configurarBotonLateral(R.id.lateralBtnNotificaciones, 3);
        configurarBotonLateral(R.id.lateralBtnConfig, 4);

        configurarReceptorDeNotificaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el receptor para que la Activity pueda recibir la señal
        LocalBroadcastManager.getInstance(this).registerReceiver(notificacionReceiver, new IntentFilter(ProyectoAppTeam.ACTION_NUEVA_NOTIFICACION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Anular el registro para no recibir señales si la Activity no está visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificacionReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CORRECCIÓN: Se llama al método con el nombre correcto
        ((ProyectoAppTeam) getApplication()).detenerListenersEnTiempoReal();
    }

    private void configurarReceptorDeNotificaciones() {
        notificacionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ProyectoAppTeam.ACTION_NUEVA_NOTIFICACION.equals(intent.getAction())) {
                    mostrarAlertaNotificacion();
                }
            }
        };
    }

    private void mostrarAlertaNotificacion() {
        if (notificacionBadge != null) {
            notificacionBadge.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarAlertaNotificacion() {
        if (notificacionBadge != null) {
            notificacionBadge.setVisibility(View.GONE);
        }
    }

    private void configurarBotonLateral(int idBoton, int idFragmento) {
        ImageButton boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> onClickMenu(idFragmento));
        }
    }

    private ImageButton getButtonFromId(int idBoton) {
        return findViewById(idBoton);
    }

    private void updateButtonSelection(int idFragmento) {
        int buttonIds[] = new int[]{
                R.id.lateralBtnMiPerfil,
                R.id.lateralBtnInicio,
                R.id.lateralBtnPublicar,
                R.id.lateralBtnNotificaciones,
                R.id.lateralBtnConfig
        };
        if (idFragmento >= 0 && idFragmento < buttonIds.length) {
            ImageButton currentButton = getButtonFromId(buttonIds[idFragmento]);
            if (currentButton != null) {
                if (lastSelectedButton != null) {
                    lastSelectedButton.setSelected(false);
                }
                currentButton.setSelected(true);
                lastSelectedButton = currentButton;
            }
        }
    }

    @Override
    public void onClickMenu(int id) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        for (Fragment f : fragments) {
            if (f.isAdded() && f.isVisible()) ft.hide(f);
        }

        if (menuFragment.isAdded() && menuFragment.isVisible()) ft.hide(menuFragment);

        if (id == -1) {
            ft.show(menuFragment);
            if (contenedorBotonesInferior != null) {
                contenedorBotonesInferior.setVisibility(View.GONE);
            }
        } else {
            if (id < 0 || id >= fragments.length) {
                return;
            }

            // Si se hace clic en notificaciones (id=3), ocultar el badge
            if (id == 3) {
                ocultarAlertaNotificacion();
            }

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
            updateButtonSelection(id);
        }
        ft.commit();
    }
}
