package com.example.cs401collaboration;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QRScanActivityTest {
    private final Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private final String cameraPermission = Manifest.permission.CAMERA;
    private final String galleryPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * beforeTests is run before each test is conducted
     */
    @Before
    public void beforeTests() {
        Intents.init();
    }

    /**
     * afterTest is run after each test has finished
     */
    @After
    public void afterTests() {
        Intents.release();
    }


    // Tests
    /**
     * selectQRImageTest tests the selection of a QR image
     */
    @Test
    public void selectQRImageTest() {
        GrantPermissionRule permissionRule = GrantPermissionRule.grant(cameraPermission, galleryPermission);
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.select_image_button)).perform(click());
            intending(hasAction(Intent.ACTION_GET_CONTENT));
        }
    }

    /**
     * takeQRImageTest tests the taking an image for QR
     */
    @Test
    public void takeQRImageTest() {
        GrantPermissionRule permissionRule = GrantPermissionRule.grant(cameraPermission, galleryPermission);
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.camera_button)).perform(click());
            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
        }
    }

    /**
     * processQRImageTest tests the processing of a QR Image
     */
    @Test
    public void processQRImageTest() {
        GrantPermissionRule permissionRule = GrantPermissionRule.grant(cameraPermission, galleryPermission);
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.select_image_button)).perform(click());
            intending(hasAction(Intent.ACTION_GET_CONTENT));
        }
    }

    /**
     * selectImageTest tests the selection of an image for CollectionView or ItemView
     */
    @Test
    public void selectImageTest() {
        GrantPermissionRule permissionRule = GrantPermissionRule.grant(cameraPermission, galleryPermission);
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
        testIntent.putExtra("imageResourceID", "placeholder.png");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.select_image_button)).perform(click());
            intending(hasAction(Intent.ACTION_GET_CONTENT));
        }
    }

    /**
     * takeImageTest tests the taking an image for CollectionView or ItemView
     */
    @Test
    public void takeImageTest() {
        GrantPermissionRule permissionRule = GrantPermissionRule.grant(cameraPermission, galleryPermission);
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
        testIntent.putExtra("imageResourceID", "placeholder.png");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.camera_button)).perform(click());
            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
        }
    }

    /**
     * deleteImageTest tests the deletion of an image
     */
    @Test
    public void deleteImageTest() {
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
        testIntent.putExtra("imageResourceID", "placeholder.png");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.delete_image_button)).perform(click());
            intending(hasExtra("imageResourceID", "placeholder.png"));
        }
    }

    /**
     * clearImageTest tests the clearing of an image and resetting its imageResourceID to the default
     */
    @Test
    public void clearImageTest() {
        Intent testResultIntent = new Intent();
        testResultIntent.putExtra("imageResourceID", "placeholder.png");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, testResultIntent);

        Intent inputIntent = new Intent(testContext, QRScanActivity.class);
        inputIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
        inputIntent.putExtra("imageResourceID", "placeholder.png");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(inputIntent)) {
            intending(hasComponent(QRScanActivity.class.getName())).respondWith(
                    new Instrumentation.ActivityResult(Activity.RESULT_OK, testResultIntent) );
            
            onView(withId(R.id.clear_image_button)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());

            int resultCode = activityScenario.getResult().getResultCode();
            Intent resultIntent = activityScenario.getResult().getResultData();

            assertEquals(Activity.RESULT_OK, resultCode);

            // assertEquals(testResultIntent, resultIntent);

        }
    }
}