package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import model.notes;

public class AddNotesActivity extends HomeActivity {
    EditText noteTitle;
    EditText noteDescription;
    Button addNote;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        noteTitle = findViewById(R.id.note_title);
        noteDescription = findViewById(R.id.note_description);
        addNote = findViewById(R.id.btn_add_note);

        addNote.setOnClickListener(this::onClick);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void onClick(View view) {
        if (view.getId() == R.id.btn_add_note) {
            addNote();
        }
    }

    private void addNote() {
        String title = noteTitle.getText().toString();
        String description = noteDescription.getText().toString();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Create a new note object
            notes note = new notes(title, description, null);

            // Add the note to Firestore
            db.collection("user").document(user.getEmail()).collection("Notes")
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        String noteId = documentReference.getId();

                        // Update the note ID with the Firestore generated ID
                        Map<String, Object> updatedNoteData = new HashMap<>();
                        updatedNoteData.put("id", noteId);

                        // Update the note data in Firestore
                        documentReference.update(updatedNoteData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Note data updated with ID: " + noteId);
                                    // Note added and data updated successfully
                                    // Finish the activity or show a success message
                                    startActivity(new Intent(getApplicationContext(), NotesActivity.class));
                                })
                                .addOnFailureListener(e -> {
                                    // Handle errors
                                    Log.e(TAG, "Error updating note data", e);
                                    // Show an error message to the user if necessary
                                });

                    })
                    .addOnFailureListener(e -> {
                        // Handle errors when adding note
                        Log.e(TAG, "Error adding note", e);
                        // Show an error message to the user if necessary
                    });
        }
    }
}
