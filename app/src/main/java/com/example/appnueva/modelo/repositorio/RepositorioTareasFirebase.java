package com.example.appnueva.modelo.repositorio;

import androidx.annotation.NonNull;

import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.resultado.ResultadoListaTareas;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.example.appnueva.modelo.resultado.ResultadoTarea;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepositorioTareasFirebase {
    private final DatabaseReference referenciaTareas;

    public RepositorioTareasFirebase() {
        // Todas las tareas se guardan dentro del nodo principal "tareas".
        referenciaTareas = FirebaseDatabase.getInstance().getReference("tareas");
    }

    public void obtenerTareas(String usuarioId, ResultadoListaTareas resultado) {
        // Lee todas las tareas del usuario que ha iniciado sesión.
        referenciaTareas.child(usuarioId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datos) {
                List<Tarea> tareas = new ArrayList<>();
                for (DataSnapshot hijo : datos.getChildren()) {
                    Tarea tarea = hijo.getValue(Tarea.class);
                    if (tarea != null) {
                        if (tarea.getId() == null || tarea.getId().isEmpty()) {
                            tarea.setId(hijo.getKey());
                        }
                        tareas.add(tarea);
                    }
                }
                resultado.alExito(tareas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                resultado.alError(errorBaseDatos.getMessage());
            }
        });
    }

    public void obtenerTareaPorId(String usuarioId, String tareaId, ResultadoTarea resultado) {
        // Busca una sola tarea usando el id del usuario y el id de la tarea.
        referenciaTareas.child(usuarioId).child(tareaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datos) {
                Tarea tarea = datos.getValue(Tarea.class);
                if (tarea == null) {
                    resultado.alError("No se ha encontrado la tarea.");
                    return;
                }

                if (tarea.getId() == null || tarea.getId().isEmpty()) {
                    tarea.setId(datos.getKey());
                }
                resultado.alExito(tarea);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError errorBaseDatos) {
                resultado.alError(errorBaseDatos.getMessage());
            }
        });
    }

    public void guardarTarea(String usuarioId, Tarea tarea, ResultadoOperacion resultado) {
        // Sirve tanto para crear como para actualizar una tarea.
        if (tarea.getId() == null || tarea.getId().trim().isEmpty()) {
            tarea.setId(UUID.randomUUID().toString());
        }

        tarea.setUsuarioId(usuarioId);
        referenciaTareas.child(usuarioId).child(tarea.getId())
                .setValue(tarea)
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public void eliminarTarea(String usuarioId, String tareaId, ResultadoOperacion resultado) {
        // Borra la tarea del nodo del usuario en Firebase.
        referenciaTareas.child(usuarioId).child(tareaId)
                .removeValue()
                .addOnSuccessListener(valorNoUsado -> resultado.alExito())
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }
}
