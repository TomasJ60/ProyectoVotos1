package co.edu.unipiloto.proyectovotos.propuestas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.edu.unipiloto.proyectovotos.Homes.HomeProyectos;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;
import co.edu.unipiloto.proyectovotos.notificaciones.VotingNotificationWorker;
import co.edu.unipiloto.proyectovotos.votos.votacion;


import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class registrodepropuestas extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private StorageReference storageReference;
    private String imageUrl;
    private Spinner spinnerTipoProyecto;
    private EditText editTextOtroTipoProyecto;
    private String tipoProyecto;


    private EditText editTextTitulo, editTextDescripcion;
    private Button buttonImage, buttonPublicar;
    private String fname, barrio, localidad, entidad;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    //Poner el tiempo
    private EditText editTextTiempoMinutos;
    private DatePicker datePicker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrodepropuestas);

        //Conexion con firebase
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        buttonImage = findViewById(R.id.buttonImage);
        buttonPublicar = findViewById(R.id.buttonPublicar);
        spinnerTipoProyecto = findViewById(R.id.spinnerTipoProyecto);
        editTextOtroTipoProyecto = findViewById(R.id.editTextOtroTipoProyecto);

        //tiempo de el proyecto
        editTextTiempoMinutos = findViewById(R.id.editTextTiempoMinutos);
        datePicker = findViewById(R.id.datePicker);


        cargarDatosRegistroProyectos();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                subirImagenFirebase(imageUri);
            }
        });

        buttonImage.setOnClickListener(view -> {
            // Abrir la galería para seleccionar imagen
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        buttonPublicar.setOnClickListener(view -> {
            String titulo = editTextTitulo.getText().toString();
            String descripcion = editTextDescripcion.getText().toString();

            //tiempo de votacion
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth(); // Recuerda que el mes es 0-indexado
            int year = datePicker.getYear();
            int duracionMin = Integer.parseInt(editTextTiempoMinutos.getText().toString());

            if (titulo.isEmpty()) {
                Toast.makeText(registrodepropuestas.this, "Se requiere llenar el campo del título", Toast.LENGTH_LONG).show();
                return;
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(registrodepropuestas.this, "Se requiere llenar el campo de descripción", Toast.LENGTH_LONG).show();
                return;
            }

            // Guardar los datos de la propuesta
            guardarDatosRegistroPropuesta(titulo, descripcion, duracionMin, year, month, day);
            enviarNotificacion(fname, titulo);

            Intent votacionIntent = new Intent(registrodepropuestas.this, votacion.class);
            votacionIntent.putExtra("proyectoTitulo", titulo);
            startActivity(votacionIntent);


        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        spinnerTipoProyecto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoProyecto = parent.getItemAtPosition(position).toString();

                // Muestra el campo de texto si el usuario selecciona "Otro"
                if (tipoProyecto.equals("Otro")) {
                        editTextOtroTipoProyecto.setVisibility(View.VISIBLE);
                } else {
                    editTextOtroTipoProyecto.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se necesita implementar nada aquí
            }
        });

    }

    private void cargarDatosRegistroProyectos() {
        String userID = fAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("registroProyectos").document(userID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Carga de datos, asegurándote de que estás obteniendo los nombres de los campos correctos
                    fname = documentSnapshot.getString("fName"); // Asegúrate de que el campo en la BD sea "fName"
                    barrio = documentSnapshot.getString("barrio");
                    localidad = documentSnapshot.getString("localidad");
                    entidad = documentSnapshot.getString("entidad");

                    Log.d("Firestore", "Datos cargados: " + fname + ", " + barrio + ", " + localidad + ", " + entidad);
                } else {
                    Log.d("Firestore", "No se encontró el documento");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Error al cargar los datos: " + e.getMessage());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            subirImagenFirebase(imageUri);
        }
    }

    private void subirImagenFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child("images/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    Toast.makeText(registrodepropuestas.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Storage", "Error al subir la imagen: " + e.getMessage());
                            Toast.makeText(registrodepropuestas.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }


    private void guardarDatosRegistroPropuesta(String titulo, String descripcion, int duracionMin, int year, int month, int day) {
        // Validar que la duración en minutos sea positiva
        if (duracionMin <= 0) {
            Toast.makeText(this, "La duración debe ser mayor a cero minutos", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoProyectoFinal = tipoProyecto.equals("Otro") ? editTextOtroTipoProyecto.getText().toString() : tipoProyecto;

        Map<String, Object> propuesta = new HashMap<>();
        propuesta.put("titulo", titulo);
        propuesta.put("descripcion", descripcion);
        propuesta.put("fname", fname);
        propuesta.put("barrio", barrio);
        propuesta.put("localidad", localidad);
        propuesta.put("entidad", entidad);
        propuesta.put("imagenUrl", imageUrl);
        propuesta.put("tipoDeProyecto", tipoProyectoFinal);

        // Calcular el tiempo límite para votar
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Date fechaInicio = cal.getTime();
        propuesta.put("fechaInicio", fechaInicio);

        cal.add(Calendar.MINUTE, duracionMin);
        Date votingDeadline = cal.getTime();
        propuesta.put("votingDeadline", votingDeadline);

        db.collection("registroPropuesta").add(propuesta)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(registrodepropuestas.this, "Propuesta guardada exitosamente", Toast.LENGTH_SHORT).show();
                        programarNotificacion(titulo, votingDeadline.getTime());
                        startActivity(new Intent(getApplicationContext(), HomeProyectos.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error al guardar la propuesta: " + e.getMessage());
                    }
                });
    }

    private void enviarNotificacion(String nombrePlaneador, String nombreProyecto) {
        String tituloPrincipal = "Planeador " + nombrePlaneador;
        String tituloSecundario = "Su proyecto se ha generado con éxito 😊 " + nombreProyecto;

        String CHANNEL_ID = "propuesta_notification_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de Propuestas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de propuestas");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Crear el PendingIntent para abrir Login.class
        Intent intent = new Intent(this, Login.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(tituloPrincipal)
                .setContentText(tituloSecundario)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // Asociar el PendingIntent a la notificación

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de notificaciones no otorgado", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationManager.notify(1, builder.build());
    }

    public void programarNotificacion(String nombreProyecto, long votingDeadlineMillis) {
        long tiempoActual = System.currentTimeMillis();
        long tiempoNotificacion = votingDeadlineMillis - 5 * 60 * 1000; // 5 minutos antes del deadline

        if (tiempoNotificacion > tiempoActual) {
            long retraso = tiempoNotificacion - tiempoActual;

            // Pasar datos al Worker
            Data datos = new Data.Builder()
                    .putString("nombreProyecto", nombreProyecto)
                    .putString("votingDeadline", new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(new Date(votingDeadlineMillis)))
                    .build();

            // Crear una solicitud de trabajo
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(VotingNotificationWorker.class)
                    .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
                    .setInputData(datos)
                    .build();

            // Encolar la solicitud
            WorkManager.getInstance(this).enqueue(workRequest);

            Toast.makeText(this, "Notificación programada 5 minutos antes del cierre de votaciones", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "El plazo ya pasó o no hay tiempo suficiente para programar", Toast.LENGTH_SHORT).show();
        }
    }
}