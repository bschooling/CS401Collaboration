package com.example.cs401collaboration.glide;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;

/**
 * Clear Glide Disk and Memory Cache.
 *
 * Usage: new ClearGlideCache(this).execute();
 *      Where $this : Activity.
 */
public class ClearGlideCache extends AsyncTask<Void, Void, Void>
{
    Activity activity;
    public ClearGlideCache (Activity activity)
    {
      this.activity = activity;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Glide.get(activity).clearDiskCache();
        return null;
    }

    @Override
    protected void onPostExecute (Void result)
    {
        Glide.get(activity).clearMemory();
    }
}
