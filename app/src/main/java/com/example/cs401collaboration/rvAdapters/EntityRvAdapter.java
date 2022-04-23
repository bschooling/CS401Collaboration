package com.example.cs401collaboration.rvAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.CollectionViewActivity;
import com.example.cs401collaboration.ItemViewActivity;
import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.Entity;

import java.util.ArrayList;

/** Adapter class for the recyclerview */
public class EntityRvAdapter extends RecyclerView.Adapter<EntityRvAdapter.Viewholder> {

    private Context context;
    private ArrayList<Entity> entityArrayList;

    /** Initialize the data for the adapter
     * Collection ArrayList holds the data to populate views with */
    public EntityRvAdapter(Context context, ArrayList<Entity> entityArrayList) {
        this.context = context;
        this.entityArrayList = entityArrayList;
    }


    /** create the new views */
    @NonNull
    @Override
    public EntityRvAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate (R.layout.collection_card, parent, false);

        return new Viewholder(view);
    }

    /** Replace the default contents of a view with data from collections */
    @Override
    public void onBindViewHolder(@NonNull EntityRvAdapter.Viewholder holder, int position) {
        Entity entity = entityArrayList.get(position);
        holder.entityFirstLine.setText(entity.getFirstLine());
        holder.entitySecondLine.setText(entity.getSecondLine());
        //TODO swap hardcoded image for image from collection
        holder.entityImage.setImageResource(android.R.drawable.ic_menu_gallery);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (entity.getType().equals(Entity.TYPE_COLLECTION)) {
                    /*  Creates new activity for the collection */
                    Intent intentCollection = new Intent(context, CollectionViewActivity.class);
                    intentCollection.putExtra("entity_clicked_id", entity.getDocID());
                    context.startActivity(intentCollection);
                } else if (entity.getType().equals(Entity.TYPE_ITEM)) {
                    /* Creates new activity for the Item */
                    Intent intentItem = new Intent(context, ItemViewActivity.class);
                    intentItem.putExtra("entity_clicked_id", entity.getDocID());
                    context.startActivity(intentItem);
                } else {
                    Toast.makeText (
                            context,
                            "Could not find entity clicked",
                            Toast.LENGTH_LONG
                    ).show();
                }

            }
        });
    }

    /** gets number of views to create */
    @Override
    public int getItemCount() {return entityArrayList.size();}

    /** Reference to the view being used */
    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView entityFirstLine, entitySecondLine;
        private ImageView entityImage;


        public Viewholder (@NonNull View itemView) {
            super(itemView);
            entityFirstLine = itemView.findViewById(R.id.card_field_top);
            entitySecondLine = itemView.findViewById(R.id.card_field_bottom);
            entityImage = itemView.findViewById(R.id.card_field_image);
        }
    }
}
