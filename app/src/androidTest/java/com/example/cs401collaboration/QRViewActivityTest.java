package com.example.cs401collaboration;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QRViewActivityTest {
    private final Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private final String ACT = "QRView";
    private final String LOG_TAG = ACT + "ActivityTest";

    @Before
    public void beforeTests() {
        Intents.init();
    }

    @After
    public void afterTests() {
        Intents.release();
    }

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

        String[] strings = {"Oven", "Ladder", "Recipe", "Meat", "Tea"};

        for (String string : strings) {
            Intent inputIntent = new Intent(testContext, QRViewActivity.class);
            inputIntent.putExtra("qrTitle", string);
            inputIntent.putExtra("entityType", "item");
            inputIntent.putExtra("encodeString", string);

            try (final ActivityScenario<QRViewActivity> activityScenario = ActivityScenario.launch(inputIntent)) {
                onView(Matchers.allOf(withId(R.id.qr_title), withParent(withId(R.id.qr_image_layout)))).check(matches(withText(string)));
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