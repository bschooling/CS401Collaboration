package com.example.cs401collaboration;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * QRViewActivity manages things about displaying the QR code of an associated item
 * It uses ZXing Android Embedded Library from JourneyApps licensed in Apache 2.0
 * @author Nguyen Nam Pham
 */
public class QRViewActivity extends AppCompatActivity {
    // Class constants (finals)
    /**
     * QR_SIZE is the size of the Bitmap generated by genQR in pixels
     */
    final static int QR_SIZE = 1000;

    /**
     * LOG_TAG is the String used in Android Logcat
     */
    private final String LOG_TAG = "QRViewActivity";

    // Instance variables
    /**
     * image is an ImageView to display the QR code
     */
    private ImageView image;

    /**
     * qrImageLayout is a ConstraintLayout that holds qrTitle and image
     */
    private ConstraintLayout qrImageLayout;

    /**
     * inputTitle holds the QR Title String to display in qr_view_layout
     */
    private String inputTitle;

    // Instance methods

    /**
     * onCreate sets up the activity
     * @param savedInstanceState is a Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView qrTitle;
        Button saveQRButton;
        Intent qrViewIntent;
        String encodeString;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_view);

        qrImageLayout = (ConstraintLayout) findViewById(R.id.qr_image_layout);
        image = (ImageView) findViewById(R.id.qr_gen_image);
        qrTitle = (TextView) findViewById(R.id.qr_title);
        saveQRButton = (Button) findViewById(R.id.save_qr_image_button);

        saveQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveQR(view);
            }
        });

        qrViewIntent = getIntent();
        inputTitle = qrViewIntent.getStringExtra("qrTitle");
        encodeString = qrViewIntent.getStringExtra("encodeString");

        qrTitle.setText(inputTitle);
        image.setImageBitmap(genQR(encodeString, QR_SIZE)); // Change inputTitle to Collection or Item ID
    }

    // Required method for CAMERA_REQUEST
    /**
     * onActivityResult receives and processes the result from another activity
     * @param requestCode is the integer to request to the activity
     * @param resultCode is the integer to give the result from the activity
     * @param data is the Intent to process
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap;
        Toast deniedToast = Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG);

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.CAMERA_REQUEST) {
                bitmap = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);
            }
        }

        else if (resultCode == QRScanActivity.RESULT_DENIED) {
            deniedToast.show();
        }
    }

    /**
     * genQR generates the QR Code as an image
     * @param input is the input text String used to encode into QR
     * @param size is the output size of the Bitmap in pixels
     * @return The generated Bitmap image
     */
    public Bitmap genQR(String input, int size) {
        BarcodeEncoder qrEncoder;
        Bitmap qrGenImage = null;
        Toast failToast = Toast.makeText(this, "Error in Generating QR Code", Toast.LENGTH_LONG);

        try {
            qrEncoder = new BarcodeEncoder();

            if (input.isEmpty())
                qrGenImage = qrEncoder.encodeBitmap(" ", BarcodeFormat.QR_CODE, size, size);

            else
                qrGenImage = qrEncoder.encodeBitmap(input, BarcodeFormat.QR_CODE, size, size);
        }

        catch (WriterException writerExcept) {
            Log.e(LOG_TAG, "Error in Generating QR Code: " + writerExcept.getMessage());
            failToast.show();
        }

        return qrGenImage;
    }

    /**
     * saveQR saves the qr_view_layout into an image on shared storage
     * @param view is a View object
     */
    public void saveQR(View view) {
        Bitmap qrImage;
        String qrFileName;
        Toast savedToast;
        Toast saveFailToast;
        OutputStream outStream;

        qrImageLayout.setDrawingCacheEnabled(true);
        qrImage = qrImageLayout.getDrawingCache();

        qrFileName = inputTitle.replace(' ', '-') + ".png";

        // Unsure about Toast duration...
        savedToast = Toast.makeText(this, "QR Code Image Saved", Toast.LENGTH_LONG);
        saveFailToast = Toast.makeText(this, "QR Code Image Save Error", Toast.LENGTH_LONG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Required due to the revamped storage scheme in Android 10 (Q)
            ContentValues values = new ContentValues();
            ContentResolver resolver = this.getContentResolver();

            // TODO Android 10 Maybe add a dedicated folder for our app...
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, qrFileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri qrImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                outStream = resolver.openOutputStream(qrImageUri);

                // Save as PNG
                // Maximum quality is used, but the quality argument is ignored.
                qrImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                outStream.close();
                savedToast.show();
            }

            catch (IOException ioException) {
                Log.e(LOG_TAG, "Android 10 and above IO Error occurred: " + ioException.getMessage());
                saveFailToast.show();
            }
        }

        else {
            // TODO Android 9 Maybe add a dedicated folder for our app...
            String qrImageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File qrFile = new File(qrImageDir, qrFileName);

            try {
                outStream = new FileOutputStream(qrFile);

                // Save as PNG
                // Maximum quality is used, but the quality argument is ignored.
                qrImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                outStream.close();
                savedToast.show();
            }

            catch (IOException ioException) {
                Log.e(LOG_TAG, "Android 9 and below IO Error occurred: " + ioException.getMessage());
                saveFailToast.show();
            }
        }
    }

    /**
     * scanQRCode spawns the QRScanActivity to scan the QRCode
     * It is here for compatibility with the call from activity layout onClick.
     * @param view is a View object
     */
    public void scanQRCode(View view) {
        Intent qrScanIntent = new Intent(this, QRScanActivity.class);
        qrScanIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

        startActivity(qrScanIntent);
    }

}