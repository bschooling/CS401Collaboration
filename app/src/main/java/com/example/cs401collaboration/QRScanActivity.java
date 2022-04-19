package com.example.cs401collaboration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

import java.util.List;

/**
 * QRScanActivity class manages things relating to scanning a QR code
 * @author Nguyen Nam Pham
 */
public class QRScanActivity extends AppCompatActivity {

    // Class variables

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
        ).build();

        barcodeScanner = BarcodeScanning.getClient(options);
        resultText = (TextView) findViewById(R.id.result_text);
        image = (ImageView) findViewById(R.id.imageView);
    }

    /**
     * takeImage takes an image from the camera activity
     * @param view is a View object
     */
    public void takeImage(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (checkPermission(Manifest.permission.CAMERA))
            startActivityForResult(cameraIntent, 200); // Deprecated!

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
     * @param requestCode is the request code to the camera activity
     * @param resultCode is the result code from the camera activity
     * @param data is the Intent object that contains the result
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap takenPhoto;
        InputImage qrImage;

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 200) {
                takenPhoto = (Bitmap) data.getExtras().get("data");

                image.setImageBitmap(takenPhoto);
                qrImage = InputImage.fromBitmap(takenPhoto, 0);

                barcodeScanner.process(qrImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    /**
                     * onSuccess process the result when the process method is successful
                     * @param barcodes is the List of Barcodes generated by the process method
                     */
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
                                } else {
                                    resultText.setText(R.string.invalid_coding_string);
                                }
                            }
                        }

                        else {
                            resultText.setText(R.string.no_qr_text);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    /**
                     * onFailure gives the error when the process method fails
                     * @param except is an Exception from the process method
                     */
                    @Override
                    public void onFailure(@NonNull Exception except) {
                        Log.e("Invii_QRScan", "QR Scan Error occurred: " + except.getMessage());
                    }
                });

            }
        }
    }

}