package co.edu.unipiloto.proyectovotos.iniciodesesion;

import static co.edu.unipiloto.proyectovotos.guardar.GuardarProyecto.scheduleProjectTransfer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import co.edu.unipiloto.proyectovotos.Homes.HomeActivity;
import co.edu.unipiloto.proyectovotos.Homes.HomeAdmin;
import co.edu.unipiloto.proyectovotos.Homes.HomeProyectos;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.Registros.RegisterPlaneadores;
import co.edu.unipiloto.proyectovotos.Registros.RegisterVotante;
import co.edu.unipiloto.proyectovotos.decisor.GenerarExcel;
import co.edu.unipiloto.proyectovotos.guardar.GuardarProyecto;

public class Login extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mRLogin;
    TextView mRegisterBtn,forgotTextLink, mplanetbtn;
    FirebaseAuth fAuth;


    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long INTERVAL = 10000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRLogin = findViewById(R.id.Btnlogin);
        mRegisterBtn = findViewById(R.id.registerbtn);
        mplanetbtn = findViewById(R.id.planerbtn);
        forgotTextLink = findViewById(R.id.forgotPassword);

        fAuth = FirebaseAuth.getInstance();

        mRLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

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

                if (email.equals("decisor@decisor.com") && password.equals("admin123")) {
                    // Redirigir a Regenerar Excel
                    Toast.makeText(Login.this, "Inicio de sesión exitoso como administrador", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), GenerarExcel.class));
                    return;
                }

                // Autenticar el usuario
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = fAuth.getCurrentUser();
                            String userID = currentUser.getUid();


                            FirebaseFirestore fstore = FirebaseFirestore.getInstance();


                            DocumentReference userDoc = fstore.collection("users").document(userID);
                            userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            // El usuario está en "users", redirigir a HomeActivity
                                            Toast.makeText(Login.this, "Inicio de sesión exitoso como usuario", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class)); //se redirige a homeactivity
                                        } else {

                                            DocumentReference proyectosDoc = fstore.collection("registroProyectos").document(userID);
                                            proyectosDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot proyectoDoc = task.getResult();
                                                        if (proyectoDoc.exists()) {
                                                            // El usuario está en "registroProyectos", redirigir a HomeProyectos
                                                            Toast.makeText(Login.this, "Inicio de sesión exitoso como entidad de proyectos", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getApplicationContext(), HomeProyectos.class));
                                                        } else {
                                                            // No está en ninguna colección
                                                            Toast.makeText(Login.this, "Error: usuario no encontrado en ninguna categoría", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(Login.this, "Error al verificar en registroProyectos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(Login.this, "Error al verificar en users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(Login.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        scheduleProjectTransfer();

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterVotante.class));
            }
        });

        mplanetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterPlaneadores.class));
            }
        });

        forgotTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Restaurar Contraseña?");
                passwordResetDialog.setMessage("introduzca su correo electrónico para recibir el enlace de restablecimiento");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Login.this, "Enlace se a enviado a su correo electronico", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error usuario no encontrado" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void scheduleProjectTransfer() {
        runnable = new Runnable() {
            @Override
            public void run() {
                GuardarProyecto.scheduleProjectTransfer(Login.this);
                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.post(runnable);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

}