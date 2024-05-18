package com.example.taskmanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class DeleteNotesActivity extends AppCompatActivity{
    private static FirebaseAuth mAuth;

    public static void showDeleteConfirmationDialog(Context context, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform task deletion
                        deleteNote(context, documentId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void deleteNote(final Context context, final String documentId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        firestore.collection("user").document(user.getEmail())
                .collection("Notes")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task deleted successfully
                        Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();

                        // Return to tasks page
                        Intent intent = new Intent(context, NotesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle deletion failure
                        Toast.makeText(context, "Failed to delete Note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
