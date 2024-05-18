package com.example.taskmanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;

import model.notes;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>  {
    private LinkedList<notes> notesList;
    private LinkedList<notes> filteredNotes;
    private Context context;

    public NotesAdapter(LinkedList<notes> notesList, Context context) {
        this.notesList = new LinkedList<>();


        this.filteredNotes = new LinkedList<>(notesList);
        this.notesList.addAll(notesList);
        this.context = context;
    }


    @Override
    public int getItemCount() {
        return filteredNotes.size(); // Utilisez filteredNotes.size() pour obtenir le nombre d'éléments affichés
    }



    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item_layout, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        notes currentNote = filteredNotes.get(position);
        holder.title.setText(currentNote.getTitle());
        holder.description.setText(currentNote.getDescription());

        // Configure the delete button
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show delete confirmation dialog
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNoteFromFirestore(currentNote);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }



    private void deleteNoteFromFirestore(notes note) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("user").document(user.getEmail())
                    .collection("Notes")
                    .document(note.getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Remove the note from the list and notify the adapter
                            notesList.remove(note);
                            filteredNotes.remove(note);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to delete Note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public Button deleteButton;

        public NoteViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            description = itemView.findViewById(R.id.note_description);
            deleteButton = itemView.findViewById(R.id.deletebutton);
        }
    }

}
