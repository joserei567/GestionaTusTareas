package com.example.appnueva.modelo.resultado;

import com.example.appnueva.modelo.Tarea;

import java.util.List;

public interface ResultadoListaTareas {
    void alExito(List<Tarea> tareas);

    void alError(String mensaje);
}
