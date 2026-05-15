package com.example.appnueva.modelo.repositorio;

import com.example.appnueva.modelo.Usuario;
import com.example.appnueva.modelo.resultado.ResultadoAutenticacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RepositorioAutenticacionFirebase {
    private final FirebaseAuth autenticacion;
    private final DatabaseReference referenciaUsuarios;

    public RepositorioAutenticacionFirebase() {
        // Aquí se prepara la parte de usuarios y sesión.
        autenticacion = FirebaseAuth.getInstance();
        referenciaUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    public void iniciarSesion(String correo, String contrasena, ResultadoAutenticacion resultado) {
        // Intenta iniciar sesión con correo y contraseña.
        autenticacion.signInWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(datosSesion -> resultado.alExito(datosSesion.getUser()))
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public void registrar(String nombre, String correo, String contrasena, ResultadoAutenticacion resultado) {
        // Crea la cuenta y guarda los datos básicos del usuario.
        autenticacion.createUserWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(datosSesion -> {
                    FirebaseUser usuario = datosSesion.getUser();
                    if (usuario == null) {
                        resultado.alError("No se pudo crear el usuario.");
                        return;
                    }

                    Usuario usuarioApp = new Usuario(usuario.getUid(), nombre, correo);
                    referenciaUsuarios.child(usuario.getUid())
                            .setValue(usuarioApp)
                            .addOnSuccessListener(valorNoUsado -> resultado.alExito(usuario))
                            .addOnFailureListener(error -> resultado.alError(error.getMessage()));
                })
                .addOnFailureListener(error -> resultado.alError(error.getMessage()));
    }

    public FirebaseUser obtenerUsuarioActual() {
        return autenticacion.getCurrentUser();
    }

    public void cerrarSesion() {
        // Cierra la sesión del usuario actual.
        autenticacion.signOut();
    }
}
