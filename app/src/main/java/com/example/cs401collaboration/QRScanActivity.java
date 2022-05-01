package com.example.cs401collaboration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * QRScanActivity class manages things relating to scanning a QR code
 * @author Nguyen Nam Pham
 */
public class QRScanActivity extends AppCompatActivity {

    // Class constants

    /**
     * GALLERY_REQUEST is a request code to the onActivityResult method to use the Gallery
     */
    public static final int GALLERY_REQUEST = 100;

    /**
     * CAMERA_REQUEST is a request code to the onActivityResult method to use the Camera
     */
    public static final int CAMERA_REQUEST = 200;

    /**
     * QR_REQUEST is a request code to the onActivityResult method to use the Camera for processing the QR image
     */
    public static final int QR_REQUEST = 300;

    /**
     * RESULT_DENIED is a result code from this activity that indicates the requested permission was denied
     */
    public static final int RESULT_DENIED = 2;

    /**
     * RESULT_FAILED is a result code from this activity that indicates an exception has occurred
     */
    public static final int RESULT_FAILED = 3;

    // Instance constants

    /**
     * LOG_TAG is a Tag String with the app name and activity name
     */
    private final String LOG_TAG = "QRScanActivity";

    /**
     * WRITE_PERMISSION holds the permission to read and write to the phone's storage
     */
    private final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    // Instance variables

    /**
     * activityRequest hold the request code of the calling activity
     */
    private int activityRequest;

    /**
     *
     */
    private int returnCode;

    /**
     *
     */
    private String resultFilePath;

    /**
     *
     */
    private Intent returnIntent;

    /**
     * resultLabel is a TextView that displays the word "Result"
     */
    private TextView resultLabel;

    /**
     * resultText is the TextView that displays the barcodeScanner result
     */
    private TextView resultText;

    /**
     * image is the image object
     */
    private ImageView image;

    /**
     * saveImageButton is a Button that saves the image when its onClick is triggered
     */
    private Button saveImageButton;

    /**
     * barcodeScanner is a BarcodeScanner object that handles scanning the QR code and displaying the result
     */
    private BarcodeScanner barcodeScanner;

    /**
     * qrToolbar is a Toolbar that handles the activity title bar
     */
    private Toolbar qrToolbar;


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
        resultLabel = (TextView) findViewById(R.id.result_label);
        image = (ImageView) findViewById(R.id.qr_scan_image);
        qrToolbar = (Toolbar) findViewById(R.id.qrScanToolbar);
        cameraButton = (Button) findViewById(R.id.camera_button);
        selectButton = (Button) findViewById(R.id.select_image_button);
        saveImageButton = (Button) findViewById(R.id.save_image_button);

        returnIntent = null;
        returnCode = 0;

        cameraButton.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick takes the image from the camera activity
             * @param view is a View object
             */
            @Override
            public void onClick(View view) {
                takeImage(view);
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

        saveImageButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               saveImage(view);
           }
        });

        image.setImageResource(R.drawable.ic_launcher_background);
    }

    /**
     * onStart starts the activity
     */
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        activityRequest = intent.getIntExtra("RequestCode", 0);

        if (activityRequest == CAMERA_REQUEST) {
            Log.d(LOG_TAG, "CAMERA_REQUEST code");

            qrToolbar.setTitle("Change Image");

            resultLabel.setVisibility(View.GONE);
            resultText.setVisibility(View.GONE);
        }

        else if (activityRequest == QR_REQUEST) {
            Log.d(LOG_TAG, "QR_REQUEST code");

            saveImageButton.setVisibility(View.GONE);
        }
    }

    /**
     * takeImage takes an image from the camera activity
     * @param view is a View object
     */
    public void takeImage(View view) {
        String cameraPermission = Manifest.permission.CAMERA;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri imageUri;
        File imageFile;
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileName = "photo-" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (activityRequest == CAMERA_REQUEST) {
            try {
                imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                imageUri = FileProvider.getUriForFile(this, "com.example.cs401collaboration.fileprovider", imageFile);

                Log.d(LOG_TAG, "ImageURI: " + imageUri);

                resultFilePath = imageFile.getAbsolutePath();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } catch (IOException ioException) {
                Log.d(LOG_TAG, "File IO Error occurred: " + ioException.getMessage());
            }
        }

        else if (activityRequest == QR_REQUEST) {
            Log.d(LOG_TAG, "QR_REQUEST code");
        }

        if (checkPermission(Manifest.permission.CAMERA))
            startActivityForResult(cameraIntent, CAMERA_REQUEST); // Deprecated!

        else {
            ActivityCompat.requestPermissions(QRScanActivity.this, new String[] { cameraPermission, WRITE_PERMISSION }, CAMERA_REQUEST);
        }
    }

    /**
     * selectImage selects an image from the Gallery
     * @param view is a View object
     */
    public void selectImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

        galleryIntent.setType("image/*");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermission(WRITE_PERMISSION))
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);

            else
                ActivityCompat.requestPermissions(QRScanActivity.this, new String[] {WRITE_PERMISSION}, GALLERY_REQUEST);
        }

        else // Android 10 (Q) and above does not need WRITE_EXTERNAL_STORAGE permission to use Gallery
            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);
    }

    /**
     * saveImage sends the result back when the returnCode is RESULT_OK
     * @param view is a View object
     */
    public void saveImage(View view) {
        if (returnCode == RESULT_OK && activityRequest == CAMERA_REQUEST && returnIntent != null) {
            setResult(returnCode, returnIntent);
            finish();
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
     * onRequestPermissionsResult processes the result of the permission request dialog box
     * @param requestCode is an integer specifying the request code
     * @param permissions is an Array of Permission Strings to request
     * @param permissionResults in an Array of Permission Results from permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] permissionResults) {
        Toast deniedCameraToast = Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG);
        Toast deniedGalleryToast = Toast.makeText(this, R.string.gallery_permission_denied, Toast.LENGTH_LONG);

        super.onRequestPermissionsResult(requestCode, permissions, permissionResults);

        if (permissionResults.length > 0) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            switch (requestCode) {
                case CAMERA_REQUEST:
                    Uri imageUri;
                    File imageFile;
                    File storageDir;
                    String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());
                    String imageFileName = "photo-" + timeStamp;

                    if (activityRequest == CAMERA_REQUEST) {
                        try {
                            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                            imageUri = Uri.fromFile(imageFile);
                            resultFilePath = imageFile.getAbsolutePath();

                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        } catch (IOException ioException) {
                            Log.d(LOG_TAG, "File IO Error occurred: " + ioException.getMessage());
                        }
                    }

                    if (permissionResults[0] == PackageManager.PERMISSION_GRANTED) {
                        resultText.setText(R.string.defaultResult);

                        startActivityForResult(cameraIntent, requestCode); // Deprecated!
                    }

                    else
                        deniedCameraToast.show();

                    break;

                case GALLERY_REQUEST:
                    if (permissionResults[0] == PackageManager.PERMISSION_GRANTED) {
                        resultText.setText(R.string.defaultResult);

                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");

                        startActivityForResult(galleryIntent, requestCode); // Deprecated!
                    }

                    else
                        deniedGalleryToast.show();

                    break;
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
        Intent resultIntent;
        ArrayList<String> resultStringList;
        InputImage qrImage = null;

        super.onActivityResult(requestCode, resultCode, data);

        resultIntent = new Intent();
        resultStringList = new ArrayList<String>();

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Log.d(LOG_TAG, " Using Camera ResultIntent: " + data);

                try {
                    if (activityRequest == QR_REQUEST) {
                        photo = (Bitmap) data.getExtras().get("data");
                        qrImage = InputImage.fromBitmap(photo, 0);

                        image.setImageBitmap(photo);
                    }

                    if (activityRequest == CAMERA_REQUEST) {
                        Log.d(LOG_TAG, "resultFilePath: " + resultFilePath);

                        File photoFile = new File(resultFilePath);
                        photo = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(photoFile));

                        image.setImageBitmap(photo);

                        returnIntent = data;
                        returnIntent.putExtra("ResultFilePath", resultFilePath);
                        returnCode = RESULT_OK;
                    }
                }

                catch (IOException ioException) {
                    Log.d(LOG_TAG, "An IO Error occurred: " + ioException.getMessage());
                }
            }

            else if (requestCode == GALLERY_REQUEST) {
                imageUri = data.getData();

                if (imageUri != null) {
                    image.setImageURI(imageUri);

                    if (activityRequest == QR_REQUEST) {
                        try {
                            qrImage = InputImage.fromFilePath(this, imageUri);
                        } catch (IOException ioException) {
                            Log.e(LOG_TAG, R.string.ioException_string + ": " + ioException.getMessage());
                            setResult(RESULT_FAILED);
                        }
                    }

                    else if (activityRequest == CAMERA_REQUEST) {
                        returnIntent = data;
                        returnCode = RESULT_OK;
                    }
                }
            }

            if (activityRequest == QR_REQUEST && qrImage != null) {
                processImage(qrImage, resultStringList, new OnSuccessListener<ArrayList<String>>() {
                    /**
                     * onSuccess gives the strings ArrayList when successful
                     * @param strings is an ArrayList of Strings to process the results
                     */
                    @Override
                    public void onSuccess(ArrayList<String> strings) {
                        resultIntent.putExtra("ResultString", strings.get(strings.size() - 1));
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    }
                }, new OnFailureListener() {
                    /**
                     * onFailure notifies the calling activity that processImage failed
                     * @param e is an Exception from processImage
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setResult(RESULT_FAILED);
                    }
                });
            }
        }
    }

    /**
     * processImage process the InputImage into a List of Barcodes and display the result
     * @param inputImage is an InputImage to process
     */
    public void processImage(InputImage inputImage, ArrayList<String> resultArrayList,
                             OnSuccessListener<ArrayList<String>> onSuccess, OnFailureListener onFail) {

        if (inputImage != null) {
            barcodeScanner.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                /**
                 * onSuccess gives the List of Barcodes when the process is successful
                 * @param barcodes is a List of Barcodes
                 */
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    int valueType;

                    if (barcodes != null && barcodes.size() > 0) {
                        for (Barcode barcode : barcodes) { // The last QR code recognized is displayed
                            String resultString;
                            int scanFormat = barcode.getFormat();

                            if (scanFormat == Barcode.FORMAT_QR_CODE) {
                                valueType = barcode.getValueType();

                                if (valueType == Barcode.TYPE_TEXT) {
                                    resultString = barcode.getDisplayValue();

                                    Log.d(LOG_TAG, "Barcode String result: " + resultString);

                                    resultArrayList.add(resultString);
                                    resultText.setText(resultString);
                                }
                            } else {
                                resultText.setText(R.string.invalid_coding_string);
                            }
                        }

                        if (resultArrayList.size() > 0)
                            onSuccess.onSuccess(resultArrayList);
                    }

                    else {
                        resultText.setText(R.string.no_qr_text);
                        Log.d(LOG_TAG, "No QR code found in the image");

                        // onFail.onFailure(new Exception("No QR Found"));
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                /**
                 * onFailure prints a message when barcodeScanner fails to process the image
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