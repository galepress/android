/***
 Copyright (c) 2008-2012 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain	a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */

package ak.detaysoft.galepress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import ak.detaysoft.galepress.service_models.ByteArrayResponseHandler;

public class CoverImageDownloader extends IntentService {
    public static final String EXTRA_MESSENGER = "ak.detaysoft.galepress.downloader.EXTRA_MESSENGER";
    private HttpClient client = null;

    public CoverImageDownloader() {
        super("CoverImageDownloader");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        client = new DefaultHttpClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        client.getConnectionManager().shutdown();
    }

    @Override
    public void onHandleIntent(Intent i) {
        Log.e("Adem", "Intent Started");
        HttpGet getMethod = new HttpGet(i.getData().toString());
        int result = Activity.RESULT_CANCELED;
        Bundle extras;
        String coverImageName = "";
        String contentId;
        try {
            extras = i.getExtras();
            if (extras != null) {
                Messenger messenger = (Messenger) extras.get(EXTRA_MESSENGER);
                Message msg = Message.obtain();
                contentId = (String) extras.get("id");
                coverImageName = (String) extras.get("coverImageName");
                ResponseHandler<byte[]> responseHandler = new ByteArrayResponseHandler();
                byte[] responseBody = client.execute(getMethod, responseHandler);
                File output = new File(GalePressApplication.getInstance().getFilesDir(),coverImageName);

                if (output.exists()) {
                    output.delete();
                }

                FileOutputStream fos = new FileOutputStream(output.getPath());

                fos.write(responseBody);
                fos.close();
                result = Activity.RESULT_OK;
                msg.arg1 = result;
                msg.arg2 = new Integer(contentId);
                msg.what = DataApi.MESSAGE_TYPE_COVER_IMAGE;
                try {
                    messenger.send(msg);
                }
                catch (android.os.RemoteException e1) {
                    Log.w(getClass().getName(), "Exception sending message", e1);
                }
            }
        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception in download", e2);
        }
    }
}
