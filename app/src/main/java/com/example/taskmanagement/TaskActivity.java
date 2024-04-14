package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import model.Tache;

public class TaskActivity extends AppCompatActivity {
    TextView titleTextView;
    TextView descriptionTextView;
    TextView deadlineTextView;
    ImageView imageView;
    String documentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Get the selected task from the intent
        Intent intent = getIntent();
        Tache selectedTask = (Tache) intent.getSerializableExtra("task");
        documentId = selectedTask.getId();

        // Display the details of the selected task
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        deadlineTextView = (TextView) findViewById(R.id.deadlineTextView);
        imageView = (ImageView) findViewById(R.id.imageView);

        titleTextView.setText(selectedTask.getTitle());
        descriptionTextView.setText(selectedTask.getDescription());
        deadlineTextView.setText(selectedTask.getDeadline());

        // Load the image from Firebase Storage using Glide
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(selectedTask.getImg());
        Glide.with(this)
                .load(storageReference)
                .into(imageView);

        FloatingActionButton deleteButton = findViewById(R.id.deletebutton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show delete confirmation dialog
                DeleteTaskActivity.showDeleteConfirmationDialog(TaskActivity.this, documentId);
            }
        });
        FloatingActionButton updateButton = findViewById(R.id.updatebutton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Créer un intent pour démarrer UpdateTaskActivity
                Intent updateIntent = new Intent(TaskActivity.this, UpdateTaskActivity.class);
                // Passer l'ID de la tâche à mettre à jour à UpdateTaskActivity
                updateIntent.putExtra("taskId", documentId);
                // Démarrer l'activité UpdateTaskActivity
                startActivity(updateIntent);
            }
        });

    }
}