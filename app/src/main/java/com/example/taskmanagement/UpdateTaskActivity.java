package com.example.taskmanagement;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class UpdateTaskActivity extends AppCompatActivity {
    EditText task;
    EditText description;
    EditText deadline;
    Button update_task;
    Button select_image;
    ImageView imageView;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<String> pickImageLauncher;
    private String taskId; // ID de la tâche à mettre à jour

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_task);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        task = (EditText)findViewById(R.id.task);
        description = (EditText) findViewById(R.id.description);
        deadline = (EditText) findViewById(R.id.deadline);
        update_task =  (Button) findViewById(R.id.btnUpdate);
        select_image = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);

        // Récupérer l'ID de la tâche à mettre à jour depuis l'intent
        taskId = getIntent().getStringExtra("taskId");

        update_task.setOnClickListener(this::onClick);
        select_image.setOnClickListener(this::onSelectImage);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Gérer le résultat ici
                    if (uri != null) {
                        // Faire quelque chose avec l'URI de l'image sélectionnée
                        // Par exemple, afficher l'image dans un ImageView
                        imageView.setImageURI(uri);
                    }
                });

        // Charger les détails de la tâche depuis Firestore
        loadTaskDetails();
    }

    private void loadTaskDetails() {
        // Référence au document de la tâche dans Firestore
        DocumentReference taskRef = db.collection("user").document(getCurrentUserEmail()).collection("Tasks").document(taskId);

        // Récupérer les données de la tâche
        taskRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Tâche trouvée, récupérer les données
                String taskName = documentSnapshot.getString("title");
                String taskDescription = documentSnapshot.getString("description");
                String taskDeadline = documentSnapshot.getString("deadline");
                String taskImageUrl = documentSnapshot.getString("img");

                // Afficher les données dans les champs de saisie
                task.setText(taskName);
                description.setText(taskDescription);
                deadline.setText(taskDeadline);
                Picasso.get().load(taskImageUrl).into(imageView);
            } else {
                // La tâche n'existe pas ou il y a eu une erreur lors de la récupération des données
                Toast.makeText(this, "Erreur: Tâche non trouvée", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Gérer les erreurs de récupération des données
            Log.e(TAG, "Erreur lors de la récupération des détails de la tâche", e);
            Toast.makeText(this, "Erreur lors de la récupération des détails de la tâche", Toast.LENGTH_SHORT).show();
        });
    }

    public void onSelectImage(View view) {
        // Intent pour sélectionner une image depuis la galerie
        pickImageLauncher.launch("image/*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Définir l'image sélectionnée dans ImageView
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    // Mettre à jour l'image de la tâche dans Firebase Cloud Storage
    private void updateImage() {
        if (imageView.getDrawable() != null) {
            // Obtenir une référence vers Firebase Storage
            StorageReference storageRef = storage.getReference();

            // Générer un nom d'image aléatoire
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";

            // Obtenir une référence vers le chemin de l'image dans Cloud Storage
            StorageReference imageRef = storageRef.child("images/" + imageName);

            // Obtenir l'URI de l'image sélectionnée
            Uri imageUri = getImageUri(imageView);

            // Mettre à jour l'image dans Cloud Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Écouter le succès/échec du téléchargement
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image téléchargée avec succès, obtenir l'URL de téléchargement
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // URL de téléchargement de l'image obtenue, maintenant mettre à jour les détails de la tâche avec l'URL de l'image dans Firestore
                    updateTaskWithImage(uri.toString());
                }).addOnFailureListener(e -> {
                    // Gérer l'erreur
                    Log.e(TAG, "Erreur lors de l'obtention de l'URL de téléchargement", e);
                });
            }).addOnFailureListener(e -> {
                // Gérer l'échec du téléchargement
                Log.e(TAG, "Erreur lors du téléchargement de l'image", e);
            });
        } else {
            // Aucune image sélectionnée
            Toast.makeText(this, "Veuillez sélectionner une image", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        // Générer un nom d'image aléatoire
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";

        // Obtenir une référence vers le répertoire cache
        File cacheDirectory = getCacheDir();

        // Créer un fichier temporaire pour stocker l'image
        File imageFile = new File(cacheDirectory, imageName);

        try {
            // Écrire les données bitmap dans le fichier
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retourner l'URI du fichier image
        return Uri.fromFile(imageFile);
    }

    private void updateTaskWithImage(String imageUrl) {
        // Mettre à jour les détails de la tâche avec l'URL de l'image dans Firestore
        db.collection("user").document(getCurrentUserEmail()).collection("Tasks").document(taskId)
                .update("img", imageUrl )
                .addOnSuccessListener(aVoid -> {
                    // Succès de la mise à jour
                    Log.d(TAG, "Image de la tâche mise à jour avec succès");
                    // Finir l'activité ou afficher un message de succès
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Gérer les erreurs
                    Log.e(TAG, "Erreur lors de la mise à jour de l'image de la tâche", e);
                    // Afficher un message d'erreur si nécessaire
                });
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnUpdate) {
            // Mettre à jour l'image de la tâche dans Firebase Cloud Storage
            updateImage();
        }
    }

    // Obtenir l'email de l'utilisateur connecté
    private String getCurrentUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }
}
