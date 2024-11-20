package co.edu.unipiloto.proyectovotos.propuestas;

import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.proyectovotos.R;

public class VerProyectoActivity extends AppCompatActivity {

    private FirebaseFirestore fStore;
    private TextView txtTitulo, txtDescripcion, txtEntidad, txtBarrio;
    private ImageView imgProyecto;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_proyecto);

        // Inicializar Firestore
        fStore = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        txtTitulo = findViewById(R.id.txtTitulo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtEntidad = findViewById(R.id.txtEntidad);
        txtBarrio = findViewById(R.id.txtBarrio);
        imgProyecto = findViewById(R.id.imgProyecto);
        pieChart = findViewById(R.id.pieChart);

        // Obtener el título del proyecto que fue seleccionado
        String tituloProyecto = getIntent().getStringExtra("tituloProyecto");

        // Mostrar el proyecto seleccionado
        mostrarProyecto(tituloProyecto);
        mostrarVotosProyecto(tituloProyecto);

    }

    private void mostrarProyecto(String titulo) {
        fStore.collection("registroPropuesta")
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Si task.getResult() no es null y contiene documentos
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            // Obtener los datos del proyecto
                            String descripcion = document.getString("descripcion");
                            String entidad = document.getString("entidad");
                            String barrio = document.getString("barrio");
                            String urlImagen = document.getString("imagenUrl");

                            // Mostrar los datos en las vistas
                            txtTitulo.setText(titulo);
                            txtDescripcion.setText(descripcion);
                            txtEntidad.setText(entidad);
                            txtBarrio.setText(barrio);

                            // Verifica que la URL de la imagen no sea null o vacía
                            if (urlImagen != null && !urlImagen.isEmpty()) {
                                // Usa Glide para cargar la imagen
                                Glide.with(this)
                                        .load(urlImagen)
                                        .into(imgProyecto);
                            } else {
                                // Si no hay imagen, muestra un placeholder o mensaje
                                imgProyecto.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    } else {
                        Log.d("VerProyectoActivity", "No se encontró el proyecto o hubo un error.");
                        Toast.makeText(VerProyectoActivity.this, "No se encontró el proyecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("VerProyectoActivity", "Error obteniendo el proyecto: " + e.getMessage());
                    Toast.makeText(VerProyectoActivity.this, "Error obteniendo el proyecto", Toast.LENGTH_SHORT).show();
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
                        pieChart.invalidate(); // Actualiza la gráfica
                    } else {
                        Toast.makeText(this, "No se encontraron votos para este proyecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error obteniendo los votos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}