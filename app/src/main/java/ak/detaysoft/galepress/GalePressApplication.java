package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;

import com.android.vending.billing.IInAppBillingService;
import com.artifex.mupdfdemo.MuPDFActivity;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import ak.detaysoft.galepress.custom_models.Subscription;
import ak.detaysoft.galepress.custom_models.UserInformations;
import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkPreferences;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.custom_models.ApplicationPlist;
import ak.detaysoft.galepress.custom_models.TabbarItem;
import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Content;
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
    private CustomTabFragment customTabFragment;
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
    private ArrayList<Integer> membershipMenuList;
    private ArrayList<Subscription> subscriptions;
    private boolean userHaveActiveSubscription = false;


    public final static int BILLING_RESPONSE_RESULT_OK = 0;
    public final static int RESULT_USER_CANCELED = 1;
    public final static int RESULT_BILLING_UNAVAILABLE = 3;
    public final static int RESULT_ITEM_UNAVAILABLE = 4;
    public final static int RESULT_DEVELOPER_ERROR = 5;
    public final static int RESULT_ERROR = 6;
    public final static int RESULT_ITEM_ALREADY_OWNED = 7;
    public final static int RESULT_ITEM_NOT_OWNED = 8; //For consumable product


    public final int M_DPI = 0;
    public final int H_DPI = 1;
    public final int XH_DPI = 2;
    public final int XXH_DPI = 3;


    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean blnBind = false;

    private UserInformations userInformation;


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
        Fabric.with(this, new Crashlytics());


        XWalkPreferences.setValue("enable-javascript", true);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);

        sInstance = this;
        parseApplicationPlist();

        DisplayImageOptions displayConfig = new DisplayImageOptions.Builder()
                .cacheInMemory(true).build();
        ImageLoaderConfiguration loaderConfig = new ImageLoaderConfiguration.Builder(this)
                .imageDecoder(new MyImageDecoder(true))
                .defaultDisplayImageOptions(displayConfig).build();
        ImageLoader.getInstance().init(loaderConfig);



        initBillingServices();
        getlocalActiveSubscripton();
        prepareMemberShipList();
        prepareSubscriptions(null);

        //Uygulama ilk acildiginda localde tutulan renk, banner ve tabbar datalarini alabilmek icin
        ApplicationThemeColor.getInstance().setParameters(null);
        setBannerLink(null);
        setTabList(null);

        if(isTestApplication()){
            SharedPreferences preferences;
            preferences= getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
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
        try{
            if (mService != null && isBlnBind()) {
                unbindService(mServiceConn);
            }
        } catch (Exception e) {
            Log.e("Billingservice", "destroyerror" + e.toString());
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
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
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
        else {

            /*
            * plist parse edmedigi durumlar oluyor. Tekrar cekmeye calisiyoruz eger olmazsa appId -1 set ediliyor.
            * */
            if (applicationPlist == null || applicationPlist.get("ApplicationID") == null)
                parseApplicationPlist();

            if (applicationPlist == null || applicationPlist.get("ApplicationID") == null)
                applicationId = String.valueOf("-1");
            else
                applicationId = (String)applicationPlist.get("ApplicationID");
        }

        return Integer.valueOf(applicationId);
    }

    public void setTestApplicationLoginInf(String username, String password, String applicationId, String facebookEmail, String facebookUserId, boolean succeeded){

        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
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
            setTestApplicationLoginInf(testApplicationInf.getUsername(), testApplicationInf.getPassword(), applicationId, testApplicationInf.getFacebookEmail(), testApplicationInf.getFacebookUserId(), false);
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
    public void setCurrentWebFragment(CustomTabFragment customTabFragment){
        this.customTabFragment = customTabFragment;
    }

    public CustomTabFragment getCustomTabFragment(){
        return this.customTabFragment;
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
        if (wakeLock != null){
            try {
                if (wakeLock.isHeld())
                    wakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
        wakeLock = null;
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
            dataApi.updateCompleted();
        }
        if(requestCount == -100) {
            requestCount = 1;
            // requestCount ilk kez initialize ediliyor. O olsaydi bitmis gibi gorunebilirdi. -101 ile initialize ettim.
        }
        this.requestCount = requestCount;
        Logout.e("Adem", "***Requestler count : " + this.requestCount);

    }

    public void incrementRequestCount() {
        setRequestCount(getRequestCount() + 1);
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

        //bannerLink = "http://rastcode.com/test/index.html";
        if(bannerLink.length() != 0 && getCurrentActivity()!= null && getCurrentActivity().getClass() == MainActivity.class
                && ((MainActivity)getCurrentActivity()).getLibraryFragment()  != null)
            ((MainActivity)getCurrentActivity()).getLibraryFragment().updateBanner();

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
            ((MainActivity)getCurrentActivity()).initCustomTabs();
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
        try{
            blnBind = bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            blnBind = false;
        }

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

    public String getMD5EncryptedValue(String s) {
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

    public String getMD5MixedValue(String s){
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

    public ArrayList<Integer> getMembershipMenuList() {
        return membershipMenuList;
    }

    public void setMembershipMenuList(ArrayList<Integer> membershipMenuList) {
        this.membershipMenuList = membershipMenuList;
    }

    public void prepareMemberShipList(){

        membershipMenuList = new ArrayList<Integer>();
        SharedPreferences preferences = getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
        String token = preferences.getString("accessToken","");
        if(token.length() != 0){ //login olmus kullanici var
            try {
                userInformation = new UserInformations(new JSONObject(token));
                String recoveryToken = userInformation.getAccessToken();
                try{
                    userInformation.setAccessToken(getMD5MixedValue(userInformation.getAccessToken()));
                } catch (Exception e){
                    userInformation.setAccessToken(recoveryToken);
                    e.printStackTrace();
                }
                if(!isUserHaveActiveSubscription())
                    membershipMenuList.add(LeftMenuMembershipAdapter.SUBSCRIPTION);

                membershipMenuList.add(LeftMenuMembershipAdapter.RESTORE);
                membershipMenuList.add(LeftMenuMembershipAdapter.LOGOUT);
            } catch (JSONException e) {
                userInformation = null;
                membershipMenuList.add(LeftMenuMembershipAdapter.LOGIN);
                e.printStackTrace();
            }
        } else { //login olmus kullanici yok
            userInformation = null;
            membershipMenuList.add(LeftMenuMembershipAdapter.LOGIN);
        }
    }

    public void editMemberShipList(boolean isLogin, JSONObject response){

        membershipMenuList = new ArrayList<Integer>();
        SharedPreferences preferences = getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        if(isLogin){ //kullanici login olacak
            editor = preferences.edit();
            userInformation = new UserInformations(response);
            String recoveryToken = userInformation.getAccessToken();
            try{
                userInformation.setAccessToken(getMD5MixedValue(userInformation.getAccessToken()));
            } catch (Exception e){
                userInformation.setAccessToken(recoveryToken);
                e.printStackTrace();
            }
            editor.putString("accessToken", userInformation.getJSONObject().toString());
            editor.commit();
            userInformation.setAccessToken(recoveryToken);
            if(!isUserHaveActiveSubscription())
                membershipMenuList.add(LeftMenuMembershipAdapter.SUBSCRIPTION);
            membershipMenuList.add(LeftMenuMembershipAdapter.RESTORE);
            membershipMenuList.add(LeftMenuMembershipAdapter.LOGOUT);
        } else { //kullanici logout olacak
            editor = preferences.edit();
            editor.putString("accessToken", "");
            userInformation = null;
            editor.commit();
            membershipMenuList.add(LeftMenuMembershipAdapter.LOGIN);
        }
    }

    public void restorePurchasedSubscriptions(final boolean applicationFirstOpen, final boolean fullRestore, final Activity activity, final ProgressDialog progress){
        AsyncTask<Void, Void, Void> restore = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //Kullanicinin daha once aldigi urunler kontrol ediliyor
                if (GalePressApplication.getInstance().isBlnBind() && GalePressApplication.getInstance().getmService() != null) {

                    Bundle ownedItems;
                    ArrayList<String> ownedSubscriptionList = new ArrayList<String>();
                    ArrayList<String> ownedSkus = new ArrayList<String>();

                    try {
                        ownedItems = GalePressApplication.getInstance().getmService().getPurchases(3, getPackageName(), "subs", null);
                        int response = ownedItems.getInt("RESPONSE_CODE");

                        if (response == 0){
                            ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                            ownedSubscriptionList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        }

                        if(ownedSkus.size() > 0){
                            for(String skuItem : ownedSkus){
                                for(int i = 0; i < subscriptions.size(); i++){
                                    if(skuItem.compareTo(subscriptions.get(i).getIdentifier()) == 0){
                                        subscriptions.get(i).setOwned(true);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    /*
                    * Abonelik satin alindiginda sendReceipt isleminin basarisiz olma ihtimaline karsi tekrar deniyoruz (MG)
                    * */
                    try {
                        if(ownedSubscriptionList != null && ownedSubscriptionList.size() > 0){
                            for(String purchaseData : ownedSubscriptionList){
                                JSONObject jpurchase = new JSONObject(purchaseData);
                                GalePressApplication.getInstance().getDataApi().sendReceipt(jpurchase.getString("productId"), jpurchase.getString("purchaseToken"), jpurchase.getString("packageName"), null, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayList<String> skuList = new ArrayList<String>();
                    Bundle querySkus = new Bundle();
                    for(int i = 0; i < subscriptions.size(); i++){
                        Subscription subscription = subscriptions.get(i);
                        skuList.add(subscription.getIdentifier());
                        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                    }

                    Bundle skuDetails;
                    try {
                        skuDetails = GalePressApplication.getInstance().getmService().getSkuDetails(3, getPackageName(), "subs", querySkus);
                        int response = skuDetails.getInt("RESPONSE_CODE");

                        if (response == 0){
                            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                            if (responseList.size() != 0) {
                                for (String thisResponse : responseList) {
                                    JSONObject object = null;
                                    try {
                                        object = new JSONObject(thisResponse);
                                        for(int i = 0; i < subscriptions.size(); i++){
                                            if(object.getString("productId").compareTo(subscriptions.get(i).getIdentifier()) == 0){
                                                subscriptions.get(i).setMarketPrice(object.getString("price"));
                                                break;
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(!applicationFirstOpen){
                    prepareSubscriptions(null);

                    if(fullRestore) {
                        dataApi.restoreContentsAndSubscritonsFromServer(activity, progress);
                    } else {
                        if(activity != null){
                            ((MainActivity)activity).openSubscriptionChooser();
                        }
                        if(progress != null && progress.isShowing())
                            progress.dismiss();
                    }
                }


            }
        };
        restore.execute();
    }

    public void restorePurchasedProductsFromMarket(final boolean fullRestore, final Activity activity, final ProgressDialog progress){

        AsyncTask<Void, Void, Void> executePurchase = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                List<L_Content> localContents = GalePressApplication.getInstance().getDatabaseApi().getAllContents(null);
                //Kullanicinin daha once aldigi urunler kontrol ediliyor
                if (GalePressApplication.getInstance().isBlnBind() && GalePressApplication.getInstance().getmService() != null) {
                    Bundle ownedItems;

                    ArrayList<String> ownedProductList = new ArrayList<String>();
                    ArrayList<String> ownedSkus = new ArrayList<String>();

                    try {
                        ownedItems = GalePressApplication.getInstance().getmService().getPurchases(3, getPackageName(), "inapp", null);
                        int response = ownedItems.getInt("RESPONSE_CODE");


                        if (response == 0){
                            ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                            ownedProductList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        }

                        if(ownedSkus.size() > 0){
                            for(String skuItem : ownedSkus){
                                for(int i = 0; i < localContents.size(); i++){
                                    L_Content content = localContents.get(i);
                                    if(skuItem.compareTo(content.getIdentifier()) == 0){
                                        content.setOwnedProduct(true);
                                        GalePressApplication.getInstance().getDatabaseApi().updateContent(content, false);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    /*
                    * Eger urun satin alindiginda sendReceipt isleminin basarisiz olma ihtimaline karsi tekrar deniyoruz (MG)
                    * */
                    try {
                        if(ownedProductList != null && ownedProductList.size() > 0){
                            for(String purchaseData : ownedProductList){
                                JSONObject jpurchase = new JSONObject(purchaseData);
                                GalePressApplication.getInstance().getDataApi().sendReceipt(jpurchase.getString("productId"), jpurchase.getString("purchaseToken"), jpurchase.getString("packageName"), null, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayList<String> skuList = new ArrayList<String>();
                    Bundle querySkus = new Bundle();
                    for(int i = 0; i < localContents.size(); i++){
                        L_Content content = localContents.get(i);
                        if(content.isBuyable()){
                            //Satin alinabilen urunse fiyati kontrol ediliyor
                            skuList.add(content.getIdentifier());
                            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                        }
                    }

                    Bundle skuDetails;
                    try {
                        skuDetails = GalePressApplication.getInstance().getmService().getSkuDetails(3, getPackageName(), "inapp", querySkus);
                        int response = skuDetails.getInt("RESPONSE_CODE");

                        if (response == 0){
                            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                            if (responseList.size() != 0) {
                                for (String thisResponse : responseList) {
                                    JSONObject object = null;
                                    try {
                                        object = new JSONObject(thisResponse);
                                        for(int i = 0; i < localContents.size(); i++){
                                            L_Content content = localContents.get(i);
                                            if(object.getString("productId").compareTo(content.getIdentifier()) == 0){
                                                content.setMarketPrice(object.getString("price"));
                                                GalePressApplication.getInstance().getDatabaseApi().updateContent(content, false);
                                                break;
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                /*
                * Eger abonelik yoksa dogrudan server restore adimina geciyoruz.
                * */
                if(GalePressApplication.getInstance().getSubscriptions().size() > 0)
                    restorePurchasedSubscriptions(false, fullRestore, activity, progress);
                else{
                    dataApi.restoreContentsAndSubscritonsFromServer(activity, progress);
                }
            }

        };
        executePurchase.execute();

    }

    public void prepareSubscriptions(JSONObject response){

        SharedPreferences preferences = getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(response != null) { //servisten gelen set edilecek
            subscriptions = new ArrayList<Subscription>();
            try {
                JSONArray array = new JSONArray();
                Subscription weekSubscription = new Subscription(Subscription.WEEK, response.getString("SubscriptionWeekIdentifier").toLowerCase()
                        , "", "", (response.getInt("SubscriptionWeekActive") == 0) ? false :true , false);
                if(weekSubscription.isActive()) {
                    subscriptions.add(weekSubscription);
                    array.put(weekSubscription.getJSONObject());
                }

                Subscription monthSubscription = new Subscription(Subscription.MONTH, response.getString("SubscriptionMonthIdentifier").toLowerCase()
                        , "", "", (response.getInt("SubscriptionMonthActive") == 0) ? false :true, false);
                if(monthSubscription.isActive()) {
                    subscriptions.add(monthSubscription);
                    array.put(monthSubscription.getJSONObject());
                }

                Subscription yearSubscription = new Subscription(Subscription.YEAR, response.getString("SubscriptionYearIdentifier").toLowerCase()
                        , "", "", (response.getInt("SubscriptionYearActive") == 0) ? false :true, false );
                if(yearSubscription.isActive()) {
                    subscriptions.add(yearSubscription);
                    array.put(yearSubscription.getJSONObject());
                }

                editor.putString("Subscription", array.toString());
                editor.commit();

                restorePurchasedSubscriptions(false, false, null, null); // marketten fiyatlarini ve kullanicinin daha once satin aldigi abonelikleri cekmek icin (farkli cihazlarda daha once alinan abonelikler gelmeyebilir)

            } catch (JSONException e) {
                e.printStackTrace();
                subscriptions = new ArrayList<Subscription>();
                editor.putString("Subscription","");
                editor.commit();
            }

        } else { //lokaldeki set edilecek yada restore sonucu set edilecek

            if(subscriptions != null) { //market restore yapildi sonucu lokale set edilecek
                JSONArray array = new JSONArray();
                for(Subscription sub : subscriptions){
                    array.put(sub.getJSONObject());
                }
                editor.putString("Subscription", array.toString());
                editor.commit();
            } else { //uygulama ilk acilista lokalde tutulan subs list alınacak
                subscriptions = new ArrayList<Subscription>();
                try {
                    JSONArray array = new JSONArray(preferences.getString("Subscription",""));
                    Subscription subscription;
                    for(int i = 0; i < array.length(); i++){
                        JSONObject object = (JSONObject) array.get(i);
                        subscription = new Subscription(object);
                        subscriptions.add(subscription);
                    }
                    restorePurchasedSubscriptions(true, false, null, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    subscriptions = new ArrayList<Subscription>();
                }
            }

        }
    }

    public UserInformations getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformations userInformation) {
        this.userInformation = userInformation;
    }

    public ArrayList<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    private void getlocalActiveSubscripton(){
        SharedPreferences preferences = getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
        userHaveActiveSubscription = preferences.getBoolean("userHaveActiveSubscription",false);
    }

    public boolean isUserHaveActiveSubscription() {
        return userHaveActiveSubscription;
    }

    public void setUserHaveActiveSubscription(boolean userHaveActiveSubscription) {
        this.userHaveActiveSubscription = userHaveActiveSubscription;

        SharedPreferences preferences = getSharedPreferences("ak.detaysoft.galepress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("userHaveActiveSubscription", userHaveActiveSubscription);
        editor.commit();
    }
}
