package com.artifex.mupdfdemo;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by p1025 on 16.04.2015.
 */
public abstract class ThumnailSafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public void safeExecute(Params... params) {
        try {
            execute(params);
        } catch(RejectedExecutionException e) {
            // Failed to start in the background, so do it in the foreground
            onPreExecute();
            if (isCancelled()) {
                onCancelled();
            } else {
                onPostExecute(doInBackground(params));
            }
        }
    }
}
