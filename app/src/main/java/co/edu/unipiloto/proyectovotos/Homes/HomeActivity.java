package co.edu.unipiloto.proyectovotos.Homes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.propuestas.VerPropuestasPorTipoDeProyecto;
import co.edu.unipiloto.proyectovotos.propuestas.VerPropuestasdelocalidades;
import co.edu.unipiloto.proyectovotos.votos.puestosDeVotacion;

public class HomeActivity extends AppCompatActivity {

    private Button mlogout,button, mverpropuestas;
    private Button mverPropuestasPorTipo;

    TextView fullname, email, phone, localidad, barrio;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        fullname = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        phone = findViewById(R.id.profilePhone);
        localidad = findViewById(R.id.profileLocalidad);
        barrio = findViewById(R.id.profileBarrio);

        mverpropuestas = findViewById(R.id.buttonVerPropuestas);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                fullname.setText(documentSnapshot.getString("fName"));
                email.setText(documentSnapshot.getString("email"));
                phone.setText(documentSnapshot.getString("phone"));
                localidad.setText(documentSnapshot.getString("localidad"));
                barrio.setText(documentSnapshot.getString("barrio"));
            }
        });

        button = findViewById(R.id.button);
        mlogout = findViewById(R.id.logout);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad Login cuando el botón sea presionado
                startActivity(new Intent(HomeActivity.this, Login.class));
            }
        });

        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, Login.class));
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        mverpropuestas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, VerPropuestasdelocalidades.class));
            }
        });
        mverPropuestasPorTipo = findViewById(R.id.buttonVerPropuestasPorTipo);

        mverPropuestasPorTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, VerPropuestasPorTipoDeProyecto.class));
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}