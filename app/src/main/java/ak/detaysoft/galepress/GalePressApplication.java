package ak.detaysoft.galepress;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

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

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static GalePressApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
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
        return 10;
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
}
