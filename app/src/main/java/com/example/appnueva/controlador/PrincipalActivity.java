package com.example.appnueva.controlador;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.EstadoTarea;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioTareasFirebase;
import com.example.appnueva.modelo.resultado.ResultadoListaTareas;
import com.example.appnueva.modelo.utilidad.UtilidadesTexto;
import com.example.appnueva.vista.AdaptadorTareas;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioTareasFirebase repositorioTareas;
    private AdaptadorTareas adaptador;
    private TextView textoVacio;
    private TextView textoSubtitulo;
    private TextView textoTotalTareas;
    private TextView textoPendientesTareas;
    private TextView textoCompletadasTareas;
    private Spinner selectorOrden;
    private FirebaseUser usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizPrincipal), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        repositorioTareas = new RepositorioTareasFirebase();
        // Se pide permiso solo en Android 13+ para que las notificaciones puedan mostrarse.
        solicitarPermisoNotificacionesSiHaceFalta();
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        if (usuarioActual == null) {
            abrirInicioSesion();
            return;
        }

        textoVacio = findViewById(R.id.textoVacio);
        textoSubtitulo = findViewById(R.id.textoSubtituloInicio);
        textoTotalTareas = findViewById(R.id.textoTotalTareas);
        textoPendientesTareas = findViewById(R.id.textoPendientesTareas);
        textoCompletadasTareas = findViewById(R.id.textoCompletadasTareas);
        selectorOrden = findViewById(R.id.selectorOrden);

        ListView listaTareas = findViewById(R.id.listaTareas);
        Button botonAnadir = findViewById(R.id.botonAnadirTarea);
        Button botonCalendario = findViewById(R.id.botonCalendario);
        Button botonPeticiones = findViewById(R.id.botonPeticiones);
        Button botonCerrarSesion = findViewById(R.id.botonCerrarSesion);

        // La pantalla principal carga la lista y deja entrar al detalle de cada tarea.
        textoSubtitulo.setText(getString(R.string.subtitulo_usuario, usuarioActual.getEmail()));
        adaptador = new AdaptadorTareas(this, new ArrayList<>());
        listaTareas.setAdapter(adaptador);
        selectorOrden.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.orden_tareas)
        ));
        selectorOrden.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> padre, android.view.View vista, int posicion, long id) {
                recargarTareas();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> padre) {
            }
        });

        listaTareas.setOnItemClickListener((padre, vista, posicion, id) -> {
            Tarea tarea = adaptador.getItem(posicion);
            if (tarea == null) {
                return;
            }

            Intent intent = new Intent(this, DetalleTareaActivity.class);
            intent.putExtra(DetalleTareaActivity.DATO_ID_TAREA, tarea.getId());
            startActivity(intent);
        });

        botonAnadir.setOnClickListener(v -> {
            if (!haySesionIniciada()) {
                abrirInicioSesion();
                return;
            }
            Intent intent = new Intent(this, CrearTareaActivity.class);
            startActivity(intent);
        });

        botonCalendario.setOnClickListener(v -> {
            if (!haySesionIniciada()) {
                abrirInicioSesion();
                return;
            }
            startActivity(new Intent(this, CalendarioActivity.class));
        });

        botonPeticiones.setOnClickListener(v -> {
            if (!haySesionIniciada()) {
                abrirInicioSesion();
                return;
            }
            startActivity(new Intent(this, PeticionesActivity.class));
        });

        botonCerrarSesion.setOnClickListener(v -> {
            repositorioAutenticacion.cerrarSesion();
            abrirInicioSesion();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarTareas();
    }

    private void recargarTareas() {
        if (!haySesionIniciada()) {
            abrirInicioSesion();
            return;
        }

        repositorioTareas.obtenerTareas(usuarioActual.getUid(), new ResultadoListaTareas() {
            @Override
            public void alExito(List<Tarea> tareas) {
                actualizarResumen(tareas);
                ordenarTareas(tareas);
                adaptador.clear();
                adaptador.addAll(tareas);
                adaptador.notifyDataSetChanged();
                textoVacio.setText(tareas.isEmpty() ? getString(R.string.sin_tareas) : "");
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(PrincipalActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarResumen(List<Tarea> tareas) {
        int total = tareas.size();
        int completadas = 0;

        for (Tarea tarea : tareas) {
            if (tarea.getEstado() == EstadoTarea.COMPLETADA) {
                completadas++;
            }
        }

        int pendientes = total - completadas;
        textoTotalTareas.setText(String.valueOf(total));
        textoPendientesTareas.setText(String.valueOf(pendientes));
        textoCompletadasTareas.setText(String.valueOf(completadas));
    }

    private boolean haySesionIniciada() {
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        return usuarioActual != null;
    }

    private void ordenarTareas(List<Tarea> tareas) {
        // Este método cambia el orden visible según la opción elegida por el usuario.
        if (selectorOrden == null || tareas == null) {
            return;
        }

        String criterio = selectorOrden.getSelectedItem() != null
                ? selectorOrden.getSelectedItem().toString()
                : getString(R.string.orden_fecha);

        if (criterio.equals(getString(R.string.orden_estado))) {
            Collections.sort(tareas, Comparator.comparingInt(tarea -> UtilidadesTexto.pesoEstado(tarea.getEstado())));
            return;
        }

        if (criterio.equals(getString(R.string.orden_categoria))) {
            Collections.sort(tareas, Comparator.comparing(Tarea::getCategoriaId, Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(Tarea::getFechaLimite, Comparator.nullsLast(String::compareTo)));
            return;
        }

        Collections.sort(tareas, Comparator.comparing(Tarea::getFechaLimite, Comparator.nullsLast(String::compareTo)));
    }

    private void abrirInicioSesion() {
        // Limpia la navegación y vuelve al login.
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void solicitarPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
    }
}
