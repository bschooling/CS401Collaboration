package com.example.cs401collaboration.rvAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.Collection;

import java.util.ArrayList;

/** Adapter class for the recyclerview */
public class CollectionRvAdapter extends RecyclerView.Adapter<CollectionRvAdapter.Viewholder> {

    private Context context;
    private ArrayList<Collection> collectionArrayList;

    /** Initialize the data for the adapter
     * Collection ArrayList holds the data to populate views with */
    public CollectionRvAdapter(Context context, ArrayList<Collection> collectionArrayList) {
        this.context = context;
        this.collectionArrayList = collectionArrayList;
    }


    /** create the new views */
    @NonNull
    @Override
    public CollectionRvAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate (R.layout.collection_card, parent, false);
        return new Viewholder(view);
    }

    /** Replace the default contents of a view with data from collections */
    @Override
    public void onBindViewHolder(@NonNull CollectionRvAdapter.Viewholder holder, int position) {
        Collection collection = collectionArrayList.get(position);
        holder.collectionName.setText(collection.getName());
        holder.collectionLocation.setText(collection.getLocation());
        //TODO override image
    }

    /** gets number of views to create */
    @Override
    public int getItemCount() {return collectionArrayList.size();}

    /** Reference to the view being used */
    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView collectionName, collectionLocation;
        //TODO add image

        public Viewholder (@NonNull View itemView) {
            super(itemView);
            collectionName = itemView.findViewById(R.id.collection_name);
            collectionLocation = itemView.findViewById(R.id.collection_item_location);
            //TODO add bind for image
        }
    }


}
