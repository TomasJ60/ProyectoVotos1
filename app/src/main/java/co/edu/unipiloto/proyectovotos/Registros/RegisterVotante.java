package co.edu.unipiloto.proyectovotos.Registros;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipiloto.proyectovotos.Homes.HomeActivity;
import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;
import co.edu.unipiloto.proyectovotos.R;


public class RegisterVotante extends AppCompatActivity {

    EditText mFullname, mEmail, mPassword, mPhone, mlocalidad, mbarrio;
    Button mRegisterbtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    private RadioGroup radioGroupgenero;
    FirebaseFirestore fstore;
    String userID;
    public static final String TAG = "TAG";

    private TextInputLayout addressLayoutvotos;
    private TextInputEditText addressInputvotos;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_votante);

        mFullname = findViewById(R.id.fullname);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mlocalidad = findViewById(R.id.localidad);
        mbarrio = findViewById(R.id.barrio);  // Inicializar el campo mbarrio
        mRegisterbtn = findViewById(R.id.Btnlogin);
        mLoginBtn = findViewById(R.id.registerbtn);
        radioGroupgenero = findViewById(R.id.radioGroup);

        // Inicializa Google Places
        Places.initialize(getApplicationContext(), "AIzaSyAMp_LEifcWuwwHbWa99j8IMUN6MjFj_SQ");
        addressLayoutvotos = findViewById(R.id.textInputLayoutBarrio);
        addressInputvotos = findViewById(R.id.barrio);

        // Configura el autocompletado de direcciones
        addressInputvotos.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(RegisterVotante.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }

        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String fullname = mFullname.getText().toString();
                String phone = mPhone.getText().toString();
                String localidad = mlocalidad.getText().toString();
                String barrio = mbarrio.getText().toString();

                int selectedGenderId = radioGroupgenero.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedGenderId);
                String genero = selectedRadioButton.getText().toString();

                // Validaciones de los campos
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Se requiere un email >:(");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Se requiere una contraseña >:(");
                    return;
                }
                if (password.length() < 6) {
                    mPassword.setError("Se requiere una contraseña mínima de 6 caracteres");
                    return;
                }

                // Registro de usuario
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //enviar verificacion de email
                            FirebaseUser veremail = fAuth.getCurrentUser();
                            veremail.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });



                            Toast.makeText(RegisterVotante.this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = fAuth.getCurrentUser();
                            if (user != null) {
                                userID = user.getUid();
                            }

                            // Subir datos del usuario a Firestore
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("fName", fullname);
                            userMap.put("email", email);
                            userMap.put("phone", phone);
                            userMap.put("localidad", localidad);
                            userMap.put("barrio", barrio);
                            userMap.put("genero", genero);

                            documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Datos de usuario subidos a Firestore con UID: " + userID);
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error guardando en Firestore: " + e.getMessage());
                                    Toast.makeText(RegisterVotante.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "Error en la creación de usuario: " + task.getException().getMessage());
                            Toast.makeText(RegisterVotante.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            String address = place.getAddress();
            addressInputvotos.setText(address);
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
