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
import android.widget.EditText;
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

// Uses QR Generation Library from JourneyApps
public class QRViewActivity extends AppCompatActivity {

    private final int QR_SIZE = 1000;
    private final String LOG_TAG = "QRTest_main";

    private EditText inputText;
    private EditText inputTitle;
    private ImageView image;
    private TextView qrTitle;
    private Button scanQRButton;
    private Button genQRButton;
    private ConstraintLayout qrImageLayout;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_view);

        qrImageLayout = (ConstraintLayout) findViewById(R.id.qr_image_layout);
        image = (ImageView) findViewById(R.id.qr_gen_image);
        qrTitle = (TextView) findViewById(R.id.qr_title);
        inputText = (EditText) findViewById(R.id.input_text);
        inputTitle = (EditText) findViewById(R.id.input_title);
        scanQRButton = (Button) findViewById(R.id.scan_qr_button);
        genQRButton = (Button) findViewById(R.id.gen_qr_button);

        genQRButton.setOnClickListener(new View.OnClickListener() {
            String inText;
            String inTitle;
            Bitmap qrImage;

            public void onClick(View view) {
                inText = inputText.getText().toString();
                inTitle = inputTitle.getText().toString();
                qrImage = genQR(inText, QR_SIZE);

                image.setImageBitmap(qrImage);
                qrTitle.setText(inTitle);
            }
        });

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                scanQRCode(view);
            }
        });

        // image.setImageResource(R.drawable.empty_qr);
        image.setImageBitmap(genQR(" ", QR_SIZE));
    }

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
        }

        return qrGenImage;
    }

    public void saveQR(View view) {
        Bitmap qrImage;
        String qrFileName;
        Toast savedToast;
        Toast saveFailToast;
        OutputStream outStream;

        qrImageLayout.setDrawingCacheEnabled(true);
        qrImage = qrImageLayout.getDrawingCache();

        qrFileName = inputTitle.getText().toString().replace(' ', '-') + ".png";

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

            // Split this try-catch blocks into a separate function
            try {
                outStream = resolver.openOutputStream(qrImageUri);

                // Save as PNG
                // The quality argument is ignored
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

            // Split this try-catch blocks into a separate function
            try {
                outStream = new FileOutputStream(qrFile);

                // Save as PNG
                // The quality argument is ignored
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

    public void scanQRCode(View view) {
        Intent qrScanIntent = new Intent(this, QRScanActivity.class);
        startActivity(qrScanIntent);
    }

}