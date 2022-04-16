package com.example.cs401collaboration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRScanActivity extends AppCompatActivity {

    private TextView resultText;
    private ImageView image;
    private BarcodeScanner barcodeScanner;

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

    // Take the image from the camera activity
    public void chooseImage(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, 200); // Deprecated!
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap takenPhoto;
        InputImage qrImage;

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 200) {
                takenPhoto = (Bitmap) data.getExtras().get("data");

                image.setImageBitmap(takenPhoto);
                qrImage = InputImage.fromBitmap(takenPhoto, 0);

                // IDE complains about unchecked casting
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
                    @Override
                    public void onFailure(@NonNull Exception except) {
                        //resultText.setText(R.string.no_qr_text);
                    }
                });
            }
        }
    }

}