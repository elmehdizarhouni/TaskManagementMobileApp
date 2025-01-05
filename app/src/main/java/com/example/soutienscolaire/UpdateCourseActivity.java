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
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UpdateCourseActivity extends HomeActivity {
    EditText subject;
    EditText description;
    EditText date;
    EditText teacher;
    Button updateCourse;
    Button selectImage;
    ImageView imageView;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<String> pickImageLauncher;
    private String courseId; // ID du cours à mettre à jour
    private static final int COURSE_UPDATED_RESULT_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_course);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        subject = findViewById(R.id.subject);
        description = findViewById(R.id.description);
        date = findViewById(R.id.date);
        teacher = findViewById(R.id.teacher);
        updateCourse = findViewById(R.id.btnUpdateCourse);
        selectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);

        // Récupérer l'ID du cours à mettre à jour depuis l'intent
        courseId = getIntent().getStringExtra("courseId");

        updateCourse.setOnClickListener(this::onClick);
        selectImage.setOnClickListener(this::onSelectImage);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageView.setImageURI(uri);
            }
        });

        // Charger les détails du cours depuis Firestore
        loadCourseDetails();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void loadCourseDetails() {
        // Référence au document du cours dans Firestore
        DocumentReference courseRef = db.collection("user").document(getCurrentUserEmail()).collection("Courses").document(courseId);

        // Récupérer les données du cours
        courseRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Cours trouvé, récupérer les données
                String courseSubject = documentSnapshot.getString("subject");
                String courseDescription = documentSnapshot.getString("description");
                String courseDate = documentSnapshot.getString("date");
                String courseTeacher = documentSnapshot.getString("teacher");
                String courseImageUrl = documentSnapshot.getString("img");

                // Afficher les données dans les champs de saisie
                subject.setText(courseSubject);
                description.setText(courseDescription);
                date.setText(courseDate);
                teacher.setText(courseTeacher);
                Picasso.get().load(courseImageUrl).into(imageView);
            } else {
                Toast.makeText(this, "Erreur : Cours non trouvé", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erreur lors de la récupération des détails du cours", e);
            Toast.makeText(this, "Erreur lors de la récupération des détails du cours", Toast.LENGTH_SHORT).show();
        });
    }

    public void onSelectImage(View view) {
        pickImageLauncher.launch("image/*");
    }

    private void updateImage(String subjectText, String descriptionText, String dateText, String teacherText) {
        if (imageView.getDrawable() != null) {
            StorageReference storageRef = storage.getReference();
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("images/" + imageName);

            Uri imageUri = getImageUri(imageView);

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateCourseWithImage(uri.toString(), subjectText, descriptionText, dateText, teacherText);
                }).addOnFailureListener(e -> Log.e(TAG, "Erreur lors de l'obtention de l'URL de téléchargement", e));
            }).addOnFailureListener(e -> Log.e(TAG, "Erreur lors du téléchargement de l'image", e));
        } else {
            Toast.makeText(this, "Veuillez sélectionner une image", Toast.LENGTH_SHORT).show();
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

    private void updateCourseWithImage(String imageUrl, String subjectText, String descriptionText, String dateText, String teacherText) {
        db.collection("user").document(getCurrentUserEmail()).collection("Courses").document(courseId)
                .update("img", imageUrl,
                        "subject", subjectText,
                        "description", descriptionText,
                        "date", dateText,
                        "teacher", teacherText)
                .addOnSuccessListener(aVoid -> {
                    setResult(COURSE_UPDATED_RESULT_CODE);
                    Intent intent = new Intent(UpdateCourseActivity.this, CoursesActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la mise à jour du cours", e));
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnUpdateCourse) {
            String subjectText = subject.getText().toString();
            String descriptionText = description.getText().toString();
            String dateText = date.getText().toString();
            String teacherText = teacher.getText().toString();
            updateImage(subjectText, descriptionText, dateText, teacherText);
        }
    }

    private String getCurrentUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }
}
