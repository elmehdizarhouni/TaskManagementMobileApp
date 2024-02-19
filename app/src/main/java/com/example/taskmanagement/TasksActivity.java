package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import model.Tache;

public class TasksActivity extends AppCompatActivity {

    FirebaseFirestore db;
    LinkedList<Tache> taches;
    RecyclerView myRecycler;
    EditText search;
    FloatingActionButton addTask;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        search = (EditText) findViewById(R.id.hint);
        taches = new LinkedList<Tache>();
        addTask = (FloatingActionButton) findViewById(R.id.fab_add);
        myRecycler = findViewById(R.id.recycler_tasks);
        MyAdapter myAdapter = new MyAdapter(taches, TasksActivity.this);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                myAdapter.getFilter().filter(s); // Apply filter as user types
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text has changed
                // We don't need to perform any action here, so leave it empty
            }
        });
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddTaskActivity.class));
            }
        });
        getTasks();
        // Set adapter to RecyclerView after initialization
        myRecycler.setAdapter(myAdapter);

    }


    void getTasks() {
        FirebaseUser user = mAuth.getCurrentUser();

        DocumentReference docRef = db.collection("user").document(user.getEmail());
        docRef.collection("Tasks").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Tache tache = new Tache(document.getString("title"), document.getString("description"), document.getString("deadline"), document.getString("img"));
                                taches.add(tache);

                            }
                            myRecycler.setHasFixedSize(true);
                            // use a linear layout manager
                            LinearLayoutManager layoutManager = new LinearLayoutManager(TasksActivity.this);
                            myRecycler.setLayoutManager(layoutManager);
                            // specify an adapter (see also next example)
                            MyAdapter myAdapter = new MyAdapter(taches, TasksActivity.this);
                            myRecycler.setAdapter(myAdapter);
                        } else {
                            Log.d("document", "document");
                            Log.d("not ok", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}