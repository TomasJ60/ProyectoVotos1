package co.edu.unipiloto.proyectovotos.votos;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.propuestas.VerPropuestasdelocalidades;

public class puestosDeVotacion extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore fStore;
    private Button btnConfirmarPuesto;
    private String puestoSeleccionado = null;
    private String proyectoTitulo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_puestosdevotacion);

        fStore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        proyectoTitulo = intent.getStringExtra("proyectoTitulo");



        String proyectoIdSeleccionado = getIntent().getStringExtra("proyectoIdSeleccionado");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.puestosdevotacionmap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(puestosDeVotacion.this);
        }

        btnConfirmarPuesto = findViewById(R.id.btnConfirmarPuesto);
        btnConfirmarPuesto.setOnClickListener(view -> {
            if (puestoSeleccionado != null) {
                Intent inetEnviar = new Intent(puestosDeVotacion.this, votacion.class);
                inetEnviar.putExtra("puestoVotacion", puestoSeleccionado);
                inetEnviar.putExtra("proyectoTitulo", proyectoTitulo);// Usar la clave "puestoVotacion"
                startActivity(inetEnviar);
            } else {
                Toast.makeText(this, "Por favor, seleccione un puesto de votación", Toast.LENGTH_SHORT).show();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        cargarPuestosDeVotacion();

        // Configurar el listener una sola vez
        mMap.setOnMarkerClickListener(marker -> {
            puestoSeleccionado = marker.getTitle();  // Actualizar con el título del marcador seleccionado
            Toast.makeText(this, "Puesto seleccionado: " + puestoSeleccionado, Toast.LENGTH_SHORT).show();
            return false;
        });

    }

    private void cargarPuestosDeVotacion() {
        fStore.collection("puestosdeVotacion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombrePuesto = document.getString("nombrePuesto");
                            String direccion = document.getString("direccion");
                            String localidad = document.getString("localidad");

                            // Obtener las coordenadas a partir de la dirección
                            obtenerCoordenadasDesdeDireccion(direccion, nombrePuesto, localidad);
                        }

                        // Centrar el mapa en Bogotá
                        LatLng bogota = new LatLng(4.60971, -74.08175); // Coordenadas de Bogotá
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 12)); // Zoom en Bogotá
                    } else {
                        Toast.makeText(this, "Error al cargar los puestos de votación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void obtenerCoordenadasDesdeDireccion(String direccion, String nombrePuesto, String localidad) {
        Geocoder geocoder = new Geocoder(this);
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double latitud = address.getLatitude();
                    double longitud = address.getLongitude();

                    // Añadir el marcador en la ubicación correcta
                    runOnUiThread(() -> {
                        LatLng latLng = new LatLng(latitud, longitud);
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(nombrePuesto)
                                .snippet("Dirección: " + direccion + "\nLocalidad: " + localidad));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No se pudo encontrar la dirección: " + direccion, Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al geocodificar la dirección", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}


