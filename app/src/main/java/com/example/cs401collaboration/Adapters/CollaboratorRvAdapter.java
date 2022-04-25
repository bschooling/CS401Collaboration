package com.example.cs401collaboration.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.User;

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
    private String currentUserID;
    private Boolean isOwner;

    /** Initialize the data for the adapter
     * Collection ArrayList holds the data to populate views with */
    public CollaboratorRvAdapter(Context context, ArrayList<User> CollaboratorArrayList, String currentUserID, Boolean isOwner) {
        this.context = context;
        this.CollaboratorArrayList = CollaboratorArrayList;
        this.currentUserID = currentUserID;
        this.isOwner = isOwner;
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
        holder.tvCollaboratorName.setText(user.getName());
        holder.tvCollaboratorEmail.setText(user.getEmail());

        if (!isOwner) {
            if (!user.getUid().equals(currentUserID)){
                holder.btDelete.setVisibility(View.GONE);
            }
        }
    }

    /** gets number of views to create */
    @Override
    public int getItemCount() {return CollaboratorArrayList.size();}

    /** Reference to the view being used */
    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView tvCollaboratorName, tvCollaboratorEmail;
        private Button btDelete;

        public Viewholder (@NonNull View collaboratorView) {
            super(collaboratorView);
            tvCollaboratorName = collaboratorView.findViewById(R.id.collaborator_name);
            tvCollaboratorEmail = collaboratorView.findViewById(R.id.collaborator_email);
            //TODO Add functionality to Delete button
            btDelete = collaboratorView.findViewById(R.id.collaborator_delete);
        }
    }
}
