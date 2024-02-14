package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout espace1;
    LinearLayout espace2;
    LinearLayout espace3;

    EditText nom;
    EditText prenom;
    EditText email;
    EditText tel;
    EditText login;
    EditText password;
    TextView messageAuth;
    Button button_login;
    Button button_register;
    Button button_signup;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        espace1=(LinearLayout) findViewById(R.id.espace1);
        espace2=(LinearLayout)findViewById(R.id.espace2);
        espace3=(LinearLayout)findViewById(R.id.espace3);
        login=(EditText) findViewById(R.id.login);
        email=(EditText) findViewById(R.id.email);
        nom=(EditText) findViewById(R.id.nom);
        prenom=(EditText) findViewById(R.id.prenom);
        tel=(EditText) findViewById(R.id.tel);
        password=(EditText) findViewById(R.id.password);

//...
        button_login=(Button) findViewById(R.id.BttLogin);
        button_register=(Button) findViewById(R.id.BttRegister);
        button_signup=(Button) findViewById(R.id.BttsignUp);
        button_login.setOnClickListener(this);
        button_register.setOnClickListener(this);
        button_signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.BttLogin){
            System.out.println("############@");
            signin(login.getText().toString(),password.getText().toString());
        }

        else if (view.getId()==R.id.BttsignUp) {
            espace2.setVisibility(View.VISIBLE);
            espace3.setVisibility(View.GONE);
            espace1.setVisibility(View.GONE);
        }
        else if (view.getId()==R.id.BttRegister) {
          signup(email.getText().toString(),
                  generatePassword(),
                  nom.getText().toString(),
                  prenom.getText().toString(),
                  tel.getText().toString());
        }
    }

    private void signup(String email, String password, String nom, String prenom,
                        String tel) {
        System.out.println("********************");
        //Authentification
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

        //Stockage de l'utilisateur
        Map<String, Object> docUser = new HashMap<>();
        docUser.put("nom", nom);
        docUser.put("prenom", prenom);
        docUser.put("tel", tel);

// Add a new document with a generated ID
        db.collection("user").document(email)
                .set(docUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    String generatePassword(){
        return "12345678";
    }

    private void signin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
        else
            updateUI(null);
    }

    private void updateUI(FirebaseUser currentUser) {
    if (currentUser!=null){
        Intent MyIntent= new Intent(this, TasksActivity.class);
        MyIntent.putExtra("email", currentUser.getEmail());
        startActivity(MyIntent);

    }
    else{
        espace2.setVisibility(View.GONE);
        espace3.setVisibility(View.GONE);
        espace1.setVisibility(View.VISIBLE);
    }
    }
}