package com.example.cs401collaboration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    public static final int GALLERY_REQUEST = 100;

    /**
     * CAMERA_REQUEST is a request code to the onActivityResult method to use the Camera
     */
    public static final int CAMERA_REQUEST = 200;

    /**
     * CAMERA_QR_REQUEST is a request code to the onActivityResult method to use the Camera for processing the QR image
     */
    public static final int CAMERA_QR_REQUEST = 300;

    /**
     * RESULT_DENIED is a result code from this activity that indicates the requested permission was denied
     */
    public static final int RESULT_DENIED = 2;

    /**
     * LOG_TAG is a Tag String with the app name and activity name
     */
    private final String LOG_TAG = "QRScanActivity";

    // Instance variables

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


    // Instance methods

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
            /**
             * onClick takes the image from the camera activity
             * @param view is a View object
             */
            @Override
            public void onClick(View view) {
                takeQRImage(view);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick takes the image from the Gallery activity
             * @param view is a View object
             */
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

    }

    /**
     * onStart starts the activity
     */
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        int requestCode = intent.getIntExtra("RequestCode", 0);

        if (requestCode == CAMERA_REQUEST) {
            Log.d(LOG_TAG, "CAMERA_REQUEST code");

            String cameraPermission = Manifest.permission.CAMERA;
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (checkPermission(cameraPermission)) {
                startActivityForResult(cameraIntent, requestCode); // Deprecated!
            }

            else {
                // When requesting permission from CAMERA_REQUEST, it will bring up QRScan Activity with dialog
                ActivityCompat.requestPermissions(QRScanActivity.this, new String[] {cameraPermission}, requestCode);
            }
        }

        else if (requestCode == CAMERA_QR_REQUEST) {
            Log.d(LOG_TAG, "CAMERA_QR_REQUEST code");
        }
    }

    /**
     * selectImage selects an image from the Gallery
     * @param view is a View object
     */
    public void selectImage(View view) {
        String galleryPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        Toast deniedToast = Toast.makeText(this, R.string.gallery_permission_denied, Toast.LENGTH_LONG);

        galleryIntent.setType("image/*");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermission(galleryPermission))
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);

            else
                deniedToast.show();
        }
    }

    /**
     * takeQRImage takes an image from the camera activity
     * @param view is a View object
     */
    public void takeQRImage(View view) {
        String cameraPermission = Manifest.permission.CAMERA;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (checkPermission(cameraPermission))
            startActivityForResult(cameraIntent, CAMERA_QR_REQUEST); // Deprecated!

        else
            ActivityCompat.requestPermissions(QRScanActivity.this, new String[] { cameraPermission }, CAMERA_QR_REQUEST);
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
     * onRequestPermissionsResult processes the result of the permission request dialog box
     * @param requestCode is an integer specifying the request code
     * @param permissions is an Array of Permission Strings to request
     * @param permissionResults in an Array of Permission Results from permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] permissionResults) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Toast deniedToast = Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG);

        super.onRequestPermissionsResult(requestCode, permissions, permissionResults);

        if (requestCode == CAMERA_QR_REQUEST) {
            if (permissionResults.length > 0 && permissionResults[0] == PackageManager.PERMISSION_GRANTED) {
                resultText.setText(R.string.defaultResult);
                startActivityForResult(cameraIntent, requestCode); // Deprecated!
            }

            else {
                // resultText.setText(R.string.camera_permission_denied);
                deniedToast.show();
            }
        }

        else if (requestCode == CAMERA_REQUEST) {
            if (permissionResults.length > 0 && permissionResults[0] == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(cameraIntent, requestCode); // Deprecated!

            // Some way to notify the caller when permission is denied
            else {
                setResult(RESULT_DENIED);
                finish(); // Required because no need to process CAMERA_REQUEST result
            }
        }
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
        InputImage qrImage;

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG, "ResultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_QR_REQUEST) {
                photo = (Bitmap) data.getExtras().get("data");

                image.setImageBitmap(photo);
                qrImage = InputImage.fromBitmap(photo, 0);

                processImage(qrImage);
            }

            else if (requestCode == CAMERA_REQUEST) {
                Log.d(LOG_TAG, "Giving CAMERA_REQUEST Result");

                Log.d(LOG_TAG, "Receiving Camera Result");
                Log.d(LOG_TAG, "ResultCode: " + resultCode);
                Log.d(LOG_TAG, "RequestCode: " + requestCode);
                // Log.d(LOG_TAG, "Data available: " + data.getExtras().isEmpty());

                setResult(RESULT_OK, data);
                finish();
            }

            else if (requestCode == GALLERY_REQUEST) {
                imageUri = data.getData();

                if (imageUri != null) {
                    image.setImageURI(imageUri);

                    try {
                        qrImage = InputImage.fromFilePath(this, imageUri);
                        processImage(qrImage);
                    }

                    catch (IOException ioException) {
                        Log.e(LOG_TAG, R.string.ioException_string + ": " + ioException.getMessage());
                    }
                }
            }
        }

        else if (resultCode == RESULT_CANCELED) {
            if (requestCode == CAMERA_REQUEST)
                finish();
        }
    }

    /**
     * processImage process the InputImage into a List of Barcodes and display the result
     * @param inputImage is an InputImage to process
     */
    public void processImage(InputImage inputImage) {
        if (inputImage != null) {
            barcodeScanner.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                /**
                 * onSuccess gives the List of Barcodes when the process is successful
                 *
                 * @param barcodes is a List of Barcodes
                 */
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    int valueType;

                    if (barcodes != null && barcodes.size() > 0) {
                        for (Barcode barcode : barcodes) { // The last QR code recognized is displayed
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
                    } else {
                        resultText.setText(R.string.no_qr_text);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                /**
                 * onFailure prints a message when barcodeScanner fails to process the image
                 *
                 * @param except is an Exception thrown by barcodeScanner
                 */
                @Override
                public void onFailure(@NonNull Exception except) {
                    //resultText.setText(R.string.no_qr_text);
                    Log.e(LOG_TAG, "Failed to generate QR Code: " + except.getMessage());
                }
            });
        }
    }

}