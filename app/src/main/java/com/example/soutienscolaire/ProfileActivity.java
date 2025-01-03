package com.example.soutienscolaire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import model.User;

public class ProfileActivity extends HomeActivity {

    private ImageView profileImg;
    private TextView profileName;
    private TextView profileEmail;
    private TextView profileNumber;
    private TextView profileAddress;
    private TextView profilePrenom;
    private Button btnEditProfile;


    private FirebaseAuth mAuth;
    private ProgressBar profileLoading;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        Button btnEditProfile = findViewById(R.id.update);

        profileName = findViewById(R.id.profile_name);
        profilePrenom = findViewById(R.id.profile_prenom);
        profileNumber = findViewById(R.id.profile_number);
        profileLoading = findViewById(R.id.profile_loading);


        // Set the status bar color to purple
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Fetch user data

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        loadUserProfile();
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }
    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        profileLoading.setVisibility(View.VISIBLE);

        if (user != null) {
            db.collection("user").document(user.getEmail()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                profileLoading.setVisibility(View.GONE);
                                Log.d("herre", "im heere");
                                DocumentSnapshot document = task.getResult();
                                User user = new User(document.getString("nom"), document.getString("prenom"), document.getString("tel"));
                                profileName.setText(document.getString("nom"));
                                profilePrenom.setText(document.getString("prenom"));
                                profileNumber.setText(document.getString("tel"));
                                Log.d("nom", document.getString("nom"));
                            }
                        }
                    });
        }
    }

}
