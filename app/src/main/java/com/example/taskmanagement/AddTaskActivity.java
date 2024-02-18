package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    EditText task;
    EditText description;
    EditText deadline;
    Button add_task;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        db = FirebaseFirestore.getInstance();
        task =(EditText) findViewById(R.id.task);
        description=(EditText) findViewById(R.id.description);
        deadline=(EditText) findViewById(R.id.deadline);
        add_task = (Button) findViewById(R.id.btnAdd);
        add_task.setOnClickListener(this::onClick);
    }
    public void onClick(View view) {

        if(view.getId()==R.id.btnAdd){
            Map<String, Object> taskk= new HashMap<>();
            taskk.put("title", task.getText().toString());
            taskk.put("description", description.getText().toString());
            taskk.put("deadline", deadline.getText().toString());
            db.collection("Tasks").add(taskk)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });

        }


    }
}