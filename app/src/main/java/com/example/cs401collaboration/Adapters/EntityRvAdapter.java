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
import com.example.cs401collaboration.StorageService;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Adapter class for the Entity recyclerview
 *
 * @author Bryce Schooling
 */
public class EntityRvAdapter extends RecyclerView.Adapter<EntityRvAdapter.Viewholder> {

    private Context context;
    private ArrayList<Entity> entityArrayList;
    private DatabaseService mDB;
    private StorageService mStorage = StorageService.getInstance();

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
        mDB = DatabaseService.getInstance();

        return new Viewholder(view);
    }

    /** Replace the default contents of a view with data from collections */
    @Override
    public void onBindViewHolder(@NonNull EntityRvAdapter.Viewholder holder, int position) {
        // Get Entity
        Entity entity = entityArrayList.get(position);

        // Populating UI
        holder.entityFirstLine.setText(entity.getFirstLine());
        holder.entitySecondLine.setText(entity.getSecondLine());

        // Set Image
        mStorage.downloadResource(entity.getImageResourceID(), new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                holder.entityImage.setImageBitmap(mStorage.toBitmap(bytes));
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        // Setting Label
        if (entity.getType().equals(Entity.TYPE_COLLECTION)) {
            if (entity.extras.containsKey("isOwned") && entity.extras.get("isOwned").equals("true"))
            {
                holder.entityLabel.setText(R.string.collection);
                holder.entityLabel.setBackgroundResource(R.color.violet);
            }
            else
            {
                holder.entityLabel.setText("Shared");
                holder.entityLabel.setBackgroundResource(R.color.mint_green);
            }
        } else if (entity.getType().equals(Entity.TYPE_ITEM)) {
            holder.entityLabel.setText(R.string.item);
            holder.entityLabel.setBackgroundResource(R.color.mint_green);
        } else {
            holder.entityLabel.setText(R.string.unknown_type);
        }

        // Card onClick
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

        // Card onLongClick
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Creating the Dialog
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.edit_dialogue, null);

                final AlertDialog editDialog = new AlertDialog.Builder(context).create();

                // UI elements for Dialog
                Button btDelete = (Button) dialogView.findViewById(R.id.dialogDelete);
                Button btConfirm = (Button) dialogView.findViewById(R.id.dialogConfirm);
                Button btCancel = (Button) dialogView.findViewById(R.id.dialogCancel);
                EditText editName = (EditText) dialogView.findViewById(R.id.editName);
                EditText editLocation = (EditText) dialogView.findViewById(R.id.editLocation);
                EditText editDescription = (EditText) dialogView.findViewById((R.id.editDescription));

                Collection updatedCollection = new Collection();
                Item updatedItem = new Item();

                // Checks for if Collection or Item
                if (entity.getType().equals(Entity.TYPE_COLLECTION)) {
                    mDB.getCollection(entity.getDocID(), new OnSuccessListener<Collection>() {
                        @Override
                        public void onSuccess(Collection collection) {
                            // Populates fields with existing data
                            editName.setText(collection.getName());
                            editLocation.setText(collection.getLocation());
                            editDescription.setText(collection.getDescription());

                            // Copy collection
                            updatedCollection.copyOther(collection);

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText (
                                    context,
                                    "Cant Find Collection Data",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                } else if (entity.getType().equals(Entity.TYPE_ITEM)) {
                    mDB.getItem(entity.getDocID(), new OnSuccessListener<Item>() {
                        @Override
                        public void onSuccess(Item item) {
                            // Populates fields with existing data
                            editName.setText(item.getName());
                            editLocation.setText(item.getLocation());
                            editDescription.setText(item.getDescription());

                            // Copy Item
                            updatedItem.copyOther(item);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText (
                                    context,
                                    "Cant Find Item Data",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                }

                // Closes dialog on Cancel press
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editDialog.dismiss();
                    }
                });

                // Saves changes on Confirm press
                btConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check for Collection or Item
                        if (entity.getType().equals(Entity.TYPE_COLLECTION)) {
                            // Assign values to updated collection
                            updatedCollection.setName(editName.getText().toString());
                            updatedCollection.setLocation(editLocation.getText().toString());
                            updatedCollection.setDescription(editDescription.getText().toString());

                            // Update Collection
                            mDB.updateCollection(updatedCollection, new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    // Update RV and close dialog
                                    entity.setFirstLine(updatedCollection.getName());
                                    entity.setSecondLine((updatedCollection.getLocation()));
                                    notifyItemChanged(holder.getAdapterPosition());
                                    editDialog.dismiss();
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText (
                                            context,
                                            "Could not update Collection",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                        } else if (entity.getType().equals(Entity.TYPE_ITEM)) {
                            // Assign values to updated Item
                            updatedItem.setName(editName.getText().toString());
                            updatedItem.setLocation(editLocation.getText().toString());
                            updatedItem.setDescription(editDescription.getText().toString());

                            // Update Item
                            mDB.updateItem(updatedItem, new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    // Update RV and close dialog
                                    entity.setFirstLine(updatedItem.getName());
                                    entity.setSecondLine((updatedItem.getLocation()));
                                    notifyItemChanged(holder.getAdapterPosition());
                                    editDialog.dismiss();
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText (
                                            context,
                                            "Could not update Item",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                        }
                    }
                });

                btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder deleteWarning = new AlertDialog.Builder(context);

                        deleteWarning.setMessage(R.string.confirm_delete);
                        deleteWarning.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Check for Collection or Item
                                if (entity.getType().equals(Entity.TYPE_COLLECTION)) {
                                    // TODO Delete collection
                                    // DB call for delete Collection
                                    mDB.deleteCollection(entity.getDocID(), new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {

                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                } else if (entity.getType().equals(Entity.TYPE_ITEM)) {
                                    // DB call for delete item
                                    mDB.deleteItem(entity.getDocID(), new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) { }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) { }
                                    });

                                }

                                // Remove from ArrayList and notify RV
                                entityArrayList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyItemRangeChanged(holder.getAdapterPosition(), entityArrayList.size());

                                dialogInterface.dismiss();
                                editDialog.dismiss();
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

                editDialog.setView(dialogView);
                editDialog.show();

                return true;
            }
        });
    }

    /** gets number of views to create */
    @Override
    public int getItemCount() {return entityArrayList.size();}

    /** Reference to the view being used */
    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView entityFirstLine, entitySecondLine, entityLabel;
        private ImageView entityImage;


        public Viewholder (@NonNull View itemView) {
            super(itemView);
            entityFirstLine = itemView.findViewById(R.id.card_field_top);
            entitySecondLine = itemView.findViewById(R.id.card_field_bottom);
            entityLabel = itemView.findViewById(R.id.typeLabel);
            entityImage = itemView.findViewById(R.id.card_field_image);
        }
    }
}
