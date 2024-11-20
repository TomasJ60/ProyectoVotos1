package co.edu.unipiloto.proyectovotos.propuestas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.votos.puestosDeVotacion;

public class VerPropuestasPorTipoDeProyecto extends AppCompatActivity {

    private Spinner tipoProyectoSpinner, proyectoSpinner;
    private Button votarButton, verProyectoButton;
    private FirebaseFirestore fStore;
    private ArrayAdapter<String> proyectoAdapter;
    private ArrayList<String> proyectosList = new ArrayList<>();
    private String tipoProyectoSeleccionado, proyectoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_propuestas_por_tipo_de_proyecto);

        tipoProyectoSpinner = findViewById(R.id.tipoProyectoSpinner);
        proyectoSpinner = findViewById(R.id.proyectoSpinner);
        votarButton = findViewById(R.id.votarButton);
        verProyectoButton = findViewById(R.id.verProyectoButton);

        fStore = FirebaseFirestore.getInstance();

        // Configurar el primer Spinner con tipos de proyecto
        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipo_proyecto_array, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoProyectoSpinner.setAdapter(tipoAdapter);

        tipoProyectoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoProyectoSeleccionado = parent.getItemAtPosition(position).toString();
                cargarProyectosPorTipo(tipoProyectoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                proyectosList.clear();
                proyectoAdapter.notifyDataSetChanged();
            }
        });

        // Configurar el adapter para el segundo Spinner
        proyectoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proyectosList);
        proyectoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        proyectoSpinner.setAdapter(proyectoAdapter);

        proyectoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                proyectoSeleccionado = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                proyectoSeleccionado = null;
            }
        });

        // Configurar el botón de votar
        votarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proyectoSeleccionado != null) {
                    Intent votarIntent = new Intent(VerPropuestasPorTipoDeProyecto.this, puestosDeVotacion.class);
                    votarIntent.putExtra("proyectoTitulo    ", proyectoSeleccionado);
                    startActivity(votarIntent);
                } else {
                    Toast.makeText(VerPropuestasPorTipoDeProyecto.this, "Seleccione un proyecto para votar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar el botón de ver proyecto
        verProyectoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proyectoSeleccionado != null) {
                    Intent verProyectoIntent = new Intent(VerPropuestasPorTipoDeProyecto.this, VerProyectoActivity.class);
                    verProyectoIntent.putExtra("tituloProyecto", proyectoSeleccionado);
                    startActivity(verProyectoIntent);
                } else {
                    Toast.makeText(VerPropuestasPorTipoDeProyecto.this, "Seleccione un proyecto para ver", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarProyectosPorTipo(String tipo) {
        proyectosList.clear();
        CollectionReference proyectosRef = fStore.collection("registroPropuesta");
        proyectosRef.whereEqualTo("tipoDeProyecto", tipo)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(VerPropuestasPorTipoDeProyecto.this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String nombreProyecto = doc.getString("titulo");
                            if (nombreProyecto != null) {
                                proyectosList.add(nombreProyecto);
                            }
                        }
                        proyectoAdapter.notifyDataSetChanged();
                    }
                });
    }

}

