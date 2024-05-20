package com.example.taskmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nomEditText;

    private EditText prenomEditText;
    private EditText emailEditText;
    private EditText telEditText;
    private EditText passwordEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nomEditText = findViewById(R.id.nom);
        prenomEditText = findViewById(R.id.prenom);
        emailEditText = findViewById(R.id.email);
        telEditText = findViewById(R.id.phone);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.BttRegister);

        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.BttRegister) {
            registerUser();
        }
    }

    private void registerUser() {
        final String nom = nomEditText.getText().toString().trim();
        final String prenom = prenomEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String tel = telEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserToFirestore( nom, prenom, email, tel);
                        } else {
                            Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String nom, String prenom, String email, String tel) {
        Map<String, Object> user = new HashMap<>();
        user.put("nom", nom);
        user.put("prenom", prenom);
        user.put("tel", tel);

        db.collection("user").document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("RegisterActivity", "User document successfully written!");
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        finish(); // Finishing the activity after successful registration
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("RegisterActivity", "Error writing document", e);
                        Toast.makeText(RegisterActivity.this, "Error writing document", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
