package com.example.cs401collaboration;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QRViewActivityTest {
    private final Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private final String ACT = "QRView";
    private final String LOG_TAG = ACT + "ActivityTest";

    @After
    public void cleanUp() {
        // onView(withId(R.id.input_title)).perform(clearText());
        // onView(withId(R.id.input_text)).perform(clearText());
    }

    @Test
    public void qrTitleTest() {
        Intent testResultIntent = new Intent();
        testResultIntent.putExtra("imageResourceID", "placeholder.png");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, testResultIntent);

        Intent inputIntent = new Intent(testContext, QRScanActivity.class);
        inputIntent.putExtra("qrTitle", "Oven");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(inputIntent)) {

            String[] strings = {"Oven", "Ladder", "Recipe", "Meat", "Tea"};

            for (String string : strings) {
                /*
                onView(withId(R.id.input_title)).perform(typeText(string), closeSoftKeyboard());
                onView(withId(R.id.gen_qr_button)).perform(click());

                onView(withId(R.id.qr_title)).check(matches(withText(string)));

                onView(withId(R.id.input_title)).perform(clearText());
                */
            }
        }
    }

    @Test
    public void genQRTest() {
        String[] strings = { "Oven", "Ladder", "Recipe", "Meat", "Tea" };
        final int QR_SIZE = 1000;

        for (String string : strings) {
            BarcodeEncoder testQREncoder = new BarcodeEncoder();
            Bitmap testBitmap;

            try {
                testBitmap = testQREncoder.encodeBitmap(string, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

                // onView(withId(R.id.input_text)).perform(typeText(string), closeSoftKeyboard());
                // onView(withId(R.id.gen_qr_button)).perform(click());

                // onView(withId(R.id.qr_gen_image)).check(matches(withBitmap(testBitmap)));
            }

            catch (WriterException writerExcept) {
                Log.e(LOG_TAG, writerExcept.getMessage());
            }
        }
    }

    @Test
    public void saveQRTest() {
        // onView(withId(R.id.input_title)).perform(typeText("TestSave"), closeSoftKeyboard());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        }

        else {

        }
    }
}