package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.android.volley.Cache;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.database_models.TestApplicationInf;

/**
 * Created by adem on 11/02/14.
 */
public class GalePressApplication
        extends Application
        implements LocationListener,GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener
{

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";
    private static DatabaseApi databaseApi = null;
    private static DataApi dataApi;
    private LibraryFragment libraryFragmentActivity;
    private int requestCount;

     //Global request queue for Volley
    private RequestQueue mRequestQueue;
    private RequestQueue mRequestQueue4Statistic;

    public static HashMap applicationPlist;
    public static LinkedHashMap extrasHashMap;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static GalePressApplication sInstance;
    public String provider;


    public Location location;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    boolean mUpdatesRequested = false;

    private Activity currentActivity = null;

    Foreground.Listener myListener = new Foreground.Listener(){
        public void onBecameForeground(){
            mLocationClient.connect();
            startUpdates();

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
            L_Statistic statistic = new L_Statistic(udid, null, location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, null, dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_applicationActive, null,null,null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);
            GalePressApplication.getInstance().getDataApi().startStatisticSend();


        }
        public void onBecameBackground(){
            // ... whatever you want to do

            // If the client is connected
            if (mLocationClient.isConnected()) {
                stopUpdates();
            }

            // After disconnect() is called, the client is considered "dead".
            mLocationClient.disconnect();

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
            L_Statistic statistic = new L_Statistic(udid, null, location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, null, dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_applicationPassive, null,null,null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);
            GalePressApplication.getInstance().getDataApi().stopStatisticSend();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        parseApplicationPlist();
        Foreground.get(this).addListener(myListener);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mUpdatesRequested = false;
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
        requestCount = -101;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Logout.e(LocationUtils.APPTAG, getString(R.string.resolved));
                        break;
                    default:
                        Logout.e(LocationUtils.APPTAG, getString(R.string.no_resolution));
                        break;
                }
            default:
                Logout.e(LocationUtils.APPTAG,getString(R.string.unknown_activity_request_code, requestCode));
                break;
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Logout.e(LocationUtils.APPTAG, getString(R.string.play_services_available));
            return true;
        } else {
            Logout.e(LocationUtils.APPTAG, "Google Play services is not available");
            return false;
        }
    }

    public Location getLocation() {
        if (servicesConnected()) {
            location = mLocationClient.getLastLocation();
        }
        return location;
    }

    public void startUpdates() {
        mUpdatesRequested = true;
        if (servicesConnected() && mLocationClient.isConnected()) {
            startPeriodicUpdates();
        }
    }

    public void stopUpdates() {
        mUpdatesRequested = false;
        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest,this);
    }

    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    @Override
    public void onDisconnected() {
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

    public RequestQueue getRequestQueue4Statistic() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue4Statistic == null) {
            mRequestQueue4Statistic = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue4Statistic;
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

    public void cancelPendingRequests4Statistic(Object tag) {
        if (mRequestQueue4Statistic != null) {
            mRequestQueue4Statistic.cancelAll(tag);
        }
    }

    public Integer getApplicationId(){
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



    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
                //connectionResult.startResolutionForResult(this.getLibraryActivity(),LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            Logout.e("Adem", "startResolutionForResult: ");
        } else {
            Logout.e("Adem", "Error Code : "+connectionResult.getErrorCode());
        }
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        if(requestCount == 0){
//            Logout.e("Adem", "***Requestler bitmis olmali");
            dataApi.updateCompleted();
        }
        if(requestCount == -100) {
            requestCount = 1;
//            Logout.e("Adem", "***Requestler yeni basliyor.");
            // requestCount ilk kez initialize ediliyor. O olsaydi bitmis gibi gorunebilirdi. -101 ile initialize ettim.
        }
        this.requestCount = requestCount;
        Logout.e("Adem", "***Requestler count : "+this.requestCount);

    }

    public void incrementRequestCount() {
        setRequestCount(getRequestCount()+1);
    }
    public void decrementRequestCount() {
        setRequestCount(getRequestCount()-1);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
