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

import com.google.mlkit.vision.barcode.BarcodeScanner;
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
    private final String LOG_TAG = "Invii_QRView";

    // Instance variables
    /**
     * image is an ImageView to display the QR code
     */
    private ImageView image;

    /**
     * qrTitle is a TextView to display the title in qr_view_layout
     */
    private TextView qrTitle;

    /**
     * qrImageLayout is a ConstraintLayout that holds qrTitle and image
     */
    private ConstraintLayout qrImageLayout;

    /**
     * barcodeScanner is a BarcodeScanner object used to generate the QR code
     */
    private BarcodeScanner barcodeScanner;

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
        Button scanQRButton;
        Intent qrViewIntent;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_view);

        qrImageLayout = (ConstraintLayout) findViewById(R.id.qr_image_layout);
        image = (ImageView) findViewById(R.id.qr_gen_image);
        qrTitle = (TextView) findViewById(R.id.qr_title);
        scanQRButton = (Button) findViewById(R.id.scan_qr_button);

        /*
        genQRButton.setOnClickListener(new View.OnClickListener() {
            *//**
             * inText holds the inputText to encode into QR code
             *//*
            String inText;

            *//**
             * inTitle holds the inputTitle to display in the qr_view_layout
             *//*
            String inTitle;

            *//**
             * qrImage is a Bitmap object that holds the QR bitmap
             *//*
            Bitmap qrImage;

            *//**
             * onClick generates the QR and title
             * @param view is a View object
             *//*
            public void onClick(View view) {
                inText = inputText.getText().toString();
                inTitle = inputTitle.getText().toString();
                qrImage = genQR(inText, QR_SIZE);

                image.setImageBitmap(qrImage);
                qrTitle.setText(inTitle);
            }
        });
        */

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick calls the scanQRCode method
             * @param view is a View object
             */
            public void onClick(View view) {
                scanQRCode(view);
            }
        });

        // image.setImageResource(R.drawable.empty_qr);
        // image.setImageBitmap(genQR(" ", QR_SIZE));

        qrViewIntent = getIntent();
        inputTitle = qrViewIntent.getStringExtra("qrTitle");
    }

    @Override
    public void onStart() {
        super.onStart();

        qrTitle.setText(inputTitle);
        image.setImageBitmap(genQR(inputTitle, QR_SIZE));
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

        try {
            qrEncoder = new BarcodeEncoder();

            if (input.isEmpty())
                qrGenImage = qrEncoder.encodeBitmap(" ", BarcodeFormat.QR_CODE, size, size);

            else
                qrGenImage = qrEncoder.encodeBitmap(input, BarcodeFormat.QR_CODE, size, size);
        }

        catch (WriterException writerExcept) {
            Log.e(LOG_TAG, writerExcept.getMessage());
            // Make a Toast that says, "Error in Generating QR Code" on long duration
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
        startActivity(qrScanIntent);
    }

}