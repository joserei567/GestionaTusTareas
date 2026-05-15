package com.example.appnueva.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.resultado.ResultadoAutenticacion;
import com.google.firebase.auth.FirebaseUser;

public class InicioSesionActivity extends AppCompatActivity {
    // Pantalla de entrada a la app.
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private EditText campoCorreo;
    private EditText campoContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizInicioSesion), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        if (repositorioAutenticacion.obtenerUsuarioActual() != null) {
            abrirPrincipal();
            return;
        }

        campoCorreo = findViewById(R.id.campoCorreoInicio);
        campoContrasena = findViewById(R.id.campoContrasenaInicio);
        Button botonIniciarSesion = findViewById(R.id.botonAcceder);
        Button botonIrARegistro = findViewById(R.id.botonIrRegistro);

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());
        botonIrARegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    private void iniciarSesion() {
        // Comprueba los campos y pide a Firebase que inicie sesión.
        String correo = campoCorreo.getText().toString().trim();
        String contrasena = campoContrasena.getText().toString().trim();

        if (correo.isEmpty()) {
            campoCorreo.setError(getString(R.string.campo_obligatorio));
            return;
        }
        if (contrasena.isEmpty()) {
            campoContrasena.setError(getString(R.string.campo_obligatorio));
            return;
        }

        repositorioAutenticacion.iniciarSesion(correo, contrasena, new ResultadoAutenticacion() {
            @Override
            public void alExito(FirebaseUser usuario) {
                Toast.makeText(InicioSesionActivity.this, R.string.sesion_iniciada, Toast.LENGTH_SHORT).show();
                abrirPrincipal();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(InicioSesionActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirPrincipal() {
        // Se limpia el historial para que no se pueda volver al login con atrás.
        Intent intent = new Intent(this, PrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
