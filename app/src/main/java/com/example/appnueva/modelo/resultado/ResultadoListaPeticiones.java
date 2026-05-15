package com.example.appnueva.modelo.resultado;

import com.example.appnueva.modelo.PeticionColaboracion;

import java.util.List;

public interface ResultadoListaPeticiones {
    void alExito(List<PeticionColaboracion> peticiones);

    void alError(String mensaje);
}
