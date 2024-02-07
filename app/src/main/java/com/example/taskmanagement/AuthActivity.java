package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout espace1;
    LinearLayout espace2;
    LinearLayout espace3;

    EditText nom;
    EditText prnom;
    EditText email;
    EditText tel;
    EditText login;
    EditText password;
    TextView messageAuth;
    Button button_login;
    Button button_register;
    Button button_signup;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();

        espace1=(LinearLayout) findViewById(R.id.espace1);
        espace2=(LinearLayout)findViewById(R.id.espace2);
        espace3=(LinearLayout)findViewById(R.id.espace3);
        email=(EditText) findViewById(R.id.email);
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

        if(view.getId()==R.id.BttLogin)
            signin(login.getText().toString(),password.getText().toString());
        else if (view.getId()==R.id.BttsignUp) {
            espace2.setVisibility(View.VISIBLE);
            espace3.setVisibility(View.GONE);
            espace1.setVisibility(View.GONE);
        }
        else if (view.getId()==R.id.BttRegister) {
          signup(email.getText().toString(),generatePassword());
        }
    }

    private void signup(String email, String password) {
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
        espace2.setVisibility(View.GONE);
        espace3.setVisibility(View.VISIBLE);
        espace1.setVisibility(View.GONE);
    }
    else{
        espace2.setVisibility(View.GONE);
        espace3.setVisibility(View.GONE);
        espace1.setVisibility(View.VISIBLE);
    }
    }
}