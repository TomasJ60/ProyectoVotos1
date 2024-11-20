package co.edu.unipiloto.proyectovotos.decisor;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import co.edu.unipiloto.proyectovotos.R;

public class boton_eliminar extends AppCompatActivity {

    private FirebaseFirestore db;
    private String idProyecto;
    private String nombreProyecto;
    private TextView textViewTitulo;
    private Button buttonEliminar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boton_eliminar);

        db = FirebaseFirestore.getInstance();

        // Recibir el id y nombre del proyecto de la intención
        idProyecto = getIntent().getStringExtra("idProyecto");
        nombreProyecto = getIntent().getStringExtra("nombreProyecto");

        textViewTitulo = findViewById(R.id.textViewTituloEliminar);
        buttonEliminar = findViewById(R.id.buttonEliminar);

        textViewTitulo.setText(nombreProyecto);

        buttonEliminar.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(boton_eliminar.this);
        builder.setTitle("Confirmación de Eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar el proyecto \"" + nombreProyecto + "\"?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> eliminarProyecto(idProyecto, nombreProyecto));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void eliminarProyecto(String idProyecto, String nombreProyecto) {
        CollectionReference propuestaRef = db.collection("guardarProyectos");
        propuestaRef.document(idProyecto).delete().addOnSuccessListener(aVoid -> {
            Log.d("boton_eliminar", "Proyecto eliminado de guardarProyectos");
            eliminarDeRegistroVotacion(nombreProyecto);
        }).addOnFailureListener(e -> {
            Log.e("boton_eliminar", "Error eliminando proyecto: ", e);
            Toast.makeText(boton_eliminar.this, "Error eliminando el proyecto", Toast.LENGTH_SHORT).show();
        });
    }

    private void eliminarDeRegistroVotacion(String nombreProyecto) {
        CollectionReference votacionRef = db.collection("registroVotacion");
        Query query = votacionRef.whereEqualTo("ProyectoVoto", nombreProyecto);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    votacionRef.document(document.getId()).delete().addOnSuccessListener(aVoid -> {
                        Log.d("boton_eliminar", "Proyecto eliminado de registroVotacion");
                    }).addOnFailureListener(e -> Log.e("boton_eliminar", "Error eliminando de registroVotacion: ", e));
                }
                Toast.makeText(boton_eliminar.this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();

                // Enviar resultado OK y finalizar la actividad
                setResult(RESULT_OK);
                finish();  // Cerrar la actividad de eliminación
            } else {
                Log.e("boton_eliminar", "Error buscando en registroVotacion: ", task.getException());
                Toast.makeText(boton_eliminar.this, "Error eliminando de registroVotacion", Toast.LENGTH_SHORT).show();
            }
        });
    }
}