package co.edu.unipiloto.proyectovotos.Registros;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipiloto.proyectovotos.Homes.HomeActivity;
import co.edu.unipiloto.proyectovotos.Homes.HomeProyectos;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.propuestas.registrodepropuestas;

public class RegisterPlaneadores extends AppCompatActivity {

        //REGISTRO
        private EditText pfullname, pemail, ppassword, pphone;
        private RadioGroup genderGroup;
        private RadioButton radioGub, radioPriv;
        private Button pRegisterbtn,btnGenerarPropuesta;
        private Spinner userTypeSpinner;

        FirebaseAuth fAuth;
        FirebaseFirestore fstore;
        String userIDp;
        public static final String TAGp = "TAG";

        private TextInputLayout addressLayout;
        private TextInputEditText addressInput; // Usamos TextInputEditText
        private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_proyectos);

            pfullname = findViewById(R.id.fullnameP);
            pemail = findViewById(R.id.email);
            ppassword = findViewById(R.id.password);
            pphone = findViewById(R.id.phone);
            genderGroup = findViewById(R.id.genderGroup);
            pRegisterbtn = findViewById(R.id.registerplannerbtn);





            // Spinner para localidades de bogota
            userTypeSpinner = findViewById(R.id.spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.localidades, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userTypeSpinner.setAdapter(adapter);


            genderGroup = findViewById(R.id.genderGroup);
            // Inicializa Google Places
            Places.initialize(getApplicationContext(), "AIzaSyAMp_LEifcWuwwHbWa99j8IMUN6MjFj_SQ");

            // Referencia al campo de dirección
            addressLayout = findViewById(R.id.direccion_layout);
            addressInput = findViewById(R.id.direccion);

            // Configura el autocompletado de direcciones
            addressInput.setOnClickListener(v -> {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(RegisterPlaneadores.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            });

            fAuth = FirebaseAuth.getInstance();
            fstore = FirebaseFirestore.getInstance();
            if (fAuth.getCurrentUser() != null){
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }

            pRegisterbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = pemail.getText().toString().trim();
                    String password = ppassword.getText().toString().trim();
                    String fullname = pfullname.getText().toString();
                    String phone = pphone.getText().toString();
                    String localidad = userTypeSpinner.getSelectedItem().toString();
                    String direccion = addressInput.getText().toString();

                    //seleccion de la entidad
                    int selectedGenderId = genderGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedGenderId);
                    String entidad = selectedRadioButton.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        pemail.setError("Se requiere un email");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        ppassword.setError("Se requiere una contraseña");
                        return;
                    }
                    if (password.length() < 6) {
                        ppassword.setError("Contraseña mínima de 6 caracteres");
                        return;
                    }

                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterPlaneadores.this, "Usuario creado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), HomeProyectos.class));
                                userIDp = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fstore.collection("registroProyectos").document(userIDp);
                                Map<String, Object> user = new HashMap<>();
                                user.put("fName", fullname);
                                user.put("email", email);
                                user.put("phone", phone);
                                user.put("localidad", localidad);
                                user.put("barrio", direccion);
                                user.put("entidad", entidad);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAGp, "Usuario creado: " + userIDp);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAGp, "Error guardando en Firestore: " + e.getMessage());
                                    }
                                });
                                startActivity(new Intent(getApplicationContext(), HomeProyectos.class));
                            } else {
                                Log.e(TAGp, "Error en la creación de usuario: " + task.getException().getMessage());
                                Toast.makeText(RegisterPlaneadores.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String address = place.getAddress();
                addressInput.setText(address);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

