package com.example.cs401collaboration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs401collaboration.glide.GlideApp;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/**
 * @author Bryce Schooling
 */
public class ItemViewActivity extends AppCompatActivity
{
    /* Database */
    private DatabaseService mDB;

    /* Storage */
    StorageService mStorage = StorageService.getInstance();

    // UI elements
    private TextView itemDescription, itemLocation;
    private ImageView itemImage;
    private Toolbar itemTitle;
    private Button  btDelete, btQr, btChangeImage;

    // Current Item ID
    String itemID;

    // Log Tag
    private static final String TAG = "ItemViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        // Database
        mDB = DatabaseService.getInstance();

        // UI connections
        itemDescription = findViewById(R.id.item_description);
        itemLocation = findViewById(R.id.item_location);
        itemImage = findViewById(R.id.item_image);
        itemTitle = findViewById(R.id.item_title);
        btDelete = findViewById(R.id.bt_delete_item);
        btChangeImage = findViewById(R.id.bt_change_image);
        btQr = findViewById(R.id.bt_item_qr);


        // Delete on click handler
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the Warning Dialog
                AlertDialog.Builder deleteWarning = new AlertDialog.Builder(ItemViewActivity.this);

                // Set Messages for the Warning Dialog
                deleteWarning.setMessage(R.string.confirm_delete);
                deleteWarning.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // DB call for delete item
                        mDB.deleteItem(itemID, new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) { }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) { }
                        });
                        dialogInterface.dismiss();
                        ItemViewActivity.this.finish();
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

        // QR button on click listener
        btQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent qrViewIntent = new Intent(ItemViewActivity.this, QRViewActivity.class);
                String inputTitle = itemTitle.getTitle().toString();

                qrViewIntent.putExtra("qrTitle", inputTitle);
                qrViewIntent.putExtra("encodeString", itemID);

                startActivity(qrViewIntent);
            }
        });

        btChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resID = getIntent().getStringExtra("getImageResourceID");
                if (resID == null) resID = "placeholder.png";

                Intent imageIntent = new Intent(ItemViewActivity.this, QRScanActivity.class);
                imageIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
                imageIntent.putExtra("imageResourceID", resID);

                startActivityForResult(imageIntent, QRScanActivity.CAMERA_REQUEST);
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Intent intent = getIntent();

        // get the id of the item clicked
        itemID = intent.getStringExtra("entity_clicked_id");

        mDB.getItem(itemID, new OnSuccessListener<Item>() {
            @Override
            public void onSuccess(Item item) {
                // populate the ui with fields from current item
                itemDescription.setText(item.getDescription());
                itemLocation.setText(item.getLocation());
                itemTitle.setTitle(item.getName());
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText (
                        ItemViewActivity.this,
                        "Unable to retrieve Item",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Set Image
        String resourceID = intent.getStringExtra("getImageResourceID");
        if (resourceID == null) resourceID = "placeholder.png";
        StorageReference resourceSR =
                FirebaseStorage.getInstance().getReference().child(resourceID);

        GlideApp.with(ItemViewActivity.this)
                .load(resourceSR)
                .into(itemImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String resultString;

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.QR_REQUEST) {

                resultString = data.getStringExtra("ResultString");

                mDB.getCollection(resultString, new OnSuccessListener<Collection>() {
                    @Override
                    public void onSuccess(Collection collection) {
                        Intent entityIntent = new Intent(ItemViewActivity.this, ItemViewActivity.class);
                        entityIntent.putExtra("entity_clicked_id", resultString);

                        startActivity(entityIntent);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (Objects.equals(e.getMessage(), "UserInvalidPermissions"))
                        {
                            Toast.makeText(
                                    ItemViewActivity.this,
                                    "Not Authorized to Access Collection",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else if (Objects.equals(e.getMessage(), "NoCollectionFound"))
                        {
                            mDB.getItem(resultString, new OnSuccessListener<Item>() {
                                @Override
                                public void onSuccess(Item item) {
                                    Intent entityIntent = new Intent(ItemViewActivity.this, ItemViewActivity.class);
                                    entityIntent.putExtra("entity_clicked_id", resultString);

                                    startActivity(entityIntent);
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText (
                                            ItemViewActivity.this,
                                            "Unable to retrieve ScanQR result",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                        }
                    }
                });
            }

            if (requestCode == QRScanActivity.CAMERA_REQUEST) {
                Item updatedItem = new Item();
                String imageResourceID = data.getStringExtra("imageResourceID");

                mDB.getItem(itemID, new OnSuccessListener<Item>() {
                    @Override
                    public void onSuccess(Item item) {
                        updatedItem.copyOther(item);
                        updatedItem.setImageResourceID(imageResourceID);

                        mDB.updateItem(updatedItem, new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Toast.makeText (ItemViewActivity.this,
                                        "Update Item Successful", Toast.LENGTH_SHORT).show();
                                // Delete old image
                                if (!item.getImageResourceID().equals("placeholder.png"))
                                {
                                    mStorage.deleteResource(item.getImageResourceID(),
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        }, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d (TAG,
                                                        "Unable to delete orphaned image id="
                                                                + item.getImageResourceID()
                                                );
                                            }
                                        });
                                }
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText (ItemViewActivity.this,
                                        "Could not update Item", Toast.LENGTH_LONG).show();
                            }
                        });

                        String oldImageResourceID = item.getImageResourceID();

                        if
                        (
                            imageResourceID != null && !imageResourceID.equals(oldImageResourceID)
                        )
                        {
                            Log.d("ItemViewActivity", "newImageResourceID: " + imageResourceID);
                            Log.d("ItemViewActivity", "oldImageResourceID: " + oldImageResourceID);

                            StorageReference resourceSR =
                                    FirebaseStorage.getInstance().getReference().child(imageResourceID);

                            Log.d("ItemViewActivity", "ResourceSR: " + resourceSR);

                            GlideApp.with(ItemViewActivity.this)
                                    .load(resourceSR)
                                    .into(itemImage);
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ItemViewActivity.this,"Unable to retrieve Item",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}