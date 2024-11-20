package co.edu.unipiloto.proyectovotos.guardar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;

public class GuardarProyecto extends Worker {

    private FirebaseFirestore db;

    public GuardarProyecto(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public Result doWork() {
        try {
            Timestamp currentTime = Timestamp.now();
            Log.i("GuardarProyecto", "Iniciando transferencia de proyectos. Hora actual: " + currentTime.toDate());

            // Consultar proyectos cuya fecha límite ya pasó
            Tasks.await(
                    db.collection("registroPropuesta")
                            .whereLessThan("votingDeadline", currentTime)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Log.i("GuardarProyecto", "Proyectos encontrados: " + task.getResult().size());
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        checkAndTransferProject(document);
                                    }
                                } else {
                                    Log.e("GuardarProyecto", "Error al consultar proyectos vencidos", task.getException());
                                }
                            })
            );

            return Result.success();
        } catch (Exception e) {
            Log.e("GuardarProyecto", "Error en doWork: ", e);
            return Result.failure();
        }
    }

    private void checkAndTransferProject(QueryDocumentSnapshot document) {
        String projectId = document.getId();

        // Verificar si el proyecto ya existe en la colección destino
        db.collection("guardarProyectos")
                .whereEqualTo("originalProjectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Si no existe, transferir y luego eliminar
                            transferAndRemoveProject(document);
                        } else {
                            Log.i("GuardarProyecto", "Proyecto ya existe en guardarProyectos: " + projectId);
                        }
                    } else {
                        Log.e("GuardarProyecto", "Error al verificar si el proyecto ya existe en guardarProyectos: " + projectId, task.getException());
                    }
                });
    }

    private void transferAndRemoveProject(QueryDocumentSnapshot document) {
        Map<String, Object> proyecto = document.getData();
        proyecto.put("originalProjectId", document.getId()); // Agregar el ID original del proyecto
        Log.i("GuardarProyecto", "Transfiriendo proyecto: " + proyecto);

        // Copiar a la nueva colección
        db.collection("guardarProyectos")
                .add(proyecto)
                .addOnSuccessListener(documentReference -> {
                    Log.i("GuardarProyecto", "Proyecto transferido con éxito: " + document.getId());

                    // Eliminar el documento original después de la transferencia exitosa
                    document.getReference().delete()
                            .addOnSuccessListener(aVoid ->
                                    Log.i("GuardarProyecto", "Proyecto eliminado de registroPropuesta: " + document.getId()))
                            .addOnFailureListener(e ->
                                    Log.e("GuardarProyecto", "Error al eliminar proyecto original: " + document.getId(), e));
                })
                .addOnFailureListener(e -> {
                    Log.e("GuardarProyecto", "Error al transferir proyecto: " + document.getId(), e);
                });
    }

    public static void scheduleProjectTransfer(Context context) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GuardarProyecto.class)
                .build();

        WorkManager.getInstance(context)
                .enqueue(workRequest);

        Log.i("GuardarProyecto", "Trabajo programado.");
    }
}

