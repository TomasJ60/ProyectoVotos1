package co.edu.unipiloto.proyectovotos.Homes;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.decisor.ProyectosAdapter;
import co.edu.unipiloto.proyectovotos.votos.Proyecto;

public class HomeAdmin extends AppCompatActivity {

    private Spinner spinnerLocalidades;
    private RecyclerView recyclerViewProyectos;
    private ProyectosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);

        inicializarRecyclerView();

        spinnerLocalidades = findViewById(R.id.spinnerLocalidades); // Asegúrate de que el spinner esté en tu layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.localidades, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocalidades.setAdapter(spinnerAdapter);

        spinnerLocalidades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String localidadSeleccionada = parent.getItemAtPosition(position).toString();
                cargarProyectosPorLocalidad(localidadSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarProyectosPorLocalidad(String localidad) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference proyectosRef = db.collection("guardarProyectos");

        // Consulta para obtener los proyectos en la localidad seleccionada
        proyectosRef.whereEqualTo("localidad", localidad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Proyecto> listaProyectos = new ArrayList<>(); // Lista para almacenar objetos Proyecto
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String titulo = document.getString("titulo");
                            String idProyecto = document.getId();
                            if (titulo != null) {
                                listaProyectos.add(new Proyecto(idProyecto, titulo)); // Crear objeto Proyecto
                            }
                        }
                        // Actualizar el RecyclerView con la lista de proyectos
                        actualizarRecyclerView(listaProyectos);
                    } else {
                        Toast.makeText(this, "Error al cargar los proyectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void inicializarRecyclerView() {
        recyclerViewProyectos = findViewById(R.id.recyclerViewProyectos); // Asegúrate de tener el RecyclerView en tu layout
        recyclerViewProyectos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProyectosAdapter(new ArrayList<>(), this);
        recyclerViewProyectos.setAdapter(adapter);
    }

    private void actualizarRecyclerView(List<Proyecto> listaProyectos) {
        // Actualizar el adaptador del RecyclerView con la nueva lista de proyectos
        adapter.actualizarListaProyectos(listaProyectos);
    }
}