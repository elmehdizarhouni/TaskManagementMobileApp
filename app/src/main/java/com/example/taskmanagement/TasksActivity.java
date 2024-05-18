package com.example.taskmanagement;

import static android.content.ContentValues.TAG;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.Query;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import model.Tache;

public class TasksActivity extends HomeActivity {

    FirebaseFirestore db;
    LinkedList<Tache> taches;
    RecyclerView myRecycler;
    MyAdapter myAdapter;
    FloatingActionButton addTask;
    private FirebaseAuth mAuth;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the activity_tasks layout
        setContentView(R.layout.activity_tasks);

        // Set the status bar color to purple
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize the list of tasks and other UI elements
        taches = new LinkedList<Tache>();
        addTask = (FloatingActionButton) findViewById(R.id.fab_add);
        myRecycler = findViewById(R.id.recycler_tasks);
        myAdapter = new MyAdapter(taches,this);
        myRecycler.setAdapter(myAdapter);

        // Initialize the SearchView
        searchView = findViewById(R.id.search_view);

        // Set a listener for text changes in the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        // Set click listener for the addTask FloatingActionButton
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start AddTaskActivity when FloatingActionButton is clicked
                startActivity(new Intent(getApplicationContext(), AddTaskActivity.class));
            }
        });

        // Fetch tasks from Firestore
        getTasks();

        // Set up navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Fetch tasks from Firestore
    void getTasks() {
        FirebaseUser user = mAuth.getCurrentUser();

        DocumentReference docRef = db.collection("user").document(user.getEmail());
        docRef.collection("Tasks").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Create Task object from Firestore document
                                Tache tache = new Tache(document.getString("title"), document.getString("description"), document.getString("deadline"), document.getString("img"), document.getId());
                                taches.add(tache); // Add Task to the list
                            }
                            myRecycler.setHasFixedSize(true);
                            // use a linear layout manager
                            LinearLayoutManager layoutManager = new LinearLayoutManager(TasksActivity.this);
                            myRecycler.setLayoutManager(layoutManager);
                            // specify an adapter
                            MyAdapter myAdapter = new MyAdapter(taches, TasksActivity.this);
                            myRecycler.setAdapter(myAdapter);
                        } else {
                            Log.d("document", "document");
                            Log.d("not ok", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Perform search in Firestore
    private void performSearch(String query) {
        FirebaseUser user = mAuth.getCurrentUser();
        Query searchQuery = db.collection("user").document(user.getEmail())
                .collection("Tasks")
                .orderBy("title")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff");

        searchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Clear the existing list
                    taches.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Create Task object from Firestore document
                        Tache tache = new Tache(document.getString("title"), document.getString("description"), document.getString("deadline"), document.getString("img"), document.getId());
                        taches.add(tache); // Add new Task to the list
                    }

                    // Log the size of the updated list
                    Log.d(TAG, "Number of tasks after search: " + taches.size());

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(TasksActivity.this);
        myRecycler.setLayoutManager(layoutManager);
        // specify an adapter
        MyAdapter myAdapter = new MyAdapter(taches, TasksActivity.this);
        myRecycler.setAdapter(myAdapter);
    }
}
