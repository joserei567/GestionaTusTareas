package com.example.appnueva.modelo.utilidad;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class UtilidadesFecha {
    private static final String[] FORMATOS_ADMITIDOS = {"yyyy-MM-dd", "yyyy/MM/dd"};

    private UtilidadesFecha() {
    }

    public static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static long obtenerMomentoRecordatorio(String fechaTexto) {
        Date fecha = convertirTextoAFecha(fechaTexto);
        if (fecha == null) {
            return -1L;
        }

        // El aviso se lanza a las 9:00 del día límite para que sea fácil de explicar.
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.set(Calendar.HOUR_OF_DAY, 9);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTimeInMillis();
    }

    private static Date convertirTextoAFecha(String fechaTexto) {
        for (String formato : FORMATOS_ADMITIDOS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(formato, Locale.getDefault());
                dateFormat.setLenient(false);
                return dateFormat.parse(fechaTexto);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
