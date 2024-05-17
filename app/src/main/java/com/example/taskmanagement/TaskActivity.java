package com.example.taskmanagement;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import model.Tache;

public class TaskActivity extends HomeActivity {
    TextView titleTextView;
    TextView descriptionTextView;
    TextView deadlineTextView;
    ImageView imageView;
    String documentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

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


        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
                // Create an intent to start UpdateTaskActivity
                Intent updateIntent = new Intent(TaskActivity.this, UpdateTaskActivity.class);
                // Pass the ID of the task to update to UpdateTaskActivity
                updateIntent.putExtra("taskId", documentId);
                // Start the activity UpdateTaskActivity
                startActivity(updateIntent);
            }
        });

    }
}