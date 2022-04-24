package com.example.cs401collaboration.Adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.CollectionViewActivity;
import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.ItemViewActivity;
import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.example.cs401collaboration.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Adapter class for the Collaborator recyclerview
 *
 * @author Bryce Schooling
 */
public class CollaboratorRvAdapter extends RecyclerView.Adapter<CollaboratorRvAdapter.Viewholder> {

    private Context context;
    private ArrayList<User> CollaboratorArrayList;
    private DatabaseService mDB;

    /** Initialize the data for the adapter
     * Collection ArrayList holds the data to populate views with */
    public CollaboratorRvAdapter(Context context, ArrayList<User> CollaboratorArrayList) {
        this.context = context;
        this.CollaboratorArrayList = CollaboratorArrayList;
    }


    /** create the new views */
    @NonNull
    @Override
    public CollaboratorRvAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate (R.layout.collaborator_card, parent, false);
        mDB = DatabaseService.getInstance();

        return new CollaboratorRvAdapter.Viewholder(view);
    }

    /** Replace the default contents of a view with data from collections */
    @Override
    public void onBindViewHolder(@NonNull CollaboratorRvAdapter.Viewholder holder, int position) {
        // Get User
        User user = CollaboratorArrayList.get(position);

        // Populate UI
    }

    /** gets number of views to create */
    @Override
    public int getItemCount() {return CollaboratorArrayList.size();}

    /** Reference to the view being used */
    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView tvCollaboratorName;
        private Button btDelete;

        public Viewholder (@NonNull View collaboratorView) {
            super(collaboratorView);
            tvCollaboratorName = collaboratorView.findViewById(R.id.collaborator_name);
            btDelete = collaboratorView.findViewById(R.id.collaborator_delete);
        }
    }
}
