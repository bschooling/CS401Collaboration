package com.example.cs401collaboration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;

/**
 * QRScanActivity class manages things relating to scanning a QR code
 * @author Nguyen Nam Pham
 */
public class QRScanActivity extends AppCompatActivity {

    // Class variables

    /**
     * GALLERY_REQUEST is a request code to the onActivityResult method to use the Gallery
     */
    private final int GALLERY_REQUEST = 100;

    /**
     * CAMERA_REQUEST is a request code to the onActivityResult method to use the Camera
     */
    private final int CAMERA_REQUEST = 200;

    /**
     * LOG_TAG is a Tag String with the app name and activity name
     */
    private final String LOG_TAG = "Invii_QRScan";


    /**
     * resultText is the TextView that displays the barcodeScanner result
     */
    private TextView resultText;

    /**
     * image is the image object
     */
    private ImageView image;

    /**
     * barcodeScanner is a BarcodeScanner object that handles scanning the QR code and displaying the result
     */
    private BarcodeScanner barcodeScanner;

    // Class methods

    /**
     * onCreate handles the creation of QRScanActivity
     * @param savedInstanceState is a saved state of an instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button cameraButton;
        Button selectButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
        ).build();

        barcodeScanner = BarcodeScanning.getClient(options);
        resultText = (TextView) findViewById(R.id.result_text);
        image = (ImageView) findViewById(R.id.qr_scan_image);
        cameraButton = (Button) findViewById(R.id.camera_button);
        selectButton = (Button) findViewById(R.id.select_image_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            // Take the image from the camera activity
            @Override
            public void onClick(View view) {
                takeImage(view);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);
            }
        });
    }

    /**
     * takeImage takes an image from the camera activity
     * @param view is a View object
     */
    public void takeImage(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (checkPermission(Manifest.permission.CAMERA))
            startActivityForResult(cameraIntent, CAMERA_REQUEST); // Deprecated!

        else {
            resultText.setText(R.string.camera_permission_denied);
        }
    }

    /**
     * checkPermission checks the permission to use a restricted activity
     * @param permission is the permission String typically in Manifest.permission
     * @return a boolean whether the checked permission is granted
     */
    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * onActivityResult process the result from the takeImage method
     * @param requestCode is the request code to the activity
     * @param resultCode is the result code from the activity
     * @param data is the Intent object that contains the result
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri imageUri;
        Bitmap photo;
        InputImage qrImage = null;

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                photo = (Bitmap) data.getExtras().get("data");

                image.setImageBitmap(photo);
                qrImage = InputImage.fromBitmap(photo, 0);
            }

            else if (requestCode == GALLERY_REQUEST) {
                imageUri = data.getData();

                if (imageUri != null) {
                    image.setImageURI(imageUri);

                    try {
                        qrImage = InputImage.fromFilePath(this, imageUri);
                    }

                    catch (IOException ioException) {
                        Log.e(LOG_TAG, R.string.ioException_string + ": " + ioException.getMessage());
                    }
                }
            }

            if (qrImage != null) {
                barcodeScanner.process(qrImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        int valueType;

                        if (barcodes != null && barcodes.size() > 0) {
                            for (Barcode barcode : barcodes) {
                                int scanFormat = barcode.getFormat();

                                if (scanFormat == Barcode.FORMAT_QR_CODE) {
                                    valueType = barcode.getValueType();

                                    if (valueType == Barcode.TYPE_TEXT) {
                                        resultText.setText(barcode.getDisplayValue());
                                    }
                                }

                                else {
                                    resultText.setText(R.string.invalid_coding_string);
                                }
                            }
                        }

                        else {
                            resultText.setText(R.string.no_qr_text);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception except) {
                        //resultText.setText(R.string.no_qr_text);
                        Log.e(LOG_TAG, "Failed to generate QR Code: " + except.getMessage());
                    }
                });
            }
        }
    }

}