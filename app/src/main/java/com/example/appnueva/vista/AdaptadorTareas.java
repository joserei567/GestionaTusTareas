package com.example.appnueva.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appnueva.R;
import com.example.appnueva.modelo.EstadoTarea;
import com.example.appnueva.modelo.Tarea;
import com.example.appnueva.modelo.utilidad.UtilidadesTexto;

import java.util.List;

public class AdaptadorTareas extends ArrayAdapter<Tarea> {
    // Adaptador que decide como se ve cada tarea dentro de las listas.
    public AdaptadorTareas(@NonNull Context context, @NonNull List<Tarea> tareas) {
        super(context, 0, tareas);
    }

    @NonNull
    @Override
    public View getView(int posicion, @Nullable View vistaReciclada, @NonNull ViewGroup grupoPadre) {
        View vista = vistaReciclada;
        if (vista == null) {
            vista = LayoutInflater.from(getContext()).inflate(R.layout.item_tarea, grupoPadre, false);
        }

        Tarea tarea = getItem(posicion);
        if (tarea != null) {
            TextView cabeceraCategoria = vista.findViewById(R.id.textoCabeceraCategoria);
            TextView titulo = vista.findViewById(R.id.textoTituloTarea);
            TextView compartida = vista.findViewById(R.id.textoTareaCompartida);
            TextView datos = vista.findViewById(R.id.textoMetaTarea);
            TextView estado = vista.findViewById(R.id.textoEstadoTarea);

            String categoriaActual = tarea.getCategoriaId();
            String categoriaAnterior = null;
            if (posicion > 0) {
                Tarea tareaAnterior = getItem(posicion - 1);
                if (tareaAnterior != null) {
                    categoriaAnterior = tareaAnterior.getCategoriaId();
                }
            }

            if (posicion == 0 || (categoriaActual != null && !categoriaActual.equalsIgnoreCase(categoriaAnterior))) {
                // Muestra un título de categoría cuando empieza un grupo nuevo.
                cabeceraCategoria.setVisibility(View.VISIBLE);
                cabeceraCategoria.setText(categoriaActual);
            } else {
                cabeceraCategoria.setVisibility(View.GONE);
            }

            titulo.setText(tarea.getTitulo());
            compartida.setVisibility(tarea.estaCompartida() ? View.VISIBLE : View.GONE);
            datos.setText(getContext().getString(
                    R.string.formato_meta_tarea,
                    UtilidadesTexto.mostrarPrioridad(tarea.getPrioridad()),
                    tarea.getFechaLimite(),
                    tarea.getCategoriaId()
            ));
            estado.setText(UtilidadesTexto.mostrarEstado(tarea.getEstado()));
            aplicarColorEstado(estado, tarea.getEstado());
        }

        return vista;
    }

    private void aplicarColorEstado(TextView textoEstado, EstadoTarea estado) {
        // Da un color distinto al estado para que la lista se lea más rápido.
        if (estado == EstadoTarea.COMPLETADA) {
            textoEstado.setBackgroundResource(R.drawable.fondo_estado_completada);
            textoEstado.setTextColor(getContext().getColor(R.color.verde_oscuro));
            return;
        }

        if (estado == EstadoTarea.EN_PROGRESO) {
            textoEstado.setBackgroundResource(R.drawable.fondo_estado_progreso);
            textoEstado.setTextColor(getContext().getColor(R.color.naranja_apoyo));
            return;
        }

        textoEstado.setBackgroundResource(R.drawable.fondo_estado_pendiente);
        textoEstado.setTextColor(getContext().getColor(R.color.azul_oscuro));
    }
}
