package com.example.appnueva.controlador;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.EstadoTarea;
import com.example.appnueva.modelo.PrioridadTarea;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioTareasFirebase;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.example.appnueva.modelo.resultado.ResultadoTarea;
import com.example.appnueva.modelo.utilidad.GestorRecordatorios;
import com.example.appnueva.modelo.utilidad.UtilidadesFecha;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class CrearTareaActivity extends AppCompatActivity {
    public static final String DATO_ID_TAREA_EDICION = "extra_id_tarea_edicion";

    private EditText campoTitulo;
    private EditText campoDescripcion;
    private EditText campoFecha;
    private Spinner selectorPrioridad;
    private Spinner selectorCategoria;
    private Spinner selectorEstado;
    private SwitchCompat interruptorRecordatorio;
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioTareasFirebase repositorioTareas;
    private FirebaseUser usuarioActual;
    private String idTareaEdicion;
    private Tarea tareaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_tarea);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizCrearTarea), (vista, margen) -> {
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

        campoTitulo = findViewById(R.id.campoTitulo);
        campoDescripcion = findViewById(R.id.campoDescripcion);
        campoFecha = findViewById(R.id.campoFecha);
        selectorPrioridad = findViewById(R.id.selectorPrioridad);
        selectorCategoria = findViewById(R.id.selectorCategoria);
        selectorEstado = findViewById(R.id.selectorEstado);
        interruptorRecordatorio = findViewById(R.id.interruptorRecordatorio);
        Button botonGuardar = findViewById(R.id.botonGuardarTarea);

        selectorPrioridad.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                PrioridadTarea.values()
        ));
        selectorEstado.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                EstadoTarea.values()
        ));
        selectorCategoria.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.categorias_tarea)
        ));

        campoFecha.setOnClickListener(v -> mostrarSelectorFecha());

        idTareaEdicion = getIntent().getStringExtra(DATO_ID_TAREA_EDICION);
        // Si llega un id, esta pantalla reutiliza el formulario para editar una tarea existente.
        if (idTareaEdicion != null && !idTareaEdicion.isEmpty()) {
            setTitle(R.string.titulo_editar_tarea);
            botonGuardar.setText(R.string.actualizar_tarea);
            cargarTarea();
        }

        botonGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void cargarTarea() {
        // Carga la tarea desde Firebase para rellenar el formulario de edición.
        repositorioTareas.obtenerTareaPorId(usuarioActual.getUid(), idTareaEdicion, new ResultadoTarea() {
            @Override
            public void alExito(Tarea tarea) {
                tareaActual = tarea;
                campoTitulo.setText(tarea.getTitulo());
                campoDescripcion.setText(tarea.getDescripcion());
                campoFecha.setText(tarea.getFechaLimite());
                selectorPrioridad.setSelection(tarea.getPrioridad().ordinal());
                selectorEstado.setSelection(tarea.getEstado().ordinal());
                interruptorRecordatorio.setChecked(tarea.isRecordatorioActivo());
                String[] categorias = getResources().getStringArray(R.array.categorias_tarea);
                int indiceCategoria = Arrays.asList(categorias).indexOf(tarea.getCategoriaId());
                selectorCategoria.setSelection(indiceCategoria >= 0 ? indiceCategoria : 0);
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(CrearTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void guardarTarea() {
        String titulo = campoTitulo.getText().toString().trim();
        String descripcion = campoDescripcion.getText().toString().trim();
        String fecha = campoFecha.getText().toString().trim();

        if (titulo.isEmpty()) {
            campoTitulo.setError(getString(R.string.campo_obligatorio));
            return;
        }

        if (fecha.isEmpty()) {
            campoFecha.setError(getString(R.string.campo_obligatorio));
            return;
        }

        // Se monta el objeto Tarea con lo que el usuario ha escrito en pantalla.
        Tarea tarea = tareaActual != null ? tareaActual : new Tarea();
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setFechaLimite(fecha);
        tarea.setPrioridad((PrioridadTarea) selectorPrioridad.getSelectedItem());
        tarea.setEstado((EstadoTarea) selectorEstado.getSelectedItem());
        tarea.setCategoriaId(selectorCategoria.getSelectedItem().toString());
        tarea.setRecordatorioActivo(interruptorRecordatorio.isChecked());
        if (tarea.getFechaCreacion() == null || tarea.getFechaCreacion().isEmpty()) {
            tarea.setFechaCreacion(UtilidadesFecha.hoy());
        }

        // El recordatorio se calcula desde la fecha límite para que el usuario no tenga que configurar más cosas.
        long momentoRecordatorio = UtilidadesFecha.obtenerMomentoRecordatorio(fecha);
        tarea.setFechaRecordatorio(momentoRecordatorio);
        if (interruptorRecordatorio.isChecked() && momentoRecordatorio <= 0) {
            Toast.makeText(this, R.string.recordatorio_no_programado, Toast.LENGTH_SHORT).show();
            return;
        }

        repositorioTareas.guardarTarea(usuarioActual.getUid(), tarea, new ResultadoOperacion() {
            @Override
            public void alExito() {
                // Si el usuario activa el interruptor, se programa la notificación.
                if (tarea.isRecordatorioActivo()) {
                    GestorRecordatorios.programarRecordatorio(CrearTareaActivity.this, tarea);
                } else {
                    GestorRecordatorios.cancelarRecordatorio(CrearTareaActivity.this, tarea.getId());
                }
                Toast.makeText(CrearTareaActivity.this, R.string.tarea_guardada, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(CrearTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirInicioSesion() {
        // Si no hay usuario activo, vuelve a la pantalla de login.
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarSelectorFecha() {
        // Muestra un calendario para que el usuario no tenga que escribir la fecha a mano.
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog dialogo = new DatePickerDialog(
                this,
                (vista, anio, mes, dia) -> campoFecha.setText(
                        String.format(Locale.getDefault(), "%04d-%02d-%02d", anio, mes + 1, dia)
                ),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        dialogo.show();
    }
}
