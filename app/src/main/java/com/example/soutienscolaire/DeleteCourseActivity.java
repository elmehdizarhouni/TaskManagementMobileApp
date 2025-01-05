package com.example.soutienscolaire;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeleteCourseActivity extends AppCompatActivity {
    private static FirebaseAuth mAuth;

    /**
     * Affiche une boîte de dialogue de confirmation pour supprimer un cours.
     *
     * @param context    Contexte de l'activité ou du fragment appelant.
     * @param documentId ID du document représentant le cours dans Firestore.
     */
    public static void showDeleteConfirmationDialog(Context context, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Appelle la méthode pour supprimer le cours
                        deleteCourse(context, documentId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Supprime un cours de la collection Firestore.
     *
     * @param context    Contexte de l'activité ou du fragment appelant.
     * @param documentId ID du document représentant le cours dans Firestore.
     */
    public static void deleteCourse(final Context context, final String documentId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            firestore.collection("user").document(user.getEmail())
                    .collection("Courses") // Remplacé "Tasks" par "Courses"
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Cours supprimé avec succès
                            Toast.makeText(context, "Course deleted successfully", Toast.LENGTH_SHORT).show();

                            // Retour à la page des cours
                            Intent intent = new Intent(context, CoursesActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Échec de la suppression
                            Toast.makeText(context, "Failed to delete course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "No authenticated user found!", Toast.LENGTH_SHORT).show();
        }
    }
}
