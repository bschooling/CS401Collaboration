package com.example.cs401collaboration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import java.io.ByteArrayOutputStream;
import android.widget.ImageView;

import java.util.UUID;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Facilitate Uploading / Downloading of Image Resources in Firebase Storage.
 *
 * Provides helpers for extracting bytes from ImageView or generating Bitmap from bytes as well.
 *
 * This is a singleton implementation.
 *
 * @author Arshdeep Padda
 */
public class StorageService
{
    /* Members */

    /** FirebaseStorage Handle */
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    /** Primary StorageReference Handle */
    private StorageReference storageReference = firebaseStorage.getReference();

    /** maxDownloadSize : 1MB */
    final private long maxDownloadSize = 1024 * 1024;

    /** Primary Module Tag */
    final private String TAG = "StorageService";


    /* Singleton Implementation */

    private static StorageService INSTANCE;

    private StorageService ()
    {

    }

    public static StorageService getInstance ()
    {
        if(INSTANCE == null)
            INSTANCE = new StorageService();
        return INSTANCE;
    }

    /**
     * Download Resource Indicated by $resource.
     * @param resource Resource to download.
     * @param successCB On Success, passed with byte[].
     * @param failureCB On Failure, passed with exception.
     */
    public void downloadResource
            (
                    String resource,
                    OnSuccessListener<byte[]> successCB,
                    OnFailureListener failureCB
            )
    {
        storageReference.child(resource)
                .getBytes(maxDownloadSize)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d (
                                TAG,
                                "downloadResource: success"
                        );
                        successCB.onSuccess(bytes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d (
                                TAG,
                                "downloadResource: failure" + e
                        );
                        failureCB.onFailure(e);
                    }
                });
    }

    /**
     * Upload Resource Indicated by $bytes.
     *
     * Resource is assigned a random name, in the case that $resourceID is null,
     * and that is then passed to onSuccess.
     *
     * @param resourceID Resource name to be uploaded to in database.
     * @param bytes Resource to upload.
     * @param successCB On Success, passed with generated name of resource.
     * @param failureCB On Failure, passed with exception.
     */
    public void uploadResource
            (
                    String resourceID,
                    byte[] bytes,
                    OnSuccessListener<String> successCB,
                    OnFailureListener failureCB
            )
    {
        if (resourceID == null) resourceID = UUID.randomUUID().toString();

        StorageReference sr = storageReference.child(resourceID);

        UploadTask uploadTask = sr.putBytes(bytes);

        String finalResourceID = resourceID;

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d (
                        TAG,
                        "uploadResource: success"
                );
                successCB.onSuccess(finalResourceID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d (
                        TAG,
                        "uploadResource: failure"
                );
                failureCB.onFailure(exception);
            }
        });
    }

    /**
     * Delete a resource from storage bucket.
     *
     * @param resourceID Resource ID to delete.
     * @param successCB On success callback, takes void.
     * @param failureCB On failure callback, called with exception.
     */
    public void deleteResource
    (
            String resourceID,
            OnSuccessListener<Void> successCB,
            OnFailureListener failureCB
    )
    {
        StorageReference delSR = storageReference.child(resourceID);

        delSR.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                successCB.onSuccess(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                failureCB.onFailure(exception);
            }
        });
    }

    /**
     * Convert resource from byte[] to Bitmap.
     *
     * @param bytes byte[] representation of resource.
     * @return Bitmap Representation.
     */
    public Bitmap toBitmap (byte[] bytes)
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Extract bytes from ImageView.
     * @param imageView ImageView to extract from.
     * @return ImageView bytes.
     */
    public byte[] toBytes (ImageView imageView)
    {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

}
