package com.example.cs401collaboration;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
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
    //private final Intent testIntent = new Intent(testContext, QRScanActivity.class);

    private final String cameraPermission = Manifest.permission.CAMERA;
    private final String galleryPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    // @Rule
    // public ActivityScenarioRule<QRScanActivity> activityRule = new ActivityScenarioRule<QRScanActivity>(QRScanActivity.class);

    @Before
    public void beforeTests() {
        Intents.init();
    }

    @After
    public void afterTests() {
        Intents.release();
    }

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

    @Test
    public void clearImageTest() {
        Intent testIntent = new Intent(testContext, QRScanActivity.class);
        testIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
        testIntent.putExtra("imageResourceID", "placeholder.png");

        try (final ActivityScenario<QRScanActivity> activityScenario = ActivityScenario.launch(testIntent)) {
            onView(withId(R.id.clear_image_button)).perform(click());
            intending(hasAction(""));
        }
    }
}