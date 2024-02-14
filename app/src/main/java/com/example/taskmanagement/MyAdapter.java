package com.example.taskmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import model.Tache;


import java.util.LinkedList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private LinkedList<Tache> taches;
    private Context context;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(LinkedList<Tache> taches, Context context) {
        this.taches = new LinkedList<Tache>() ;
        this.taches.addAll(taches);
        this.context=context;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
// create a new view
        View itemLayoutView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item_layout,
                        parent, false);
        MyViewHolder vh = new MyViewHolder(itemLayoutView);
        return vh;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
        holder.title.setText(taches.get(position).getTitle());
    // Reference to an image file in Cloud Storage
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(taches.get(position).getImg());
    // Download directly from StorageReference using Glide
    // (See MyAppGlideModule for Loader registration)
        Glide.with(context /* context */)
                .load(storageReference)
                .into(holder.img);
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taches.size();
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView title;
        public ImageView img;
        // Context is a reference to the activity that contain the the recycler view
        public MyViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title =itemLayoutView.findViewById(R.id.title);
            img= itemLayoutView.findViewById(R.id.img);
            itemLayoutView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {

        }
    }
}
