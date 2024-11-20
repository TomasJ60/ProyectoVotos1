package co.edu.unipiloto.proyectovotos.decisor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.votos.Proyecto;

public class boton_Ver extends AppCompatActivity {
    private TextView tvNombrePoryecto, tvNombrePlaneador, tvDireccionProyecto;
    private TextView tvTotalVotos, tvVotosFavor, tvVotosContra, tvVotoBlanco;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String proyectoId;

    private PieChart pieChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boton_ver);

        // Inicializar TextViews
        tvNombrePoryecto = findViewById(R.id.tvNombrePoryecto);
        tvNombrePlaneador = findViewById(R.id.tvNombrePlaneador);
        tvDireccionProyecto = findViewById(R.id.tvDireccionProyecto);
        tvTotalVotos = findViewById(R.id.tvTotalVotos);
        tvVotosFavor = findViewById(R.id.tvVotosFavor);
        tvVotosContra = findViewById(R.id.tvVotosContra);
        tvVotoBlanco = findViewById(R.id.tvVotoBlanco);

        pieChart = findViewById(R.id.pieChart);

        // Inicializar Firebase Auth y Firestore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        String nombreProyecto = getIntent().getStringExtra("nombreProyecto");
        Log.d("ProyectoNombre", "Nombre del Proyecto: " + nombreProyecto);


        // Recupera el ID del proyecto
        String idProyecto = getIntent().getStringExtra("idProyecto");
        Log.d("ProyectoIDVer", "ID del Proyecto en Intent: " + idProyecto);  // Verificar si el ID se está recibiendo correctamente
        cargarDetallesProyecto(idProyecto);

        // Obtener el ID del proyecto del Intent y verificar
        proyectoId = getIntent().getStringExtra("idProyecto");
        Log.d("ProyectoID", "ID del Proyecto en ProyectoID: " + proyectoId);

        // Cargar datos del proyecto
        cargarDatosProyecto(proyectoId);
        cargarDatosVotacion(nombreProyecto);
        mostrarVotosProyecto(nombreProyecto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarDetallesProyecto(String idProyecto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("guardarProyectos").document(idProyecto).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Proyecto proyecto = document.toObject(Proyecto.class);

                        } else {

                        }
                    } else {

                    }
                });
    }

    private void cargarDatosProyecto(String proyectoId) {
        DocumentReference proyectoRef = fStore.collection("guardarProyectos").document(proyectoId);
        proyectoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                tvNombrePoryecto.setText(documentSnapshot.getString("titulo"));
                tvDireccionProyecto.setText(documentSnapshot.getString("barrio"));
                tvNombrePlaneador.setText(documentSnapshot.getString("fname"));

                Log.d("Firestore", "Documento encontrado: " + documentSnapshot.getData());
                String planeadorId = documentSnapshot.getString("idPlaneador");
            } else {
                Log.d("Firestore", "El documento no existe.");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error al obtener los datos", e);
        });
    }


    private void cargarDatosVotacion(String nombreProyecto) {
        CollectionReference votacionRef = fStore.collection("registroVotacion");
        votacionRef.whereEqualTo("ProyectoVoto", nombreProyecto).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalVotos = queryDocumentSnapshots.size();
            int votosFavor = 0;
            int votosContra = 0;
            int votosBlanco = 0;

            if (queryDocumentSnapshots.isEmpty()) {
                Log.d("Votacion", "No se encontraron documentos para el proyecto: " + nombreProyecto);
            }

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                String voto = document.getString("voto");
                Log.d("Votacion", "Voto encontrado: " + voto); // Log para cada voto encontrado

                if (voto != null) {
                    if ("si".equalsIgnoreCase(voto)) {
                        votosFavor++;
                    } else if ("no".equalsIgnoreCase(voto)) {
                        votosContra++;
                    } else if ("blanco".equalsIgnoreCase(voto)) {
                        votosBlanco++;
                    }
                } else {
                    Log.d("Votacion", "El documento no contiene el campo 'voto': " + document.getId());
                }
            }
            
            tvTotalVotos.setText(String.valueOf(totalVotos));
            tvVotosFavor.setText(String.valueOf(votosFavor));
            tvVotosContra.setText(String.valueOf(votosContra));
            tvVotoBlanco.setText(String.valueOf(votosBlanco));

            // Log para verificar que los datos son correctos
            Log.d("Votacion", "Total Votos: " + totalVotos);
            Log.d("Votacion", "Votos a favor: " + votosFavor);
            Log.d("Votacion", "Votos en contra: " + votosContra);
            Log.d("Votacion", "Votos en blanco: " + votosBlanco);
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error al obtener los datos de votación", e);
        });
    }

    private void mostrarVotosProyecto(String tituloProyecto) {
        fStore.collection("registroVotacion")
                .whereEqualTo("ProyectoVoto", tituloProyecto)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        int votosSi = 0;
                        int votosNo = 0;
                        int votosBlanco = 0;

                        for (DocumentSnapshot document : task.getResult()) {
                            String voto = document.getString("voto");
                            if ("si".equalsIgnoreCase(voto)) {
                                votosSi++;
                            } else if ("no".equalsIgnoreCase(voto)) {
                                votosNo++;
                            } else if ("blanco".equalsIgnoreCase(voto)) {
                                votosBlanco++;
                            }
                        }

                        // Calcular el total de votos
                        int totalVotos = votosSi + votosNo + votosBlanco;

                        // Evitar división por cero si no hay votos
                        if (totalVotos == 0) {
                            Toast.makeText(this, "No se han registrado votos aún", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Calcular los porcentajes
                        float porcentajeSi = (votosSi / (float) totalVotos) * 100;
                        float porcentajeNo = (votosNo / (float) totalVotos) * 100;
                        float porcentajeBlanco = (votosBlanco / (float) totalVotos) * 100;

                        // Crear la gráfica de pastel con porcentajes
                        List<PieEntry> entries = new ArrayList<>();
                        entries.add(new PieEntry(porcentajeSi, "Sí (" + String.format("%.1f", porcentajeSi) + "%)"));
                        entries.add(new PieEntry(porcentajeNo, "No (" + String.format("%.1f", porcentajeNo) + "%)"));
                        entries.add(new PieEntry(porcentajeBlanco, "Blanco (" + String.format("%.1f", porcentajeBlanco) + "%)"));

                        // Crear una lista de colores personalizados
                        ArrayList<Integer> colors = new ArrayList<>();
                        colors.add(Color.parseColor("#4CAF50")); // Verde para Sí
                        colors.add(Color.parseColor("#F44336")); // Rojo para No
                        colors.add(Color.parseColor("#FFEB3B")); // Amarillo para Blanco

                        PieDataSet dataSet = new PieDataSet(entries, "Resultados de la votación");
                        dataSet.setColors(colors); // Asigna los colores personalizados

                        PieData pieData = new PieData(dataSet);
                        pieChart.setData(pieData);
                        pieChart.invalidate();
                    } else {
                        Toast.makeText(this, "No se encontraron votos para este proyecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error obteniendo los votos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

