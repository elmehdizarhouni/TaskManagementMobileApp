package com.example.soutienscolaire;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.LinkedList;

import model.Course;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final LinkedList<Course> filteredCourses; // List to hold filtered courses
    private final Context context; // Context of the activity

    // Constructor to initialize the adapter with courses and context
    public MyAdapter(LinkedList<Course> courses, Context context) {
        this.filteredCourses = new LinkedList<>(courses);
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a course item
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_item_layout, parent, false);
        return new MyViewHolder(itemLayoutView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Course currentCourse = filteredCourses.get(position);

        // Set the subject of the course to the TextView in the ViewHolder
        holder.subject.setText(currentCourse.getSubject());

        // Load the image of the course into the ImageView using Glide library
        Glide.with(context)
                .load(currentCourse.getImg())
                .into(holder.img);

        // Set click listener for the course item
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, CourseActivity.class);
                intent.putExtra("course", filteredCourses.get(adapterPosition));
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredCourses.size();
    }

    // Provide a reference to the views for each data item
    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView subject;
        public ImageView img;

        // Constructor to initialize the views in the ViewHolder
        public MyViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            subject = itemLayoutView.findViewById(R.id.subject);
            img = itemLayoutView.findViewById(R.id.img);
            itemLayoutView.setOnClickListener(this);
        }

        // Handle click events on the task item
        @Override
        public void onClick(View v) {
            // Not implemented in this example
        }
    }
}
