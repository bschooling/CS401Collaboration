package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * @author Bryce Schooling
 */
public class ItemViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* Storage */
    StorageService mStorage = StorageService.getInstance();

    // UI elements
    private TextView itemDescription, itemLocation;
    private ImageView itemImage;
    private Toolbar itemTitle;
    private Button  btDelete, btQr;

    // Current Item ID
    String itemID;

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
        btQr = findViewById(R.id.bt_item_qr);


        // Delete on click handler
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder deleteWarning = new AlertDialog.Builder(ItemViewActivity.this);

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
                Intent qrViewIntent = new Intent(getBaseContext(), QRViewActivity.class);
                String inputTitle = itemTitle.getTitle().toString();

                qrViewIntent.putExtra("qrTitle", inputTitle);

                startActivity(qrViewIntent);
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Intent intent = getIntent();

        itemID = intent.getStringExtra("entity_clicked_id");

        mDB.getItem(itemID, new OnSuccessListener<Item>() {
            @Override
            public void onSuccess(Item item) {
                itemDescription.setText(item.getDescription());
                itemLocation.setText(item.getLocation());
                // Set Image
                mStorage.downloadResource(item.getImageResourceID(), new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        itemImage.setImageBitmap(mStorage.toBitmap(bytes));
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
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
    }
}