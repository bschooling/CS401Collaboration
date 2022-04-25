package com.example.cs401collaboration.Adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
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
    private Collection currCollection;
    private ArrayList<User> CollaboratorArrayList;
    private DatabaseService mDB;
    private String currentUserID;
    private Boolean isOwner;

    /** Initialize the data for the adapter
     * Collection ArrayList holds the data to populate views with */
    public CollaboratorRvAdapter(Context context, Collection currCollection, ArrayList<User> CollaboratorArrayList, String currentUserID, Boolean isOwner) {
        this.context = context;
        this.currCollection = currCollection;
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
        User user = CollaboratorArrayList.get(holder.getAdapterPosition());

        // Populate UI
        holder.tvCollaboratorName.setText(user.getName());
        holder.tvCollaboratorEmail.setText(user.getEmail());

        if (!isOwner) {
            if (!user.getUid().equals(currentUserID)){
                holder.btDelete.setVisibility(View.GONE);
            }
        }

        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder deleteWarning = new AlertDialog.Builder(context);

                deleteWarning.setMessage(R.string.confirm_delete);
                deleteWarning.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDB.removeCollab(currCollection, CollaboratorArrayList.get(holder.getAdapterPosition()).getUid(), new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {


                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                        // Remove from ArrayList and notify RV
                        CollaboratorArrayList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        notifyItemRangeChanged(holder.getAdapterPosition(), CollaboratorArrayList.size());

                        dialogInterface.dismiss();
                    }

                });
                deleteWarning.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog warning = deleteWarning.create();
                warning.show();
            }
        });
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
            btDelete = collaboratorView.findViewById(R.id.collaborator_delete);

        }
    }
}
