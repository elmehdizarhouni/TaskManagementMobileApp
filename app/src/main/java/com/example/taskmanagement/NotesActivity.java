package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SearchView;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import model.Tache;
import model.notes; // Importing the notes class

public class NotesActivity extends HomeActivity {

    FirebaseFirestore db;
    LinkedList<notes> notesList;
    RecyclerView myRecycler;

    private androidx.appcompat.widget.SearchView searchView;
    NotesAdapter myAdapter;

    FloatingActionButton addNote;
    private FirebaseAuth mAuth;
    private SearchView search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notes);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        notesList = new LinkedList<>();
        addNote = findViewById(R.id.fab_add_note);
        myRecycler = findViewById(R.id.recycler_notes);
        NotesAdapter notesAdapter = new NotesAdapter(notesList, NotesActivity.this);
        searchView = findViewById(R.id.search_view);
        myAdapter = new NotesAdapter(notesList,this);
        myRecycler.setAdapter(myAdapter);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when user submits query
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search when text in the SearchView changes
                performSearch(newText);
                return false;
            }
        });


        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddNotesActivity.class));
            }
        });
        getNotes();
        myRecycler.setAdapter(notesAdapter);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    void getNotes() {
        FirebaseUser user = mAuth.getCurrentUser();

        DocumentReference docRef = db.collection("user").document(user.getEmail());
        docRef.collection("Notes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                notes note = new notes(document.getString("title"), document.getString("description"), document.getId());
                                notesList.add(note);
                            }
                            myRecycler.setHasFixedSize(true);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(NotesActivity.this);
                            myRecycler.setLayoutManager(layoutManager);


                            NotesAdapter noteAdapter = new NotesAdapter(notesList, NotesActivity.this);
                            myRecycler.setAdapter(noteAdapter);
                        } else {
                            Log.d("document", "document");
                            Log.d("not ok", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void performSearch(String query) {
        FirebaseUser user = mAuth.getCurrentUser();
        Query searchQuery = db.collection("user").document(user.getEmail())
                .collection("Notes")
                .orderBy("title")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff");

        searchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Clear the existing list
                    notesList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Create Task object from Firestore document
                        notes note = new notes(document.getString("title"), document.getString("description"),  document.getId());
                        notesList.add(note); // Add new Task to the list
                    }

                    // Log the size of the updated list
                    Log.d(TAG, "Number of tasks after search: " + notesList.size());

                    // Notify the adapter that the data set has changed

                    myAdapter.notifyDataSetChanged();
                    displayFilteredTasks();

                } else {
                    Log.d("document", "document");
                    Log.d("not ok", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    private void displayFilteredTasks() {
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(NotesActivity.this);
        myRecycler.setLayoutManager(layoutManager);
        // specify an adapter
        NotesAdapter myAdapter = new NotesAdapter(notesList, NotesActivity.this);
        myRecycler.setAdapter(myAdapter);
    }
}
