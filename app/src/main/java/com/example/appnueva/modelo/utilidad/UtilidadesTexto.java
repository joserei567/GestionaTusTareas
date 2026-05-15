package com.example.appnueva.modelo.utilidad;

import com.example.appnueva.modelo.EstadoTarea;
import com.example.appnueva.modelo.PrioridadTarea;

public final class UtilidadesTexto {
    private UtilidadesTexto() {
    }

    public static String mostrarPrioridad(PrioridadTarea prioridad) {
        if (prioridad == null) {
            return "";
        }
        switch (prioridad) {
            case ALTA:
                return "Alta";
            case MEDIA:
                return "Media";
            case BAJA:
            default:
                return "Baja";
        }
    }

    public static String mostrarEstado(EstadoTarea estado) {
        if (estado == null) {
            return "";
        }
        switch (estado) {
            case COMPLETADA:
                return "Completada";
            case EN_PROGRESO:
                return "En progreso";
            case PENDIENTE:
            default:
                return "Pendiente";
        }
    }

    public static int pesoEstado(EstadoTarea estado) {
        if (estado == null) {
            return 99;
        }
        switch (estado) {
            case PENDIENTE:
                return 0;
            case EN_PROGRESO:
                return 1;
            case COMPLETADA:
            default:
                return 2;
        }
    }
}
