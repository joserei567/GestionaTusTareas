package com.example.appnueva.modelo.resultado;

import com.example.appnueva.modelo.Tarea;

public interface ResultadoTarea {
    void alExito(Tarea tarea);

    void alError(String mensaje);
}
