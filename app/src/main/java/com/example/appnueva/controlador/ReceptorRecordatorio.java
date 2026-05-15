package com.example.appnueva.controlador;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.utilidad.GestorRecordatorios;

public class ReceptorRecordatorio extends BroadcastReceiver {
    public static final String DATO_ID_TAREA = "extra_id_tarea_recordatorio";
    public static final String DATO_TITULO_TAREA = "extra_titulo_tarea_recordatorio";
    public static final String DATO_FECHA_TAREA = "extra_fecha_tarea_recordatorio";

    @Override
    public void onReceive(Context contexto, Intent intent) {
        // Esta clase se ejecuta cuando llega la hora del recordatorio.
        String idTarea = intent.getStringExtra(DATO_ID_TAREA);
        String titulo = intent.getStringExtra(DATO_TITULO_TAREA);
        String fecha = intent.getStringExtra(DATO_FECHA_TAREA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // La notificación muestra lo importante de la tarea.
        NotificationCompat.Builder aviso = new NotificationCompat.Builder(contexto, GestorRecordatorios.ID_CANAL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Recordatorio de tarea")
                .setContentText("La tarea \"" + titulo + "\" vence el " + fecha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager gestorNotificaciones =
                (NotificationManager) contexto.getSystemService(Context.NOTIFICATION_SERVICE);
        if (gestorNotificaciones != null && idTarea != null) {
            gestorNotificaciones.notify(idTarea.hashCode(), aviso.build());
        }
    }
}
