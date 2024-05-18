package com.example.taskmanagement;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import model.Tache;

import java.util.LinkedList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>  {
    private LinkedList<Tache> taches; // List to hold tasks
    private Context context; // Context of the activity
    private LinkedList<Tache> filteredTaches; // List to hold filtered tasks

    // Constructor to initialize the adapter with tasks and context
    public MyAdapter(LinkedList<Tache> taches, Context context) {
        this.taches = new LinkedList<Tache>() ;
        this.filteredTaches = new LinkedList<>(taches);
        this.taches.addAll(taches);
        this.context=context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for a task item
        View itemLayoutView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item_layout,
                        parent, false);
        // Create a ViewHolder for the task item view
        MyViewHolder vh = new MyViewHolder(itemLayoutView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Set the title of the task to the TextView in the ViewHolder
        holder.title.setText(filteredTaches.get(position).getTitle());
        // Load the image of the task into the ImageView using Glide library
        Glide.with(context /* context */)
                .load(filteredTaches.get(position).getImg())
                .into(holder.img);
        // Set click listener for the task item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When an item is clicked, create an intent to start TaskDetailsActivity
                Intent intent = new Intent(context, TaskActivity.class);
                // Put the selected task as an extra in the intent
                intent.putExtra("task", filteredTaches.get(position));
                // Start the activity
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taches.size();
    }

    // Provide a reference to the views for each data item
    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView title;
        public ImageView img;

        // Constructor to initialize the views in the ViewHolder
        public MyViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title =itemLayoutView.findViewById(R.id.title);
            img= itemLayoutView.findViewById(R.id.img);
            itemLayoutView.setOnClickListener(this);
        }

        // Handle click events on the task item
        @Override
        public void onClick(View v) {
            // Not implemented in this example
        }
    }
}
