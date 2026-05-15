package com.example.appnueva.modelo.resultado;

import com.example.appnueva.modelo.MensajeChat;

import java.util.List;

public interface ResultadoListaMensajes {
    void alExito(List<MensajeChat> mensajes);

    void alError(String mensaje);
}
