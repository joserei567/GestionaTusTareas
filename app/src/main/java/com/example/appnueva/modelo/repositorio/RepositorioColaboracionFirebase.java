package com.example.appnueva.modelo.repositorio;

import androidx.annotation.NonNull;

import com.example.appnueva.modelo.MensajeChat;
import com.example.appnueva.modelo.PeticionColaboracion;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.resultado.ResultadoListaMensajes;
import com.example.appnueva.modelo.resultado.ResultadoListaPeticiones;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RepositorioColaboracionFirebase {
    private final DatabaseReference referenciaPeticiones;
    private final DatabaseReference referenciaTareas;
    private final DatabaseReference referenciaChats;

    public RepositorioColaboracionFirebase() {
        FirebaseDatabase baseDatos = FirebaseDatabase.getInstance();
        referenciaPeticiones = baseDatos.getReference("peticiones");
        referenciaTareas = baseDatos.getReference("tareas");
        referenciaChats = baseDatos.getReference("chats");
    }

    public void enviarPeticion(PeticionColaboracion peticion, ResultadoOperacion resultado) {
        // Guarda la invitación dentro del correo del colaborador.
        if (peticion.getId() == null || peticion.getId().isEmpty()) {
            peticion.setId(UUID.randomUUID().toString());
        }

        referenciaPeticiones.child(claveCorreo(peticion.getColaboradorCorreo())).child(peticion.getId())
                .setValue(peticion)
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public void obtenerPeticiones(String correo, ResultadoListaPeticiones resultado) {
        // Lee solo las peticiones pendientes del usuario actual.
        referenciaPeticiones.child(claveCorreo(correo)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datos) {
                List<PeticionColaboracion> peticiones = new ArrayList<>();
                for (DataSnapshot hijo : datos.getChildren()) {
                    PeticionColaboracion peticion = hijo.getValue(PeticionColaboracion.class);
                    if (peticion != null && "PENDIENTE".equals(peticion.getEstado())) {
                        peticiones.add(peticion);
                    }
                }
                resultado.alExito(peticiones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                resultado.alError(errorBaseDatos.getMessage());
            }
        });
    }

    public void aceptarPeticion(PeticionColaboracion peticion, String usuarioId, ResultadoOperacion resultado) {
        // Copia la tarea del propietario en la cuenta del colaborador.
        referenciaTareas.child(peticion.getPropietarioId()).child(peticion.getTareaId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datos) {
                        Tarea tarea = datos.getValue(Tarea.class);
                        if (tarea == null) {
                            resultado.alError("No se ha encontrado la tarea original.");
                            return;
                        }

                        tarea.setUsuarioId(usuarioId);
                        tarea.setChatId(peticion.getChatId());
                        tarea.setColaboradorCorreo(peticion.getPropietarioCorreo());

                        referenciaTareas.child(usuarioId).child(tarea.getId())
                                .setValue(tarea)
                                .addOnSuccessListener(valorNoUsado -> marcarPeticionAceptada(peticion, resultado))
                                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                        resultado.alError(errorBaseDatos.getMessage());
                    }
                });
    }

    public void denegarPeticion(PeticionColaboracion peticion, ResultadoOperacion resultado) {
        // Se marca como denegada para que deje de salir en la lista de pendientes.
        peticion.setEstado("DENEGADA");
        referenciaPeticiones.child(claveCorreo(peticion.getColaboradorCorreo())).child(peticion.getId())
                .setValue(peticion)
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public void enviarMensaje(String chatId, MensajeChat mensaje, ResultadoOperacion resultado) {
        // Cada mensaje se guarda dentro del chat compartido.
        if (mensaje.getId() == null || mensaje.getId().isEmpty()) {
            mensaje.setId(UUID.randomUUID().toString());
        }

        referenciaChats.child(chatId).child("mensajes").child(mensaje.getId())
                .setValue(mensaje)
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public void obtenerMensajes(String chatId, ResultadoListaMensajes resultado) {
        // Lectura simple de mensajes. Se mantiene por si alguna pantalla necesita cargar una vez.
        referenciaChats.child(chatId).child("mensajes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datos) {
                List<MensajeChat> mensajes = new ArrayList<>();
                for (DataSnapshot hijo : datos.getChildren()) {
                    MensajeChat mensaje = hijo.getValue(MensajeChat.class);
                    if (mensaje != null) {
                        mensajes.add(mensaje);
                    }
                }
                mensajes.sort(Comparator.comparingLong(MensajeChat::getFechaEnvio));
                resultado.alExito(mensajes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                resultado.alError(errorBaseDatos.getMessage());
            }
        });
    }

    public ValueEventListener escucharMensajes(String chatId, ResultadoListaMensajes resultado) {
        // Este listener se queda escuchando para que el chat se actualice al instante.
        DatabaseReference referenciaMensajes = referenciaChats.child(chatId).child("mensajes");
        ValueEventListener escuchador = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datos) {
                List<MensajeChat> mensajes = new ArrayList<>();
                for (DataSnapshot hijo : datos.getChildren()) {
                    MensajeChat mensaje = hijo.getValue(MensajeChat.class);
                    if (mensaje != null) {
                        mensajes.add(mensaje);
                    }
                }
                mensajes.sort(Comparator.comparingLong(MensajeChat::getFechaEnvio));
                resultado.alExito(mensajes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                resultado.alError(errorBaseDatos.getMessage());
            }
        };

        referenciaMensajes.addValueEventListener(escuchador);
        return escuchador;
    }

    public void dejarDeEscucharMensajes(String chatId, ValueEventListener escuchador) {
        if (chatId != null && escuchador != null) {
            referenciaChats.child(chatId).child("mensajes").removeEventListener(escuchador);
        }
    }

    private void marcarPeticionAceptada(PeticionColaboracion peticion, ResultadoOperacion resultado) {
        // Al aceptar, la petición deja de aparecer como pendiente.
        peticion.setEstado("ACEPTADA");
        referenciaPeticiones.child(claveCorreo(peticion.getColaboradorCorreo())).child(peticion.getId())
                .setValue(peticion)
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    private String claveCorreo(String correo) {
        if (correo == null) {
            return "";
        }

        // Firebase no deja usar algunos caracteres en el nombre de un nodo.
        return correo.trim().toLowerCase(Locale.ROOT)
                .replace(".", "_")
                .replace("@", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_");
    }
}
