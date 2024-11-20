package co.edu.unipiloto.proyectovotos.decisor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import co.edu.unipiloto.proyectovotos.R;

public class boton_editar extends AppCompatActivity {

    private EditText etTituloProyecto, etDescripcionProyecto;
    private Button btnGuardarCambios;
    private FirebaseFirestore db;
    private String idProyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boton_editar);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Referencias a los elementos del XML
        etTituloProyecto = findViewById(R.id.etTituloProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        // Obtener el ID del proyecto desde el Intent
        idProyecto = getIntent().getStringExtra("idProyecto");

        // Cargar los detalles actuales del proyecto para mostrarlos en los campos de edición
        cargarDatosProyecto(idProyecto);

        // Configurar el listener para el botón de guardar cambios
        btnGuardarCambios.setOnClickListener(v -> {
            String nuevoTitulo = etTituloProyecto.getText().toString();
            String nuevaDescripcion = etDescripcionProyecto.getText().toString();

            if (!nuevoTitulo.isEmpty() && !nuevaDescripcion.isEmpty()) {
                editarProyecto(idProyecto, nuevoTitulo, nuevaDescripcion);
            } else {
                Toast.makeText(boton_editar.this, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosProyecto(String idProyecto) {
        db.collection("guardarProyectos").document(idProyecto).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etTituloProyecto.setText(documentSnapshot.getString("titulo"));
                        etDescripcionProyecto.setText(documentSnapshot.getString("descripcion"));
                    } else {
                        Toast.makeText(boton_editar.this, "Proyecto no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al cargar los datos", e));
    }

    private void editarProyecto(String idProyecto, String nuevoTitulo, String nuevaDescripcion) {
        // Editar en la colección guardarProyectos
        db.collection("guardarProyectos").document(idProyecto)
                .update("titulo", nuevoTitulo, "descripcion", nuevaDescripcion)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(boton_editar.this, "Proyecto actualizado correctamente", Toast.LENGTH_SHORT).show();
                    actualizarVotacion(nuevoTitulo);

                    // Enviar resultado OK y finalizar la actividad
                    setResult(RESULT_OK);
                    finish();  // Cerrar la actividad de edición
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el proyecto", e));
    }


    private void actualizarVotacion(String nuevoTitulo) {
        // Actualizar los documentos relacionados en la colección registroVotacion
        CollectionReference votacionRef = db.collection("registroVotacion");
        votacionRef.whereEqualTo("ProyectoVoto", getIntent().getStringExtra("nombreProyecto")).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        votacionRef.document(document.getId())
                                .update("ProyectoVoto", nuevoTitulo)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Votación actualizada"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar votación", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener los datos de votación",e));
    }
}