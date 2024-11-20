package co.edu.unipiloto.proyectovotos.decisor;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipiloto.proyectovotos.R;

public class conteodevotosActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private Spinner spinnerLocalidad, spinnerTipoProyecto;
    private RecyclerView recyclerViewConteoVotos;
    private Button btnFiltrar;

    private AdapterConteoDeVotos adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteodevotos);

        datePicker = findViewById(R.id.datePicker);
        spinnerLocalidad = findViewById(R.id.spinnerLocalidad);
        spinnerTipoProyecto = findViewById(R.id.spinnerTipoProyecto);
        recyclerViewConteoVotos = findViewById(R.id.recyclerViewConteoVotos);
        btnFiltrar = findViewById(R.id.btnFiltrar);

        db = FirebaseFirestore.getInstance();

        recyclerViewConteoVotos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterConteoDeVotos(new ArrayList<>());
        recyclerViewConteoVotos.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterLocalidad = ArrayAdapter.createFromResource(
                this, R.array.localidades, android.R.layout.simple_spinner_item);
        adapterLocalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocalidad.setAdapter(adapterLocalidad);

        ArrayAdapter<CharSequence> adapterTipoProyecto = ArrayAdapter.createFromResource(
                this, R.array.tipo_proyecto_array, android.R.layout.simple_spinner_item);
        adapterTipoProyecto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoProyecto.setAdapter(adapterTipoProyecto);

        btnFiltrar.setOnClickListener(v -> {
            Log.d("conteoVotos", "btnFiltrar clicked");
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        Timestamp timestampInicio = new Timestamp(calendar.getTime());

        String localidadSeleccionada = spinnerLocalidad.getSelectedItem().toString();
        String tipoDeProyectoSeleccionado = spinnerTipoProyecto.getSelectedItem().toString();

        calendar.set(year, month, day, 23, 59, 59);
        Timestamp timestampFin = new Timestamp(calendar.getTime());

        // Log para los filtros aplicados
        Log.d("conteoVotos", "Aplicando filtros: " +
                "Localidad: " + localidadSeleccionada +
                ", Tipo de Proyecto: " + tipoDeProyectoSeleccionado +
                ", Fecha Inicio: " + timestampInicio +
                ", Fecha Fin: " + timestampFin);

        Query query = db.collection("guardarProyectos")
                .whereGreaterThanOrEqualTo("votingDeadline", timestampInicio)
                .whereLessThanOrEqualTo("votingDeadline", timestampFin);

        if (!localidadSeleccionada.equals("Todas")) {
            query = query.whereEqualTo("localidad", localidadSeleccionada);
        }

        if (!tipoDeProyectoSeleccionado.equals("Todos")) {
            query = query.whereEqualTo("tipoDeProyecto", tipoDeProyectoSeleccionado);
        }

        // Agregar log de la consulta con los filtros
        Log.d("conteoVotos", "Query construida con filtros: " +
                "Localidad: " + localidadSeleccionada + ", Tipo de Proyecto: " + tipoDeProyectoSeleccionado);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("conteoVotos", "Proyectos obtenidos: " + task.getResult().size());
                List<ConteoVotos> conteoVotosList = new ArrayList<>();
                int totalProyectos = task.getResult().size();
                int[] processedProjects = {0};

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String projectName = document.getString("titulo");
                    buscarVotos(projectName, conteoVotosList, totalProyectos, processedProjects);
                }
            } else {
                Log.e("conteoVotos", "Error obteniendo propuestas", task.getException());
            }
        });
    }

    private void buscarVotos(String projectName, List<ConteoVotos> conteoVotosList, int totalProyectos, int[] processedProjects) {
        Log.d("conteoVotos", "Buscando votos para el proyecto: " + projectName);
        db.collection("registroVotacion")
                .whereEqualTo("ProyectoVoto", projectName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) {
                        Log.w("conteoVotos", "No se encontraron votos para el proyecto: " + projectName);
                        processedProjects[0]++;
                    } else {
                        Log.d("conteoVotos", "Cantidad de votos obtenidos: " + task.getResult().size());
                        int yesVotes = 0, noVotes = 0, blankVotes = 0;

                        for (QueryDocumentSnapshot voteDoc : task.getResult()) {
                            String vote = voteDoc.getString("voto").trim().toLowerCase();
                            Log.d("conteoVotos", "Voto encontrado: '" + vote + "'");

                            if ("sí".equals(vote) || "si".equals(vote)) {
                                yesVotes++;
                            } else if ("no".equals(vote)) {
                                noVotes++;
                            } else if ("blanco".equals(vote)) {
                                blankVotes++;
                            } else {
                                Log.w("conteoVotos", "Voto desconocido o inválido: " + vote);
                            }
                        }

                        Log.d("conteoVotos", "Conteo final de votos para el proyecto '" + projectName + "': Sí=" + yesVotes + ", No=" + noVotes + ", Blanco=" + blankVotes);

                        ConteoVotos conteo = new ConteoVotos(projectName,
                                task.getResult().getDocuments().get(0).getString("localidad"),
                                task.getResult().getDocuments().get(0).getString("tipoDeProyecto"),
                                yesVotes, noVotes, blankVotes);
                        conteoVotosList.add(conteo);

                        processedProjects[0]++;
                    }
                    if (!task.isSuccessful()) {
                        Log.e("conteoVotos", "Error obteniendo votos para el proyecto '" + projectName + "': ", task.getException());
                        processedProjects[0]++;
                    }


                    // Verifica si ya se han procesado todos los proyectos
                    if (processedProjects[0] == totalProyectos) {
                        // Ordenar la lista por votos "sí" en orden descendente
                        Collections.sort(conteoVotosList, (c1, c2) -> Integer.compare(c2.getYesVotes(), c1.getYesVotes()));
                        runOnUiThread(() -> adapter.updateList(conteoVotosList));
                    }
                });
    }
}
