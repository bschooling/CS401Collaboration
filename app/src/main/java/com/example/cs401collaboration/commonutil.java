package com.example.cs401collaboration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Common Utilities
 *
 * A central place for common procedures.
 */
public class commonutil
{
    static private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Bounces user to login screen if user is unauthenticated.
     *
     * @param context Activity Context
     */
    public static void bounceIfUnauthenticated (Context context)
    {
        if (mAuth.getCurrentUser() == null)
        {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            context.startActivity(intent);
            Activity activity = (Activity) context;
            activity.finishAffinity();
        }
    }
}
