package com.example.appnueva.controlador;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.MensajeChat;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioColaboracionFirebase;
import com.example.appnueva.modelo.resultado.ResultadoListaMensajes;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {
    // Pantalla del chat que pertenece a una tarea compartida.
    public static final String DATO_ID_CHAT = "extra_id_chat";
    public static final String DATO_TITULO_TAREA = "extra_titulo_tarea";
    private static final String ID_CANAL_CHAT = "mensajes_chat";

    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioColaboracionFirebase repositorioColaboracion;
    private FirebaseUser usuarioActual;
    private String chatId;
    private EditText campoMensaje;
    private TextView textoSinMensajes;
    private ArrayAdapter<String> adaptador;
    private ListView listaMensajes;
    private ValueEventListener escuchadorMensajes;
    private final Set<String> idsMensajesVistos = new HashSet<>();
    private boolean primeraCargaMensajes = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizChat), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        repositorioColaboracion = new RepositorioColaboracionFirebase();
        crearCanalChat();
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        if (usuarioActual == null) {
            abrirInicioSesion();
            return;
        }

        chatId = getIntent().getStringExtra(DATO_ID_CHAT);
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, R.string.chat_no_disponible, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView textoTituloChat = findViewById(R.id.textoTituloChat);
        textoSinMensajes = findViewById(R.id.textoSinMensajes);
        campoMensaje = findViewById(R.id.campoMensaje);
        Button botonEnviarMensaje = findViewById(R.id.botonEnviarMensaje);
        listaMensajes = findViewById(R.id.listaMensajes);

        String tituloTarea = getIntent().getStringExtra(DATO_TITULO_TAREA);
        textoTituloChat.setText(tituloTarea == null ? getString(R.string.titulo_chat) : tituloTarea);

        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listaMensajes.setAdapter(adaptador);
        botonEnviarMensaje.setOnClickListener(v -> enviarMensaje());
        escucharMensajes();
    }

    private void escucharMensajes() {
        // Mantiene el chat conectado a Firebase mientras la pantalla está abierta.
        escuchadorMensajes = repositorioColaboracion.escucharMensajes(chatId, new ResultadoListaMensajes() {
            @Override
            public void alExito(List<MensajeChat> mensajes) {
                avisarMensajesNuevos(mensajes);
                adaptador.clear();
                for (MensajeChat mensaje : mensajes) {
                    adaptador.add(mensaje.getCorreoAutor() + ": " + mensaje.getTexto());
                }
                adaptador.notifyDataSetChanged();
                textoSinMensajes.setText(mensajes.isEmpty() ? getString(R.string.sin_mensajes) : "");
                if (!mensajes.isEmpty()) {
                    listaMensajes.post(() -> listaMensajes.setSelection(adaptador.getCount() - 1));
                }
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(ChatActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarMensaje() {
        String texto = campoMensaje.getText().toString().trim();
        if (texto.isEmpty()) {
            campoMensaje.setError(getString(R.string.campo_obligatorio));
            return;
        }

        MensajeChat mensaje = new MensajeChat();
        mensaje.setCorreoAutor(usuarioActual.getEmail());
        mensaje.setTexto(texto);
        mensaje.setFechaEnvio(System.currentTimeMillis());

        // El mensaje se guarda dentro del chat compartido de esa tarea.
        repositorioColaboracion.enviarMensaje(chatId, mensaje, new ResultadoOperacion() {
            @Override
            public void alExito() {
                campoMensaje.setText("");
            }

            @Override
            public void alError(String mensajeError) {
                Toast.makeText(ChatActivity.this, mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void avisarMensajesNuevos(List<MensajeChat> mensajes) {
        for (MensajeChat mensaje : mensajes) {
            if (mensaje.getId() == null) {
                continue;
            }

            boolean mensajeNuevo = idsMensajesVistos.add(mensaje.getId());
            if (!primeraCargaMensajes && mensajeNuevo && esMensajeDeOtraPersona(mensaje)) {
                mostrarNotificacionMensaje(mensaje);
            }
        }
        primeraCargaMensajes = false;
    }

    private boolean esMensajeDeOtraPersona(MensajeChat mensaje) {
        String correoActual = usuarioActual.getEmail();
        return correoActual != null
                && mensaje.getCorreoAutor() != null
                && !correoActual.equalsIgnoreCase(mensaje.getCorreoAutor());
    }

    private void crearCanalChat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel canal = new NotificationChannel(
                ID_CANAL_CHAT,
                getString(R.string.canal_chat_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        canal.setDescription(getString(R.string.canal_chat_descripcion));

        NotificationManager gestorNotificaciones = getSystemService(NotificationManager.class);
        if (gestorNotificaciones != null) {
            gestorNotificaciones.createNotificationChannel(canal);
        }
    }

    private void mostrarNotificacionMensaje(MensajeChat mensaje) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder aviso = new NotificationCompat.Builder(this, ID_CANAL_CHAT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notificacion_chat_titulo, mensaje.getCorreoAutor()))
                .setContentText(mensaje.getTexto())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager gestorNotificaciones =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (gestorNotificaciones != null) {
            gestorNotificaciones.notify(mensaje.getId().hashCode(), aviso.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repositorioColaboracion.dejarDeEscucharMensajes(chatId, escuchadorMensajes);
    }

    private void abrirInicioSesion() {
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
