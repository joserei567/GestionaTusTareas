package com.example.appnueva.modelo.utilidad;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.appnueva.controlador.ReceptorRecordatorio;
import com.example.appnueva.modelo.Tarea;

public final class GestorRecordatorios {
    public static final String ID_CANAL = "recordatorios_tareas";

    private GestorRecordatorios() {
    }

    public static void programarRecordatorio(Context contexto, Tarea tarea) {
        // Programa la alarma del sistema para lanzar la notificacion de la tarea.
        if (tarea.getId() == null || !tarea.isRecordatorioActivo() || tarea.getFechaRecordatorio() <= 0) {
            return;
        }

        crearCanal(contexto);
        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
        if (gestorAlarmas == null) {
            return;
        }

        PendingIntent aviso = crearAvisoPendiente(contexto, tarea);
        gestorAlarmas.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tarea.getFechaRecordatorio(),
                aviso
        );
    }

    public static void cancelarRecordatorio(Context contexto, String tareaId) {
        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
        if (gestorAlarmas == null || tareaId == null) {
            return;
        }

        PendingIntent aviso = crearAvisoPendiente(contexto, tareaId, "", "");
        gestorAlarmas.cancel(aviso);
        aviso.cancel();
    }

    public static void crearCanal(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel canal = new NotificationChannel(
                ID_CANAL,
                "Recordatorios de tareas",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        canal.setDescription("Notificaciones para avisar de tareas proximas");

        NotificationManager gestorNotificaciones = contexto.getSystemService(NotificationManager.class);
        if (gestorNotificaciones != null) {
            gestorNotificaciones.createNotificationChannel(canal);
        }
    }

    private static PendingIntent crearAvisoPendiente(Context contexto, Tarea tarea) {
        return crearAvisoPendiente(
                contexto,
                tarea.getId(),
                tarea.getTitulo(),
                tarea.getFechaLimite()
        );
    }

    private static PendingIntent crearAvisoPendiente(
            Context contexto,
            String tareaId,
            String titulo,
            String fechaLimite
    ) {
        // El receptor recibe los datos necesarios para mostrar el aviso.
        Intent intent = new Intent(contexto, ReceptorRecordatorio.class);
        intent.putExtra(ReceptorRecordatorio.DATO_ID_TAREA, tareaId);
        intent.putExtra(ReceptorRecordatorio.DATO_TITULO_TAREA, titulo);
        intent.putExtra(ReceptorRecordatorio.DATO_FECHA_TAREA, fechaLimite);

        return PendingIntent.getBroadcast(
                contexto,
                tareaId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
