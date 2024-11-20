package co.edu.unipiloto.proyectovotos.propuestas;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.Locale;

import co.edu.unipiloto.proyectovotos.votos.Proyecto;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;
import co.edu.unipiloto.proyectovotos.votos.puestosDeVotacion;
import co.edu.unipiloto.proyectovotos.votos.votacion;

public class VerPropuestasdelocalidades extends AppCompatActivity {

        private Spinner spinnerLocalidades;
        private Spinner spinnerTitulos;
        private FirebaseAuth fAuth;
        private FirebaseFirestore fStore;
        private Button btnVotar;
        private List<Proyecto> listaProyectos = new ArrayList<>();

        // Google Maps
        private GoogleMap mMap;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_ver_propuestasdelocalidades);

            // Inicializar Firebase
            fAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();

            // Configurar el Spinner
            spinnerLocalidades = findViewById(R.id.spinner2);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.localidades, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLocalidades.setAdapter(adapter);

            spinnerTitulos = findViewById(R.id.spinnerTitulos);
            btnVotar = findViewById(R.id.btn_votar);

            mostrarTitulosPropuestas();

            // En la clase VerPropuestasdelocalidades
            btnVotar.setOnClickListener(view -> {
                String tituloSeleccionado = (String) spinnerTitulos.getSelectedItem();
                Log.d("Votar", "Título seleccionado: " + tituloSeleccionado);

                if (tituloSeleccionado != null && !tituloSeleccionado.isEmpty()) {
                    // Buscar el proyecto correspondiente en la lista
                    Proyecto proyectoSeleccionado = null;
                    for (Proyecto proyecto : listaProyectos) {
                        if (proyecto.getTitulo().equals(tituloSeleccionado)) {
                            proyectoSeleccionado = proyecto;
                            break;
                        }
                    }

                    // Si se encuentra el proyecto seleccionado
                    if (proyectoSeleccionado != null) {
                        // Redirigir a puestosDeVotacion y enviar el título del proyecto
                        Intent intentPuestos = new Intent(VerPropuestasdelocalidades.this, puestosDeVotacion.class);
                        intentPuestos.putExtra("proyectoTitulo", proyectoSeleccionado.getTitulo()); // Enviar el título
                        startActivity(intentPuestos);
                    } else {
                        Toast.makeText(VerPropuestasdelocalidades.this, "Error al encontrar el proyecto", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerPropuestasdelocalidades.this, "Selecciona un proyecto", Toast.LENGTH_SHORT).show();
                }
            });







            // Verificar si el usuario está autenticado
            FirebaseUser currentUser = fAuth.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(VerPropuestasdelocalidades.this, Login.class));
                finish(); // Para que no vuelva a esta actividad si el usuario no está autenticado
            }

            // Listener para el Spinner de localidades
            spinnerLocalidades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String localidadSeleccionada = parentView.getItemAtPosition(position).toString();
                    Toast.makeText(VerPropuestasdelocalidades.this, "Localidad seleccionada: " + localidadSeleccionada, Toast.LENGTH_SHORT).show();
                    mostrarProyectosPorLocalidad(localidadSeleccionada);  // Cargar proyectos según la localidad
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // No se seleccionó nada
                }
            });

            Button btnVerProyecto = findViewById(R.id.btn_ver_proyecto);
            btnVerProyecto.setOnClickListener(v -> {
                String tituloSeleccionado = spinnerTitulos.getSelectedItem().toString();

                if (tituloSeleccionado != null &&   !tituloSeleccionado.isEmpty()) {
                    Intent intent = new Intent(VerPropuestasdelocalidades.this, VerProyectoActivity.class);
                    intent.putExtra("tituloProyecto", tituloSeleccionado);
                    startActivity(intent);
                } else {
                    Toast.makeText(VerPropuestasdelocalidades.this, "Selecciona un proyecto", Toast.LENGTH_SHORT).show();
                }
            });

            // Manejar insets del sistema (si es necesario para adaptaciones UI)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Inicializar el mapa
            configurarMapa();
        }

        // Método para configurar el mapa
        private void configurarMapa() {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.puestosdevotacionmap);
            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    mMap = googleMap;


                    Log.d("VerPropuestas", "Mapa inicializado correctamente.");
                    // En tu método de configurarMapa, al hacer clic en un marcador:
                    mMap.setOnMarkerClickListener(marker -> {
                        String tituloProyecto = marker.getTitle(); // Obtén el título del marcador
                        Toast.makeText(VerPropuestasdelocalidades.this, "Proyecto seleccionado: " + tituloProyecto, Toast.LENGTH_SHORT).show();

                        // Guardar el título seleccionado en spinnerTitulos
                        for (int i = 0; i < spinnerTitulos.getAdapter().getCount(); i++) {
                            if (spinnerTitulos.getAdapter().getItem(i).equals(tituloProyecto)) {
                                spinnerTitulos.setSelection(i);
                                break;
                            }
                        }
                        return false;
                    });
                });
            } else {
                Log.e("VerPropuestas", "El fragmento de mapa es nulo.");
            }
        }

        // Método para mostrar proyectos por localidad desde Firebase Firestore
        private void mostrarProyectosPorLocalidad(String localidadSeleccionada) {
            // Limpiar el mapa y la lista de proyectos antes de agregar nuevos marcadores
            if (mMap != null) {
                mMap.clear();
            }
            listaProyectos.clear(); // Limpiar la lista de proyectos

            // Consulta a Firebase para obtener todas las propuestas con la localidad seleccionada
            fStore.collection("registroPropuesta")
                    .whereEqualTo("localidad", localidadSeleccionada)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> listaTitulos = new ArrayList<>();
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String barrio = document.getString("barrio");
                                    String titulo = document.getString("titulo");

                                    // Crear el proyecto y agregarlo a la lista
                                    Proyecto proyecto = new Proyecto(document.getId(), titulo, document.getString("descripcion"), document.getString("direccion"), document.getString("entidad"));
                                    listaProyectos.add(proyecto);
                                    listaTitulos.add(titulo);

                                    // Mostrar la ubicación en el mapa para cada propuesta
                                    mostrarUbicacionEnMapa(barrio, titulo);
                                }

                                // Actualizar el Spinner de títulos
                                ArrayAdapter<String> adapterTitulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTitulos);
                                adapterTitulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerTitulos.setAdapter(adapterTitulos);
                            } else {
                                // Si no hay propuestas en la localidad, limpiar el Spinner de títulos
                                ArrayAdapter<String> adapterTitulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
                                adapterTitulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerTitulos.setAdapter(adapterTitulos);
                                Toast.makeText(VerPropuestasdelocalidades.this, "No hay propuestas en esta localidad", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Firebase", "Error al obtener documentos: ", task.getException());
                            Toast.makeText(VerPropuestasdelocalidades.this, "Error al obtener las propuestas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    private void mostrarUbicacionEnMapa(String direccion, String tituloProyecto) {
        if (mMap == null) {
            Log.e("VerPropuestas", "GoogleMap is not initialized");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            // Buscar la ubicación de la dirección proporcionada
            addressList = geocoder.getFromLocationName(direccion, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Agregar un marcador por cada proyecto
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(tituloProyecto));

                // Mover la cámara para ver la ubicación
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12)); // Ajustar el nivel de zoom
            } else {
                Toast.makeText(this, "No se encontró la dirección: " + direccion, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }

        private void mostrarTitulosPropuestas() {
            fStore.collection("registroPropuesta")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listaProyectos.clear(); // Limpiar la lista antes de agregar nuevos proyectos
                            List<String> listaTitulos = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String titulo = document.getString("titulo");
                                String descripcion = document.getString("descripcion");
                                String direccion = document.getString("direccion");
                                String entidad = document.getString("entidad");

                                Proyecto proyecto = new Proyecto(id, titulo, descripcion, direccion, entidad);
                                listaProyectos.add(proyecto);
                                listaTitulos.add(titulo);
                            }

                            ArrayAdapter<String> adapterTitulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTitulos);
                            adapterTitulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTitulos.setAdapter(adapterTitulos);
                        }
                    });

        }
}

