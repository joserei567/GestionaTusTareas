package com.example.appnueva.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.EstadoTarea;
import com.example.appnueva.modelo.PeticionColaboracion;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioColaboracionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioTareasFirebase;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.example.appnueva.modelo.resultado.ResultadoTarea;
import com.example.appnueva.modelo.utilidad.GestorRecordatorios;
import com.example.appnueva.modelo.utilidad.UtilidadesTexto;
import com.google.firebase.auth.FirebaseUser;

public class DetalleTareaActivity extends AppCompatActivity {
    public static final String DATO_ID_TAREA = "extra_id_tarea";

    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioTareasFirebase repositorioTareas;
    private RepositorioColaboracionFirebase repositorioColaboracion;
    private Tarea tarea;
    private FirebaseUser usuarioActual;
    private EditText campoCorreoColaborador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_tarea);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizDetalle), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        repositorioTareas = new RepositorioTareasFirebase();
        repositorioColaboracion = new RepositorioColaboracionFirebase();
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        if (usuarioActual == null) {
            abrirInicioSesion();
            return;
        }

        String idTarea = getIntent().getStringExtra(DATO_ID_TAREA);
        if (idTarea == null || idTarea.isEmpty()) {
            Toast.makeText(this, R.string.tarea_no_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button botonCompletar = findViewById(R.id.botonCompletarTarea);
        Button botonEliminar = findViewById(R.id.botonEliminarTarea);
        Button botonEditar = findViewById(R.id.botonEditarTarea);
        Button botonEnviarPeticion = findViewById(R.id.botonEnviarPeticion);
        Button botonAbrirChat = findViewById(R.id.botonAbrirChat);
        campoCorreoColaborador = findViewById(R.id.campoCorreoColaborador);

        // Desde el detalle se puede completar, editar o borrar la tarea.
        botonCompletar.setOnClickListener(v -> marcarComoCompletada());
        botonEliminar.setOnClickListener(v -> confirmarEliminarTarea());
        botonEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearTareaActivity.class);
            intent.putExtra(CrearTareaActivity.DATO_ID_TAREA_EDICION, idTarea);
            startActivity(intent);
        });
        botonEnviarPeticion.setOnClickListener(v -> enviarPeticionColaboracion());
        botonAbrirChat.setOnClickListener(v -> abrirChat());

        cargarTarea(idTarea);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usuarioActual != null) {
            String idTarea = getIntent().getStringExtra(DATO_ID_TAREA);
            if (idTarea != null && !idTarea.isEmpty()) {
                cargarTarea(idTarea);
            }
        }
    }

    private void cargarTarea(String idTarea) {
        // Vuelve a leer la tarea desde Firebase para mostrar siempre los datos actualizados.
        repositorioTareas.obtenerTareaPorId(usuarioActual.getUid(), idTarea, new ResultadoTarea() {
            @Override
            public void alExito(Tarea tareaCargada) {
                tarea = tareaCargada;
                mostrarTarea();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void mostrarTarea() {
        // Pinta en pantalla todos los datos importantes de la tarea.
        ((TextView) findViewById(R.id.textoTituloDetalle)).setText(tarea.getTitulo());
        ((TextView) findViewById(R.id.textoDescripcionDetalle)).setText(tarea.getDescripcion());
        ((TextView) findViewById(R.id.textoPrioridadDetalle)).setText(UtilidadesTexto.mostrarPrioridad(tarea.getPrioridad()));
        ((TextView) findViewById(R.id.textoCategoriaDetalle)).setText(tarea.getCategoriaId());
        ((TextView) findViewById(R.id.textoEstadoDetalle)).setText(UtilidadesTexto.mostrarEstado(tarea.getEstado()));
        ((TextView) findViewById(R.id.textoFechaDetalle)).setText(tarea.getFechaLimite());
        ((TextView) findViewById(R.id.textoRecordatorioDetalle)).setText(
                tarea.isRecordatorioActivo() ? getString(R.string.recordatorio_activado) : getString(R.string.recordatorio_desactivado)
        );

        TextView textoCompartida = findViewById(R.id.textoDetalleCompartida);
        textoCompartida.setVisibility(tarea.estaCompartida() ? View.VISIBLE : View.GONE);

        TextView textoCompartidaCon = findViewById(R.id.textoCompartidaCon);
        if (tarea.getColaboradorCorreo() != null && !tarea.getColaboradorCorreo().isEmpty()) {
            textoCompartidaCon.setVisibility(View.VISIBLE);
            textoCompartidaCon.setText(getString(R.string.compartida_con, tarea.getColaboradorCorreo()));
        } else {
            textoCompartidaCon.setVisibility(View.GONE);
        }
    }

    private void marcarComoCompletada() {
        if (tarea == null) {
            return;
        }
        tarea.setEstado(EstadoTarea.COMPLETADA);
        // Cuando una tarea ya está hecha, el recordatorio deja de tener sentido.
        tarea.setRecordatorioActivo(false);
        repositorioTareas.guardarTarea(usuarioActual.getUid(), tarea, new ResultadoOperacion() {
            @Override
            public void alExito() {
                GestorRecordatorios.cancelarRecordatorio(DetalleTareaActivity.this, tarea.getId());
                mostrarTarea();
                Toast.makeText(DetalleTareaActivity.this, R.string.tarea_completada, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void eliminarTarea() {
        if (tarea == null) {
            return;
        }
        // Al borrar la tarea también se borra su recordatorio.
        repositorioTareas.eliminarTarea(usuarioActual.getUid(), tarea.getId(), new ResultadoOperacion() {
            @Override
            public void alExito() {
                GestorRecordatorios.cancelarRecordatorio(DetalleTareaActivity.this, tarea.getId());
                Toast.makeText(DetalleTareaActivity.this, R.string.tarea_eliminada, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarEliminarTarea() {
        if (tarea == null) {
            return;
        }

        // Evita borrar una tarea por accidente.
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(R.string.confirmar_eliminar_mensaje)
                .setPositiveButton(R.string.eliminar_tarea, (dialogo, cual) -> eliminarTarea())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void enviarPeticionColaboracion() {
        if (tarea == null) {
            return;
        }

        String correoColaborador = campoCorreoColaborador.getText().toString().trim();
        if (correoColaborador.isEmpty()) {
            campoCorreoColaborador.setError(getString(R.string.correo_colaborador_obligatorio));
            return;
        }

        String chatId = asegurarChatId();
        tarea.setColaboradorCorreo(correoColaborador);

        // Primero se guarda la tarea con el chat para que el otro usuario pueda compartirlo.
        repositorioTareas.guardarTarea(usuarioActual.getUid(), tarea, new ResultadoOperacion() {
            @Override
            public void alExito() {
                crearPeticion(correoColaborador, chatId);
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void crearPeticion(String correoColaborador, String chatId) {
        PeticionColaboracion peticion = new PeticionColaboracion();
        peticion.setTareaId(tarea.getId());
        peticion.setTituloTarea(tarea.getTitulo());
        peticion.setPropietarioId(usuarioActual.getUid());
        peticion.setPropietarioCorreo(usuarioActual.getEmail());
        peticion.setColaboradorCorreo(correoColaborador);
        peticion.setChatId(chatId);
        peticion.setEstado("PENDIENTE");

        repositorioColaboracion.enviarPeticion(peticion, new ResultadoOperacion() {
            @Override
            public void alExito() {
                Toast.makeText(DetalleTareaActivity.this, R.string.peticion_enviada, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirChat() {
        if (tarea == null) {
            return;
        }

        asegurarChatId();
        repositorioTareas.guardarTarea(usuarioActual.getUid(), tarea, new ResultadoOperacion() {
            @Override
            public void alExito() {
                Intent intent = new Intent(DetalleTareaActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.DATO_ID_CHAT, tarea.getChatId());
                intent.putExtra(ChatActivity.DATO_TITULO_TAREA, tarea.getTitulo());
                startActivity(intent);
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(DetalleTareaActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String asegurarChatId() {
        if (tarea.getChatId() == null || tarea.getChatId().isEmpty()) {
            tarea.setChatId(usuarioActual.getUid() + "_" + tarea.getId());
        }
        return tarea.getChatId();
    }

    private void abrirInicioSesion() {
        // Si se pierde la sesión, se redirige al usuario al login.
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
