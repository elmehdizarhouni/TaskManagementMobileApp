package com.example.soutienscolaire;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddCourseActivity extends HomeActivity {
    EditText subject;
    EditText description;
    EditText date;
    EditText teacher;
    Button addCourse;
    Button selectImage;
    ImageView imageView;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ProgressBar profileLoading;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course); // Assurez-vous que le fichier XML est correctement renommé
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des champs
        subject = findViewById(R.id.subject);
        description = findViewById(R.id.description);
        date = findViewById(R.id.date);
        teacher = findViewById(R.id.teacher);
        addCourse = findViewById(R.id.btnAddCourse);
        selectImage = findViewById(R.id.btnSelectImage);
        profileLoading = findViewById(R.id.profile_loading);
        imageView = findViewById(R.id.imageView);

        addCourse.setOnClickListener(this::onClick);
        selectImage.setOnClickListener(this::onSelectImage);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageView.setImageURI(uri);
            }
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void onSelectImage(View view) {
        pickImageLauncher.launch("image/*");
    }

    private void uploadImage() {
        if (imageView.getDrawable() != null) {
            StorageReference storageRef = storage.getReference();
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("images/" + imageName);
            Uri imageUri = getImageUri(imageView);

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                addCourseWithImage(uri.toString());
            }).addOnFailureListener(e -> Log.e(TAG, "Error getting download URL", e))).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
            });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
        File cacheDirectory = getCacheDir();
        File imageFile = new File(cacheDirectory, imageName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(imageFile);
    }

    private void addCourseWithImage(String imageUrl) {
        Map<String, Object> courseMap = new HashMap<>();
        courseMap.put("subject", subject.getText().toString());
        courseMap.put("description", description.getText().toString());
        courseMap.put("date", date.getText().toString());
        courseMap.put("teacher", teacher.getText().toString());
        courseMap.put("img", imageUrl);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            db.collection("user").document(user.getEmail()).collection("Courses")
                    .add(courseMap)
                    .addOnSuccessListener(documentReference -> {
                        String courseId = documentReference.getId();
                        profileLoading.setVisibility(View.VISIBLE);

                        Map<String, Object> updatedCourseMap = new HashMap<>(courseMap);
                        updatedCourseMap.put("id", courseId);

                        documentReference.set(updatedCourseMap)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Course data updated with ID: " + courseId);
                                    profileLoading.setVisibility(View.GONE);
                                    startActivity(new Intent(getApplicationContext(), CoursesActivity.class));
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating course data", e));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding course", e));
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnAddCourse) {
            uploadImage();
        }
    }
}
