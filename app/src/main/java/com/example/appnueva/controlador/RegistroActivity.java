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

public class RegistroActivity extends AppCompatActivity {
    // Pantalla para crear una cuenta nueva.
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private EditText campoNombre;
    private EditText campoCorreo;
    private EditText campoContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizRegistro), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        campoNombre = findViewById(R.id.campoNombreRegistro);
        campoCorreo = findViewById(R.id.campoCorreoRegistro);
        campoContrasena = findViewById(R.id.campoContrasenaRegistro);
        Button botonCrearCuenta = findViewById(R.id.botonCrearCuenta);
        Button botonVolverInicio = findViewById(R.id.botonVolverInicio);

        botonCrearCuenta.setOnClickListener(v -> registrarUsuario());
        botonVolverInicio.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        // Valida los datos antes de mandar el registro a Firebase.
        String nombre = campoNombre.getText().toString().trim();
        String correo = campoCorreo.getText().toString().trim();
        String contrasena = campoContrasena.getText().toString().trim();

        if (nombre.isEmpty()) {
            campoNombre.setError(getString(R.string.campo_obligatorio));
            return;
        }
        if (correo.isEmpty()) {
            campoCorreo.setError(getString(R.string.campo_obligatorio));
            return;
        }
        if (contrasena.length() < 6) {
            campoContrasena.setError(getString(R.string.contrasena_corta));
            return;
        }

        repositorioAutenticacion.registrar(nombre, correo, contrasena, new ResultadoAutenticacion() {
            @Override
            public void alExito(FirebaseUser usuario) {
                Toast.makeText(RegistroActivity.this, R.string.cuenta_creada, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistroActivity.this, PrincipalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}
