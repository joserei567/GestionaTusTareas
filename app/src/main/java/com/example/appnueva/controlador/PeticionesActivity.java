package com.example.appnueva.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appnueva.R;
import com.example.appnueva.modelo.PeticionColaboracion;
import com.example.appnueva.modelo.repositorio.RepositorioAutenticacionFirebase;
import com.example.appnueva.modelo.repositorio.RepositorioColaboracionFirebase;
import com.example.appnueva.modelo.resultado.ResultadoListaPeticiones;
import com.example.appnueva.modelo.resultado.ResultadoOperacion;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PeticionesActivity extends AppCompatActivity {
    // Pantalla donde se aceptan las tareas compartidas por otros usuarios.
    private RepositorioAutenticacionFirebase repositorioAutenticacion;
    private RepositorioColaboracionFirebase repositorioColaboracion;
    private FirebaseUser usuarioActual;
    private TextView textoSinPeticiones;
    private ArrayAdapter<String> adaptador;
    private final List<PeticionColaboracion> peticiones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_peticiones);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizPeticiones), (vista, margen) -> {
            Insets barrasSistema = margen.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom);
            return margen;
        });

        repositorioAutenticacion = new RepositorioAutenticacionFirebase();
        repositorioColaboracion = new RepositorioColaboracionFirebase();
        usuarioActual = repositorioAutenticacion.obtenerUsuarioActual();
        if (usuarioActual == null) {
            abrirInicioSesion();
            return;
        }

        textoSinPeticiones = findViewById(R.id.textoSinPeticiones);
        ListView listaPeticiones = findViewById(R.id.listaPeticiones);
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listaPeticiones.setAdapter(adaptador);

        listaPeticiones.setOnItemClickListener((padre, vista, posicion, id) -> mostrarOpcionesPeticion(posicion));
        cargarPeticiones();
    }

    private void cargarPeticiones() {
        // Busca las peticiones pendientes usando el correo del usuario actual.
        repositorioColaboracion.obtenerPeticiones(usuarioActual.getEmail(), new ResultadoListaPeticiones() {
            @Override
            public void alExito(List<PeticionColaboracion> peticionesRecibidas) {
                peticiones.clear();
                peticiones.addAll(peticionesRecibidas);
                pintarPeticiones();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(PeticionesActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintarPeticiones() {
        adaptador.clear();
        for (PeticionColaboracion peticion : peticiones) {
            adaptador.add(peticion.getPropietarioCorreo() + "\nQuiere compartir: " + peticion.getTituloTarea());
        }
        adaptador.notifyDataSetChanged();
        textoSinPeticiones.setText(peticiones.isEmpty() ? getString(R.string.sin_peticiones) : "");
    }

    private void mostrarOpcionesPeticion(int posicion) {
        if (posicion < 0 || posicion >= peticiones.size()) {
            return;
        }

        PeticionColaboracion peticion = peticiones.get(posicion);
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_responder_peticion)
                .setMessage(getString(R.string.mensaje_responder_peticion, peticion.getPropietarioCorreo(), peticion.getTituloTarea()))
                .setPositiveButton(R.string.aceptar_peticion, (dialogo, cual) -> aceptarPeticion(peticion))
                .setNegativeButton(R.string.denegar_peticion, (dialogo, cual) -> denegarPeticion(peticion))
                .setNeutralButton(R.string.cancelar, null)
                .show();
    }

    private void aceptarPeticion(PeticionColaboracion peticion) {
        // Al aceptar, se copia la tarea en mi cuenta y se comparte el mismo chat.
        repositorioColaboracion.aceptarPeticion(peticion, usuarioActual.getUid(), new ResultadoOperacion() {
            @Override
            public void alExito() {
                Toast.makeText(PeticionesActivity.this, R.string.peticion_aceptada, Toast.LENGTH_SHORT).show();
                cargarPeticiones();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(PeticionesActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void denegarPeticion(PeticionColaboracion peticion) {
        repositorioColaboracion.denegarPeticion(peticion, new ResultadoOperacion() {
            @Override
            public void alExito() {
                Toast.makeText(PeticionesActivity.this, R.string.peticion_denegada, Toast.LENGTH_SHORT).show();
                cargarPeticiones();
            }

            @Override
            public void alError(String mensaje) {
                Toast.makeText(PeticionesActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirInicioSesion() {
        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
