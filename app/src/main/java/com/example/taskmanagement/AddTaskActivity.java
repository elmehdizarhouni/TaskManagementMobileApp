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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends HomeActivity {
    EditText task;
    EditText description;
    EditText deadline;
    Button add_task;
    Button select_image;
    ImageView imageView;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<String> pickImageLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        task =(EditText) findViewById(R.id.task);
        description=(EditText) findViewById(R.id.description);
        deadline=(EditText) findViewById(R.id.deadline);
        add_task = (Button) findViewById(R.id.btnAdd);
        select_image = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        add_task.setOnClickListener(this::onClick);
        select_image.setOnClickListener(this::onSelectImage);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the result here
                    if (uri != null) {
                        // Do something with the selected image URI
                        // For example, display the image in an ImageView
                        imageView.setImageURI(uri);
                    }
                });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    public void onSelectImage(View view) {
        // Intent to pick an image from gallery
        pickImageLauncher.launch("image/*");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Set the selected image to ImageView
            Uri imageUri = data.getData();
        }
    }
    // Upload image to Firebase Cloud Storage
    private void uploadImage() {
        if (imageView.getDrawable() != null) {
            // Get reference to Firebase Storage
            StorageReference storageRef = storage.getReference();

            // Generate a random image name
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";

            // Get reference to the image path in Cloud Storage
            StorageReference imageRef = storageRef.child("images/" + imageName);

            // Get the selected image URI
            Uri imageUri = getImageUri(imageView);

            // Upload image to Cloud Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Listen for upload success/failure
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, get download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Image download URL obtained, now add task details with image URL to Firestore
                    addTaskWithImage(uri.toString());
                }).addOnFailureListener(e -> {
                    // Handle error
                    Log.e(TAG, "Error getting download URL", e);
                });
            }).addOnFailureListener(e -> {
                // Handle unsuccessful upload
                Log.e(TAG, "Error uploading image", e);
            });
        } else {
            // No image selected
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri getImageUri(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        // Generate a random image name
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";

        // Get reference to the cache directory
        File cacheDirectory = getCacheDir();

        // Create a temporary file to store the image
        File imageFile = new File(cacheDirectory, imageName);

        try {
            // Write the bitmap data to the file
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the file URI of the image
        return Uri.fromFile(imageFile);
    }

    private void addTaskWithImage(String imageUrl) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", task.getText().toString());
        taskMap.put("description", description.getText().toString());
        taskMap.put("deadline", deadline.getText().toString());
        taskMap.put("img", imageUrl); // Add image URL to task details
        taskMap.put("id", imageUrl);
        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();

        // Add task details to Firestore
        // Add task details to Firestore
        if (user != null) {
            db.collection("user").document(user.getEmail()).collection("Tasks")
                    .add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        String taskId = documentReference.getId();

                        // Update task ID with Firestore generated ID
                        Map<String, Object> updatedTaskMap = new HashMap<>(taskMap);
                        updatedTaskMap.put("id", taskId);

                        // Update task data in Firestore
                        documentReference.set(updatedTaskMap)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Task data updated with ID: " + taskId);
                                    // Task added and data updated successfully
                                    // Finish the activity or show a success message
                                    startActivity(new Intent(getApplicationContext(), TasksActivity.class));
                                })
                                .addOnFailureListener(e -> {
                                    // Handle errors
                                    Log.e(TAG, "Error updating task data", e);
                                    // Show an error message to the user if necessary
                                });

                    })
                    .addOnFailureListener(e -> {
                        // Handle errors when adding task
                        Log.e(TAG, "Error adding task", e);
                        // Show an error message to the user if necessary
                    });
        }

    }
    public void onClick(View view) {
        if (view.getId() == R.id.btnAdd) {
            // Upload image to Firebase Cloud Storage
            uploadImage();
        }
    }



}