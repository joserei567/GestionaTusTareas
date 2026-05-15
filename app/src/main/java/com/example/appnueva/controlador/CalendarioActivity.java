package com.example.appnueva.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioTareasFirebase;
import com.example.appnueva.modelo.resultado.ResultadoListaTareas;
import com.example.appnueva.vista.AdaptadorTareas;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioActivity extends AppCompatActivity {
    // Pantalla para ver las tareas según el día elegido.
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioTareasFirebase repositorioTareas;
    private AdaptadorTareas adaptador;
    private TextView textoFechaSeleccionada;
    private TextView textoSinTareasDia;
    private FirebaseUser usuarioActual;
    private String fechaSeleccionada;
    private final List<Tarea> todasLasTareas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizCalendario), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        repositorioTareas = new RepositorioTareasFirebase();
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        if (usuarioActual == null) {
            abrirInicioSesion();
            return;
        }

        CalendarView calendario = findViewById(R.id.calendarioTareas);
        ListView listaTareas = findViewById(R.id.listaTareasCalendario);
        textoFechaSeleccionada = findViewById(R.id.textoFechaSeleccionada);
        textoSinTareasDia = findViewById(R.id.textoSinTareasDia);

        adaptador = new AdaptadorTareas(this, new ArrayList<>());
        listaTareas.setAdapter(adaptador);
        listaTareas.setOnItemClickListener((padre, vista, posicion, id) -> abrirDetalle(adaptador.getItem(posicion)));

        fechaSeleccionada = obtenerFechaActual();
        calendario.setOnDateChangeListener((vista, anio, mes, dia) -> {
            fechaSeleccionada = crearFecha(anio, mes + 1, dia);
            mostrarTareasDelDia();
        });

        cargarTareas();
    }

    private void cargarTareas() {
        // Primero lee todas las tareas y después filtra por fecha.
        repositorioTareas.obtenerTareas(usuarioActual.getUid(), new ResultadoListaTareas() {
            @Override
            public void alExito(List<Tarea> tareas) {
                todasLasTareas.clear();
                todasLasTareas.addAll(tareas);
                mostrarTareasDelDia();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(CalendarioActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarTareasDelDia() {
        // Solo muestra las tareas cuya fecha límite coincide con el calendario.
        List<Tarea> tareasDia = new ArrayList<>();
        for (Tarea tarea : todasLasTareas) {
            if (fechaSeleccionada.equals(tarea.getFechaLimite())) {
                tareasDia.add(tarea);
            }
        }

        adaptador.clear();
        adaptador.addAll(tareasDia);
        adaptador.notifyDataSetChanged();

        textoFechaSeleccionada.setText(getString(R.string.formato_tareas_del_dia, fechaSeleccionada));
        textoSinTareasDia.setVisibility(tareasDia.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void abrirDetalle(Tarea tarea) {
        if (tarea == null) {
            return;
        }

        Intent intent = new Intent(this, DetalleTareaActivity.class);
        intent.putExtra(DetalleTareaActivity.DATO_ID_TAREA, tarea.getId());
        startActivity(intent);
    }

    private String obtenerFechaActual() {
        Calendar calendario = Calendar.getInstance();
        return crearFecha(
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH) + 1,
                calendario.get(Calendar.DAY_OF_MONTH)
        );
    }

    private String crearFecha(int anio, int mes, int dia) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", anio, mes, dia);
    }

    private void abrirInicioSesion() {
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
