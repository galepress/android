package ak.detaysoft.galepress;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ak.detaysoft.galepress.database_models.TestApplicationInf;

/**
 * Created by adem on 11/02/14.
 */
public class GalePressApplication extends Application {

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";
    private static DatabaseApi databaseApi = null;
    private static DataApi dataApi;
    private LibraryFragment libraryFragmentActivity;

     //Global request queue for Volley
    private RequestQueue mRequestQueue;

    public static HashMap applicationPlist;
    public static LinkedHashMap extrasHashMap;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static GalePressApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        parseApplicationPlist();
        getDataApi().getAppDetail();
    }

    public void parseApplicationPlist(){
        applicationPlist = new HashMap();
        extrasHashMap = new LinkedHashMap();
        Object[] extras;
        try {
            InputStream is = getResources().openRawResource(R.raw.application);
            NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(is);
            applicationPlist = (HashMap)rootDict.toJavaObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        extras= (Object[])applicationPlist.get("Extras");

        for(int i=0; i<extras.length; i++){
            HashMap extra = (HashMap)extras[i];
            extrasHashMap.putAll(extra);
        }
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized GalePressApplication getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public DatabaseApi getDatabaseApi() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (databaseApi == null) {
            databaseApi = new DatabaseApi(this);
        }
        return databaseApi;
    }

    public DataApi getDataApi() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (dataApi == null) {
            dataApi = new DataApi();
        }
        return dataApi;
    }

    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public Integer getApplicationId(){
        // TODO: ApplicationID burada application.plist'den alinmali..
        String applicationId = (String)applicationPlist.get("ApplicationID");
        return Integer.valueOf(applicationId);
    }

    public TestApplicationInf getTestApplicationLoginInf(){
        // TODO: returns testApplicaionLogin informations
        return new TestApplicationInf();
    }

    public boolean isTestApplication(){
        // TODO: isTest Application flag must be read from application.plist.
        if(true){
            // This is not test application.
            return false;
        }
        else{
            // Test Application
            return true;
        }
    }

    public LibraryFragment getLibraryActivity() {
        return libraryFragmentActivity;
    }

    public void setLibraryActivity(LibraryFragment libraryFragmentActivity) {
        this.libraryFragmentActivity = libraryFragmentActivity;
    }

    void displayMessageOnScreen(Context context, String message) {

        Intent intent = new Intent("ak.detaysoft.galepress.DISPLAY_MESSAGE");
        intent.putExtra("Message", message);

        // Send Broadcast to Broadcast receiver with message
        context.sendBroadcast(intent);

    }


    //Function to display simple Alert Dialog
//    public void showAlertDialog(Context context, String title, String message,
//                                Boolean status) {
//        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//
//        // Set Dialog Title
//        alertDialog.setTitle(title);
//
//        // Set Dialog Message
//        alertDialog.setMessage(message);
//
//        if(status != null)
//            // Set alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);
//
//        // Set OK Button
//        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//
//        // Show Alert Message
//        alertDialog.show();
//    }

    private PowerManager.WakeLock wakeLock;

    public  void acquireWakeLock(Context context) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");

        wakeLock.acquire();
    }

    public  void releaseWakeLock() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}
