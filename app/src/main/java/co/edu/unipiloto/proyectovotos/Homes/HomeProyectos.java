package co.edu.unipiloto.proyectovotos.Homes;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.propuestas.registrodepropuestas;

public class HomeProyectos extends AppCompatActivity implements OnMapReadyCallback {
    private static final int GALLERY_INTENT_CODE = 1023;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    ImageView profileImage;
    Button changeProfileImage, btnGenerarPropuesta;
    StorageReference storageReference;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    GoogleMap mMap;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homeproyectos);

        profileImage = findViewById(R.id.profileImage);
        changeProfileImage = findViewById(R.id.changeProfile);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userId = fAuth.getCurrentUser().getUid();
        btnGenerarPropuesta = findViewById(R.id.btngenerar);

        TextView nnombre = findViewById(R.id.textViewnombre);
        TextView ncargo = findViewById(R.id.textViewcargo);
        TextView ndirecccion = findViewById(R.id.textViewDireccion);
        TextView nlocalidad = findViewById(R.id.textViewLocalidad);

        StorageReference profileRef = storageReference.child("registroProyectos/" + fAuth.getCurrentUser().getUid() + "/profile.jpeg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImage));

        changeProfileImage.setOnClickListener(v -> {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(openGalleryIntent, 1000);
        });

        //image

        DocumentReference documentReference = fStore.collection("registroProyectos").document(userId);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                nnombre.setText(documentSnapshot.getString("fName"));
                ncargo.setText(documentSnapshot.getString("entidad"));
                ndirecccion.setText(documentSnapshot.getString("barrio"));
                nlocalidad.setText(documentSnapshot.getString("localidad"));

                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(profileImage); // Usar Picasso para cargar la imagen
                } else {
                    // Si no hay imagen subida, se puede mostrar una imagen por defecto
                    profileImage.setImageResource(R.drawable.mientras);
                }

                String direccion = documentSnapshot.getString("barrio");
                if (direccion != null && !direccion.isEmpty()) {
                    mostrarUbicacionEnMapa(direccion);
                }
            }
        });

        changeProfileImage.setOnClickListener(v -> {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(openGalleryIntent, 1000);
        });

        Places.initialize(getApplicationContext(), "AIzaSyAMp_LEifcWuwwHbWa99j8IMUN6MjFj_SQ");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ndirecccion.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(HomeProyectos.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        btnGenerarPropuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), registrodepropuestas.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("HomeProyectos", "Google Map is ready");
    }


    private void mostrarUbicacionEnMapa(String direccion) {
        if (mMap == null) {
            Log.e("HomeProyectos", "GoogleMap is not initialized");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(direccion, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación del Proyecto"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFireBase(imageUri);
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            String address = place.getAddress();
            LatLng latLng = place.getLatLng();
            if (address != null && latLng != null) {
                //ndirecccion.setText(address);

                // Mostrar la ubicación en el mapa
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación Seleccionada"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "Dirección o ubicación no disponible", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImageToFireBase(Uri imageUri) {
        // Crear referencia para la imagen en Firebase Storage
        StorageReference fileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");


        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {

                Picasso.get().load(uri).into(profileImage);


                DocumentReference documentReference = fStore.collection("registroProyectos").document(userId);
                documentReference.update("imageUrl", uri.toString()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeProyectos.this, "Imagen subida y URL guardada en Firestore", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(HomeProyectos.this, "Error al guardar la URL en Firestore", Toast.LENGTH_SHORT).show();
                });
            });
        }).addOnFailureListener(e -> Toast.makeText(HomeProyectos.this, "Falló la subida de la imagen", Toast.LENGTH_SHORT).show());
    }
}
