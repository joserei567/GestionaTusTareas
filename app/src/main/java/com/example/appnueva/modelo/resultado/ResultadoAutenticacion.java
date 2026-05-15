package com.example.appnueva.modelo.resultado;

import com.google.firebase.auth.FirebaseUser;

public interface ResultadoAutenticacion {
    void alExito(FirebaseUser usuario);

    void alError(String mensaje);
}
