package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;

import com.android.vending.billing.IInAppBillingService;
import com.artifex.mupdfdemo.MuPDFActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
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
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import ak.detaysoft.galepress.custom_models.ApplicationPlist;
import ak.detaysoft.galepress.custom_models.TabbarItem;
import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.database_models.TestApplicationInf;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.MyImageDecoder;

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
    private WebFragment webFragment;
    private Fragment currentFragment;
    private int requestCount;

     //Global request queue for Volley
    private RequestQueue mRequestQueue;
    private RequestQueue mRequestQueue4Statistic;

    public static HashMap applicationPlist;
    public static LinkedHashMap extrasHashMap;
    private MainActivity mainActivity;
    private MuPDFActivity muPDFActivity;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static GalePressApplication sInstance;
    public String provider;


    public Location location;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;SharedPreferences mPrefs;
    private ContentDetailPopupActivity contentDetailPopupActivity;
    SharedPreferences.Editor mEditor;
    boolean mUpdatesRequested = false;
    private Activity currentActivity = null;
    private TestApplicationInf testApplicationInf;
    private String bannerLink = "";
    private ArrayList<TabbarItem> tabList;
    public boolean isTablistChanced = true;


    public final int M_DPI = 0;
    public final int H_DPI = 1;
    public final int XH_DPI = 2;
    public final int XXH_DPI = 3;


    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean blnBind = false;

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

        DisplayImageOptions displayConfig = new DisplayImageOptions.Builder()
                .cacheInMemory(true).build();
        ImageLoaderConfiguration loaderConfig = new ImageLoaderConfiguration.Builder(this)
                .imageDecoder(new MyImageDecoder(true))
                .defaultDisplayImageOptions(displayConfig).build();
        ImageLoader.getInstance().init(loaderConfig);

        parseApplicationPlist();
        initBillingServices();

        //Uygulama ilk acildiginda localde tutulan renk, banner ve tabbar datalarini alabilmek icin
        ApplicationThemeColor.getInstance().setParameters(null);
        setBannerLink(null);
        setTabList(null);

        if(isTestApplication()){
            SharedPreferences preferences;
            preferences= PreferenceManager.getDefaultSharedPreferences(this);
            setTestApplicationLoginInf(preferences.getString("AppUserName", ""), preferences.getString("AppPassword", ""), preferences.getString("AppId", "0"),
                                       preferences.getString("FacebookEmail", ""), preferences.getString("FacebookUserId", ""), false);
        }

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

    public void destroyBillingServices(){
        if (mService != null) {
            unbindService(mServiceConn);
        }
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

    public ArrayList<ApplicationPlist> getApplicationPlist(){
        parseApplicationPlist();
        ArrayList<ApplicationPlist> list = new ArrayList<ApplicationPlist>();
        ArrayList<String> keyString = new ArrayList<String>(extrasHashMap.keySet());
        ArrayList<String> valueString = new ArrayList<String>(extrasHashMap.values());

        ApplicationPlist item;
        for(int i=0; i<keyString.size(); i++){
            item = new ApplicationPlist(keyString.get(i).toString(), valueString.get(i).toString());
            list.add(item);

        }
        return list;
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

        String applicationId;
        if(isTestApplication())
            applicationId = testApplicationInf.getApplicationId();
        else
            applicationId = (String)applicationPlist.get("ApplicationID");
        return Integer.valueOf(applicationId);
    }

    public void setTestApplicationLoginInf(String username, String password, String applicationId, String facebookEmail, String facebookUserId, boolean succeeded){

        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
        editor = preferences.edit();
        editor.putString("AppUserName", username);
        editor.putString("AppPassword", password);
        editor.putString("AppId", applicationId);
        editor.putString("FacebookEmail", facebookEmail);
        editor.putString("FacebookUserId", facebookUserId);
        editor.commit();

        testApplicationInf = new TestApplicationInf(username,password,applicationId,facebookEmail, facebookUserId, succeeded);
    }

    public void reCreateApplicationTableData(String applicationId){
        if(dataApi != null){
            dataApi.getDatabaseApi().deleteApplication(dataApi.getDatabaseApi().getApplication(getApplicationId()));
            dataApi.getDatabaseApi().createApplication(new L_Application(Integer.parseInt(applicationId), -1));
        }
    }

    public TestApplicationInf getTestApplicationLoginInf(){
        return testApplicationInf;
    }

    public boolean isTestApplication(){
        if(applicationPlist != null && applicationPlist.get("isTestApplication") != null)
            return (Boolean)applicationPlist.get("isTestApplication");
        else
            return false;
    }

    public LibraryFragment getLibraryActivity() {
        return libraryFragmentActivity;
    }

    public void setLibraryActivity(LibraryFragment libraryFragmentActivity) {
        this.libraryFragmentActivity = libraryFragmentActivity;
    }
    public void setCurrentWebFragment(WebFragment webFragment){
        this.webFragment = webFragment;
    }

    public WebFragment getWebFragment(){
        return this.webFragment;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
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

    public ContentDetailPopupActivity getContentDetailPopupActivity() {
        return contentDetailPopupActivity;
    }

    public void setContentDetailPopupActivity(ContentDetailPopupActivity contentDetailPopupActivity) {
        this.contentDetailPopupActivity = contentDetailPopupActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setBannerLink(JSONObject response) {

        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
        editor = preferences.edit();
        String link = "";
        try{
            if(!isTestApplication()) {
                if (response.getString("BannerActive").compareTo("1") == 0)
                    link = response.getString("BannerPage");
            }
            else
                    link =  response.getString("BannerPage");

        } catch (Exception e){
            link = preferences.getString("bannerPage","");
        }
        editor.putString("bannerPage", link);
        editor.commit();

        bannerLink = link;

        if(bannerLink.length() != 0 && getCurrentActivity()!= null && getCurrentActivity().getClass() == MainActivity.class
                && ((MainActivity)getCurrentActivity()).getCurrentLibraryFragment()  != null)
            ((MainActivity)getCurrentActivity()).getCurrentLibraryFragment().updateBanner();


    }

    public int getDeviceDensity(){
        double density = getResources().getDisplayMetrics().density;
        if (density >= 3.0) {
            return XXH_DPI;
        }
        if (density >= 2.0) {
            return XH_DPI;
        }
        if (density >= 1.5 && density < 2.0) {
            return H_DPI;
        }
        else {
            return M_DPI;
        }
    }

    public void setTabList(JSONObject response) {
        ArrayList<TabbarItem> newTabList = new ArrayList<TabbarItem>();
        try {

            if(response != null){
                JSONArray arrayTabs = response.optJSONArray("Tabs");
                int contentsLength = arrayTabs.length();
                for (int i = 0; i < contentsLength; i++) {
                    JSONObject item = arrayTabs.optJSONObject(i);
                    if (null != item) {
                        TabbarItem tab = new TabbarItem(item);
                        newTabList.add(tab);
                    }
                }

                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
                editor = preferences.edit();
                editor.putString("customTabs", arrayTabs.toString());
                editor.commit();

            } else {
                SharedPreferences preferences;
                preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
                JSONArray arrayTabs = new JSONArray(preferences.getString("customTabs", ""));
                int contentsLength = arrayTabs.length();
                for (int i = 0; i < contentsLength; i++) {
                    JSONObject item = arrayTabs.optJSONObject(i);
                    if (null != item) {
                        TabbarItem tab = new TabbarItem(item);
                        newTabList.add(tab);
                    }
                }
            }

        } catch (Exception e) {
            newTabList = null;
        }

        if(tabList != null && tabList.size() == newTabList.size()){
            int index = 0;
            for(TabbarItem currentListItem : tabList){
                for(TabbarItem newListItem : newTabList){
                    if(currentListItem.toString().compareTo(newListItem.toString()) == 0)
                        index++;
                }
            }
            if(index == newTabList.size())
                isTablistChanced = false;
        } else
            isTablistChanced = true;

        tabList = newTabList;

        if(getCurrentActivity()!= null && getCurrentActivity().getClass() == MainActivity.class && isTablistChanced){
            ((MainActivity)getCurrentActivity()).setCustomTabs();
        }
    }

    public ArrayList<TabbarItem> getTabList(){
        if(tabList != null)
            return tabList;
        else
            return null;
    }

    public String getBannerLink() {
        if(bannerLink != null)
            return bannerLink;
        else
            return "";
    }

    public MuPDFActivity getMuPDFActivity() {
        return muPDFActivity;
    }

    public void setMuPDFActivity(MuPDFActivity muPDFActivity) {
        this.muPDFActivity = muPDFActivity;
    }

    public void initBillingServices(){
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        blnBind = bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public IInAppBillingService getmService() {
        return mService;
    }

    public ServiceConnection getmServiceConn() {
        return mServiceConn;
    }

    public boolean isBlnBind() {
        return blnBind;
    }

    public String md5(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(s.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public String prepareMD5ForStorage(String s){
        int mod;
        try{
            mod = getApplicationId()%4;
        } catch (Exception e){
            mod = 0;
        }

        String str1 = "";
        if(mod != 0)
            str1 = s.substring(0,mod);
        String str2 = s.substring(mod, mod+1);
        String str3 = s.substring(mod+1, mod+2);
        String str4 = s.substring(mod+2);

        return str1+str3+str2+str4;
    }
}
