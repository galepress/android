package ak.detaysoft.galepress;

/**
 * Created by adem on 11/02/14.
 */


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.artifex.mupdfdemo.MuPDFActivity;
import com.google.android.gcm.GCMRegistrar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ak.detaysoft.galepress.custom_models.ApplicationIds;
import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_ContentCategory;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.search_models.ReaderSearchResult;
import ak.detaysoft.galepress.search_models.MenuSearchResult;
import ak.detaysoft.galepress.service_models.R_AppCategories;
import ak.detaysoft.galepress.service_models.R_AppContents;
import ak.detaysoft.galepress.service_models.R_AppDetail;
import ak.detaysoft.galepress.service_models.R_AppVersion;
import ak.detaysoft.galepress.service_models.R_Category;
import ak.detaysoft.galepress.service_models.R_Content;
import ak.detaysoft.galepress.service_models.R_ContentDetail;
import ak.detaysoft.galepress.service_models.R_ContentFileUrl;
import ak.detaysoft.galepress.util.ApplicationThemeColor;


public class DataApi extends Object {
    //http://galepress.com/ws/v100/applications/20/detail

    private static final String webServisVersion = "103";
    private static final String domainUrl = "http://www.galepress.com";
    public static final Integer MESSAGE_TYPE_COVER_IMAGE = 1;
    public static final Integer MESSAGE_TYPE_COVER_PDF_DOWNLOAD = 2;
    public boolean isBlockedFromWS = false;

    static final String GCM_SENDER_ID = "151896860923";  // Place here your Google project id

    private DatabaseApi databaseApi = null;
    public DownloadPdfTask downloadPdfTask;
    public StatisticSendTask statisticSendTask;
    private Context mContext;

    public void updateCompleted() {
        if (GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(LaunchActivity.class)) {
            LaunchActivity launchActivity = (LaunchActivity) GalePressApplication.getInstance().getCurrentActivity();
            L_Content masterContent = getMasterContent();
            launchActivity.masterContent = masterContent;

            ////
            boolean internet = isConnectedToInternet();
            boolean otherContents = isLibraryMustBeEnabled();
            if (masterContent != null) {
                if (masterContent.isPdfDownloaded()) {
                    if (masterContent.isPdfUpdateAvailable()) {
                        if (internet) {
                            launchActivity.startMasterDownload();
                        } else {
                            launchActivity.openMasterContent();
                        }
                    } else {
                        launchActivity.openMasterContent();
                    }
                } else {
                    if (internet) {
                        launchActivity.startMasterDownload();
                    } else {
                        if (otherContents) {
                            launchActivity.openLibraryFragment();
                        } else {
                            // Do Nothing
                        }
                    }

                }
            } else {
                launchActivity.openLibraryFragment();
            }

        } else if (GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(MainActivity.class)) {
            ((MainActivity) GalePressApplication.getInstance().getCurrentActivity()).updateActivityViewAndAdapter(false);
        } else if (GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(ViewerLoginActivity.class)) {
            ViewerLoginActivity loginActivity = (ViewerLoginActivity) GalePressApplication.getInstance().getCurrentActivity();
            L_Content masterContent = getMasterContent();
            loginActivity.masterContent = masterContent;
            loginActivity.updateActivity();
            ////
            boolean internet = isConnectedToInternet();
            boolean otherContents = isLibraryMustBeEnabled();
            if (masterContent != null) {
                if (masterContent.isPdfDownloaded()) {
                    if (masterContent.isPdfUpdateAvailable()) {
                        if (internet) {
                            loginActivity.startMasterDownload();
                        } else {
                            loginActivity.openMasterContent();
                        }
                    } else {
                        loginActivity.openMasterContent();
                    }
                } else {
                    if (internet) {
                        loginActivity.startMasterDownload();
                    } else {
                        if (otherContents) {
                            loginActivity.openLibraryFragment();
                        } else {
                            // Do Nothing
                        }
                    }
                }
            } else {
                loginActivity.openLibraryFragment();
            }
        }
    }

    public boolean isLibraryMustBeEnabled() {

        if (GalePressApplication.getInstance().isTestApplication())
            return true;

        List<L_Content> list = GalePressApplication.getInstance().getDatabaseApi().getAllContents(null);
        L_Content masterContent = getMasterContent();
        if (list != null && list.size() == 1 && masterContent != null && list.get(0).getId().intValue() == masterContent.getId().intValue()) {
            return false;
        } else {
            return true;
        }
    }

    public class DownloadPdfTask extends AsyncTask<ArrayList<String>, Integer, String> {
        File tempDirectory = null;
        File directory = null;
        L_Content content = null;
        long total;
        Activity activity;

        public DownloadPdfTask(Activity context, L_Content c) {
            this.activity = context;
            this.content = c;
        }

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            String remoteUrl = params[0].get(0);
            String contentId = params[0].get(1);
            this.content = getDatabaseApi().getContent(Integer.valueOf(contentId));
            String pdfFileName = params[0].get(2);
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                directory = new File(GalePressApplication.getInstance().getFilesDir() + "/" + contentId);
                tempDirectory = new File(GalePressApplication.getInstance().getFilesDir() + "/" + UUID.randomUUID().toString());
                tempDirectory.mkdir();

                File outputFile = new File(tempDirectory.getPath(), pdfFileName);
                URL url = new URL(remoteUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Logout.e("Adem", "Response Code : " + connection.getResponseCode() + " Response Message : " + connection.getResponseMessage());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                final long fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();


                output = new FileOutputStream(outputFile.getPath());
                //output = GalePressApplication.getInstance().getApplicationContext().openFileOutput(pdfFileName, Context.MODE_WORLD_READABLE);

                byte data[] = new byte[1024];
                total = 0;
                int count;
                long lastCtm = System.currentTimeMillis();
                long ctm;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        if (tempDirectory != null) {
                            deleteFolder(tempDirectory);
                        }
                        content.setPdfDownloading(false);
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) { // only if total length is known
//                        publishProgress((int) (total * 100 / fileLength));
                        ctm = System.currentTimeMillis();
                        if (lastCtm + 100 < ctm) {
                            lastCtm = ctm;
                            publishProgress((int) total, (int) fileLength);
                        }
                    }
                    output.write(data, 0, count);
                }

                outputFile.setReadable(true, false);
                publishProgress((int) total, (int) fileLength);
                if (directory.exists()) {
                    deleteFolder(directory);
                }
                final boolean isUpdate = content.isPdfDownloaded();

                tempDirectory.renameTo(directory);
                Decompress decompressor = new Decompress(directory + "/" + pdfFileName, directory + "/", GalePressApplication.getInstance().getApplicationContext());
                Logout.e("Adem", "Content Directory : " + directory.getPath() + "");
                decompressor.unzip();
                if (checkDownloadSuccessfull(directory)) {
                    new File(directory + "/" + pdfFileName).delete();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            content.setPdfUpdateAvailable(false);
                            getDatabaseApi().updateContent(content, true);

                            if (isUpdate) {
                                Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                                String udid = UUID.randomUUID().toString();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Calendar cal = Calendar.getInstance();
                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                Location location = GalePressApplication.getInstance().location;
                                L_Statistic statistic = new L_Statistic(udid, content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentUpdated, null, null, null);
                                GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);
                            } else {
                                Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                                String udid = UUID.randomUUID().toString();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Calendar cal = Calendar.getInstance();
                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                Location location = GalePressApplication.getInstance().location;
                                L_Statistic statistic = new L_Statistic(udid, content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentDownloaded, null, null, null);
                                GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);
                            }

                        }
                    });
                } else {
                    final String errorMessage = getErrorMessageFromXMLFile(directory, pdfFileName);
                    if (directory != null) {
                        deleteFolder(directory);
                    }
                    cancel(true);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GalePressApplication.getInstance(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

                }
                return null;
            } catch (Exception e) {
                Logout.e("Error", e.getLocalizedMessage());
                total = 0;
                if (tempDirectory != null) {
                    deleteFolder(tempDirectory);
                }
                Handler mainHandler = new Handler(GalePressApplication.getInstance().getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateCompleted();
                    }
                };
                mainHandler.post(myRunnable);
            }

            return null;

        }

        private boolean checkDownloadSuccessfull(File directory) {
            int fileCount = directory.list().length;
            return fileCount > 1;
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {
            final Integer[] p = progress;
            Handler mainHandler = new Handler(GalePressApplication.getInstance().getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    progressUpdate(content, p[0], p[1]);
                }
            };
            mainHandler.post(myRunnable);
        }

        @Override
        protected void onPreExecute() {

            //icerik indirildigi zaman downloaded ekrani aciksa update etmek icin
            if (GalePressApplication.getInstance().getMainActivity() != null) {
                MainActivity act = GalePressApplication.getInstance().getMainActivity();
                if (act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0)
                    act.getDownloadedLibraryFragment().updateGridView();
            }

            if (GalePressApplication.getInstance().getLibraryActivity() != null)
                GalePressApplication.getInstance().getLibraryActivity().updateAdapterList(content, false);

            if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                if (content.getId().compareTo(GalePressApplication.getInstance().getContentDetailPopupActivity().getContent().getId()) == 0)
                    GalePressApplication.getInstance().getContentDetailPopupActivity().setContent(content);
                GalePressApplication.getInstance().getContentDetailPopupActivity().update();
            }
        }

        @Override
        protected void onCancelled() {
            if (tempDirectory != null) {
                deleteFolder(tempDirectory);
            }
            content.setPdfDownloading(false);
            getDatabaseApi().updateContent(content, true);
            if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                if (content.getId().compareTo(GalePressApplication.getInstance().getContentDetailPopupActivity().getContent().getId()) == 0)
                    GalePressApplication.getInstance().getContentDetailPopupActivity().setContent(content);
                GalePressApplication.getInstance().getContentDetailPopupActivity().update();
            }

            super.onCancelled();
        }


        @Override
        protected void onPostExecute(String a) {
            if (total != 0) {
                this.content.setPdfDownloaded(true);
                this.content.setPdfDownloading(false);
                getDatabaseApi().updateContent(this.content, true);

                if (GalePressApplication.getInstance().getCurrentActivity() != null) {
                    if (this.content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(LaunchActivity.class)) {
                        LaunchActivity launchActivity = (LaunchActivity) GalePressApplication.getInstance().getCurrentActivity();
                        launchActivity.openMasterContent();
                    } else if (this.content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(ViewerLoginActivity.class)) {
                        ViewerLoginActivity loginActivity = (ViewerLoginActivity) GalePressApplication.getInstance().getCurrentActivity();
                        loginActivity.openMasterContent();
                    } else if (this.content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(UserLoginActivity.class)) {
                        UserLoginActivity userLoginActivity = (UserLoginActivity) GalePressApplication.getInstance().getCurrentActivity();
                        userLoginActivity.openMasterContent();
                    }
                }
            } else {
                updateCompleted();
            }

            if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                if (content.getId().compareTo(GalePressApplication.getInstance().getContentDetailPopupActivity().getContent().getId()) == 0)
                    GalePressApplication.getInstance().getContentDetailPopupActivity().setContent(content);
                GalePressApplication.getInstance().getContentDetailPopupActivity().update();
            }

            //icerik indirildigi zaman indirilenler ekrani aciksa update etmek icin
            if (GalePressApplication.getInstance().getMainActivity() != null) {
                MainActivity act = GalePressApplication.getInstance().getMainActivity();
                if (act.getDownloadedLibraryFragment() != null)
                    act.getDownloadedLibraryFragment().updateGridView();
            }
        }

    }

    private void progressUpdate(L_Content content, long total, long fileLength) {
        if (GalePressApplication.getInstance().getCurrentActivity() != null) {
            if (content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(LaunchActivity.class)) {
                LaunchActivity launchActivity = (LaunchActivity) GalePressApplication.getInstance().getCurrentActivity();
                launchActivity.progressUpdate(total, fileLength);
            } else if (content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(ViewerLoginActivity.class)) {
                ViewerLoginActivity loginActivity = (ViewerLoginActivity) GalePressApplication.getInstance().getCurrentActivity();
                loginActivity.progressUpdate(total, fileLength);
            } else if (content.isMaster() && GalePressApplication.getInstance().getCurrentActivity().getClass().equals(UserLoginActivity.class)) {
                UserLoginActivity loginActivity = (UserLoginActivity) GalePressApplication.getInstance().getCurrentActivity();
                loginActivity.progressUpdate(total, fileLength);
            } else {
                ContentHolderAdapter.ViewHolder viewHolder = getViewHolderForContent(content);

                if (viewHolder != null) {
                    if (viewHolder.content.getId().compareTo(content.getId()) == 0) {
                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                        viewHolder.progressBar.setProgress((int) (total * 100 / fileLength));
                    }
                }
            }
        } else if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null
                && GalePressApplication.getInstance().getContentDetailPopupActivity().content.getId().compareTo(content.getId()) == 0) {
            ContentDetailPopupActivity act = GalePressApplication.getInstance().getContentDetailPopupActivity();
            act.contentHolder.progressBar.setProgress((int) (total * 100 / fileLength));
        }
    }

    public class StatisticSendTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            List<L_Statistic> statistics = getDatabaseApi().getAllStatistics();
            if (statistics != null) {
                for (L_Statistic statistic : statistics) {
                    if (isCancelled() == false) {
                        postStatistic(statistic);
                    } else {
                        break;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            cancel(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancel(true);
        }
    }

    public String getBuildVersion() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }

    private Uri.Builder getWebServiceUrlBuilder() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.galepress.com")
                .appendPath("webservice")
                .appendPath(webServisVersion);
        return builder;
    }

    private Uri.Builder getFullTextSearchWebServiceUrlBuilder() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.galepress.com")
                .appendPath("search");
        return builder;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_TYPE_COVER_IMAGE) {
                getCoverImageVersionToUpdate(msg.arg2, false, false);
            } else if (msg.what == MESSAGE_TYPE_COVER_PDF_DOWNLOAD) {
            }

        }
    };

    public DataApi() {
    }

    public DatabaseApi getDatabaseApi() {
        if (databaseApi == null) {
            databaseApi = GalePressApplication.getInstance().getDatabaseApi();
        }
        return databaseApi;
    }

    public static boolean isConnectedToInternet() {
        try {
            boolean result;
            ConnectivityManager connectivityManager = (ConnectivityManager) GalePressApplication.getInstance().getSystemService(GalePressApplication.getInstance().CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            if (!result) {
                Toast.makeText(GalePressApplication.getInstance(), GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_1), Toast.LENGTH_LONG).show();
            }
            return result;
        } catch (Exception e) {
            Logout.e("Adem", "Error on Checking internet connection");
            return false;
        }

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public void updateApplication() {
        if (isConnectedToInternet() && !isBlockedFromWS) {
            getRemoteApplicationVersion();
        } else if (!isBlockedFromWS) {
            updateCompleted();
        }
    }

    public L_Content getMasterContent() {
        List<L_Content> allContents = getDatabaseApi().getAllContents(null);

        if (allContents != null) {
            for (L_Content content : allContents) {
                if (content.isMaster()) {
                    return content;
                }
            }
        }

        return null;
    }

    public void commitStatisticsToDB(L_Statistic statistic) {
        if (!GalePressApplication.getInstance().isTestApplication())
            getDatabaseApi().createStatistic(statistic);
    }


    public void login(final String token, final String userId, final String email, final String name, final String last_name,
                      final Activity activity, boolean isFacebookLogin, String uname, String password) {

        if (isConnectedToInternet()) {
            getBuildVersion();
            Logout.e("Adem", "INC");
            GalePressApplication.getInstance().incrementRequestCount();
            GalePressApplication application = GalePressApplication.getInstance();
            RequestQueue requestQueue = application.getRequestQueue();
            final JsonObjectRequest request;

            Integer applicationID;
            if (GalePressApplication.getInstance().isTestApplication()) {
                applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
            } else {
                applicationID = application.getApplicationId();
            }

            final String gcmRegisterId = GCMRegistrar.getRegistrationId(GalePressApplication.getInstance().getApplicationContext());

            Uri.Builder uriBuilder = getWebServiceUrlBuilder();
            uriBuilder.appendPath("applications");
            if (!isFacebookLogin)
                uriBuilder.appendPath("login_application");
            else
                uriBuilder.appendPath("fblogin");
            if (!isFacebookLogin) {
                uriBuilder.appendQueryParameter("username", uname);
                uriBuilder.appendQueryParameter("password", password);
                uriBuilder.appendQueryParameter("applicationID", applicationID.toString());
            } else {
                uriBuilder.appendQueryParameter("clientLanguage", activity.getResources().getString(R.string.language));
                uriBuilder.appendQueryParameter("applicationID", applicationID.toString());
                uriBuilder.appendQueryParameter("facebookToken", token);
                uriBuilder.appendQueryParameter("facebookUserId", userId);
                uriBuilder.appendQueryParameter("facebookEmail", email);
                uriBuilder.appendQueryParameter("name", name);
                uriBuilder.appendQueryParameter("surname", last_name);
            }
            uriBuilder.appendQueryParameter("deviceToken", gcmRegisterId);

            request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.isNull("accessToken")) {
                                    String accessToken = response.getString("accessToken");
                                    if (accessToken != null && accessToken.length() != 0) {
                                        GalePressApplication.getInstance().editMemberShipList(true, response);
                                        ((UserLoginActivity) activity).getUpdateDialog().setMessage(activity.getResources().getString(R.string.Restore) + "...");
                                        GalePressApplication.getInstance().restorePurchasedProductsFromMarket(true, activity, ((UserLoginActivity) activity).getUpdateDialog());
                                    } else {
                                        GalePressApplication.getInstance().editMemberShipList(false, null);

                                        if (activity instanceof UserLoginActivity) {
                                            ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_0));
                                        }
                                    }
                                } else if (!response.isNull("status")) {
                                    int code = response.getInt("status");
                                    if (code == 160) {
                                        if (activity instanceof UserLoginActivity) {
                                            ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_160));
                                        }
                                    } else if (code == 140) {
                                        if (activity instanceof UserLoginActivity) {
                                            ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_140));
                                        }
                                    } else {
                                        if (activity instanceof UserLoginActivity) {
                                            ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_0));
                                        }
                                    }
                                } else {
                                    GalePressApplication.getInstance().editMemberShipList(false, null);
                                    if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_0));
                                    }
                                }


                                Logout.e("Adem", "DECREMENT");
                                GalePressApplication.getInstance().decrementRequestCount();
                            } catch (Exception e) {
                                GalePressApplication.getInstance().editMemberShipList(false, null);
                                if (activity instanceof UserLoginActivity) {
                                    ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_0));
                                }
                                e.printStackTrace();
                                Logout.e("Adem", "DECREMENT");
                                GalePressApplication.getInstance().decrementRequestCount();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.getMessage() != null) {
                                if (error.getMessage().toLowerCase().contains("160")) {
                                    if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_160));
                                    }
                                } else if (error.getMessage().toLowerCase().contains("140")) {
                                    if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_140));
                                    }
                                } else {
                                    if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_0));
                                    }
                                }
                                VolleyLog.e("Error: ", error.getMessage());
                            }
                            GalePressApplication.getInstance().editMemberShipList(false, null);
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();

                        }
                    });
            request.setShouldCache(Boolean.FALSE);
            requestQueue.add(request);
        } else {
            if (activity instanceof UserLoginActivity) {
                ((UserLoginActivity) activity).internetConnectionWarning();
            }

        }
    }

    public void getCustomerApplications(final ViewerLoginActivity activity, final boolean isFacebookLogin) {
        if (isConnectedToInternet()) {
            GalePressApplication application = GalePressApplication.getInstance();
            RequestQueue requestQueue = application.getRequestQueue();
            final JsonArrayRequest request;

            Uri.Builder uriBuilder = getWebServiceUrlBuilder();
            uriBuilder.appendPath("applications");
            uriBuilder.appendPath("authorized_application_list");

            if (isFacebookLogin) {
                uriBuilder.appendQueryParameter("userFacebookID", application.getTestApplicationLoginInf().getFacebookUserId());
                uriBuilder.appendQueryParameter("userFbEmail", application.getTestApplicationLoginInf().getFacebookEmail());
            } else {
                uriBuilder.appendQueryParameter("username", application.getTestApplicationLoginInf().getUsername());
                uriBuilder.appendQueryParameter("password", application.getTestApplicationLoginInf().getPassword());
            }

            request = new JsonArrayRequest(Request.Method.POST, uriBuilder.build().toString(),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                final ArrayList<ApplicationIds> ids = new ArrayList<ApplicationIds>();
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject obj = (JSONObject) response
                                            .get(i);
                                    ids.add(new ApplicationIds(obj.getString("Name"), obj.getString("ApplicationID")));
                                }

                                if (ids.size() != 0) {
                                    if (ids.size() == 1) {
                                        GalePressApplication.getInstance().getTestApplicationLoginInf().setApplicationId(ids.get(0).getId());
                                        GalePressApplication.getInstance().reCreateApplicationTableData(ids.get(0).getId());
                                        deleteEverythingAndUpdateApplication();
                                    } else {


                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        builder.setTitle(activity.getResources().getString(R.string.select_customer_application));
                                        builder.setNegativeButton(activity.getResources().getString(R.string.cancel),
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        activity.customWarning(null);
                                                    }
                                                });

                                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                                activity,
                                                android.R.layout.select_dialog_singlechoice);
                                        for (ApplicationIds appId : ids) {
                                            arrayAdapter.add(appId.getName());
                                        }
                                        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                GalePressApplication.getInstance().getTestApplicationLoginInf().setApplicationId(ids.get(which).getId());
                                                GalePressApplication.getInstance().reCreateApplicationTableData(ids.get(which).getId());
                                                deleteEverythingAndUpdateApplication();
                                            }
                                        });
                                        builder.show();
                                    }
                                } else {
                                    activity.customWarning(activity.getResources().getString(R.string.no_customer_application));
                                }

                            } catch (Exception e) {
                                activity.customWarning(activity.getResources().getString(R.string.WARNING_0));
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.getMessage() != null) {
                                if (error.getMessage().toLowerCase().contains("140"))
                                    activity.customWarning(activity.getResources().getString(R.string.WARNING_140));
                                else if (error.getMessage().toLowerCase().contains("141"))
                                    activity.customWarning(activity.getResources().getString(R.string.WARNING_141));
                                else
                                    activity.customWarning(activity.getResources().getString(R.string.WARNING_0));
                                VolleyLog.e("Error: ", error.getMessage());
                            } else {
                                activity.customWarning(activity.getResources().getString(R.string.WARNING_0));
                            }


                        }
                    }
            );
            request.setShouldCache(Boolean.FALSE);
            requestQueue.add(request);
        } else {
            activity.internetConnectionWarning();
        }
    }

    public void getAppDetail(Context mContext) {
        this.mContext = mContext;
        if (isConnectedToInternet()) {

            getBuildVersion();

            GalePressApplication application = GalePressApplication.getInstance();
            RequestQueue requestQueue = application.getRequestQueue();

            Integer applicationID;
            if (GalePressApplication.getInstance().isTestApplication()) {
                applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
            } else {
                applicationID = application.getApplicationId();
            }

            String udid = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            String osVersion = "";
            String release = Build.VERSION.RELEASE;
            int sdkVersion = Build.VERSION.SDK_INT;
            osVersion = sdkVersion + "_" + release;

            final String gcmRegisterId = GCMRegistrar.getRegistrationId(GalePressApplication.getInstance().getApplicationContext());

            JsonObjectRequest request;


            Uri.Builder uriBuilder = getWebServiceUrlBuilder();

            uriBuilder.appendPath("applications");
            uriBuilder.appendPath(applicationID.toString());
            uriBuilder.appendPath("detail");
            uriBuilder.appendQueryParameter("deviceType", "android");
            uriBuilder.appendQueryParameter("osVersion", osVersion);
            uriBuilder.appendQueryParameter("deviceDetail", getDeviceName());
            uriBuilder.appendQueryParameter("deviceToken", gcmRegisterId);
            if (GalePressApplication.getInstance().getUserInformation() != null
                    && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                    && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
                uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken()); // Kullanici login oldugu zaman deviceToken alınamamışsa burda gönderilen accesToken ile tekrar eşleştiriyoruz.
            uriBuilder.appendQueryParameter("buildVersion", getBuildVersion());
            uriBuilder.appendQueryParameter("udid", udid);

            request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                R_AppDetail appDetail = new R_AppDetail(response);

                                GalePressApplication.getInstance().prepareSubscriptions(response);
                                if (appDetail.getForce() == R_AppDetail.FORCE_WARN) {
                                    isBlockedFromWS = false;
                                    // Warn user to update app.
                                    final String marketUrl = appDetail.getAndroidLink();
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                                    alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.forceUpdateWarnMessage));

                                    alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (marketUrl != null && !marketUrl.isEmpty()) {
                                                try {
                                                    String packageName = marketUrl.substring(marketUrl.indexOf("?id=") + 4, marketUrl.length());
                                                    Uri marketUri = Uri.parse("market://details?id=" + packageName);
                                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                                    GalePressApplication.getInstance().getLibraryActivity().startActivity(marketIntent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                        }
                                    });
                                    alertDialog.setNegativeButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    alertDialog.show();
                                } else if (appDetail.getForce() == R_AppDetail.FORCE_BLOCK_APP || appDetail.getForce() == R_AppDetail.FORCE_BLOCK_AND_DELETE) {
                                    // App is blocked. Lock all content features.
                                    isBlockedFromWS = true;
                                    final String marketUrl = appDetail.getAndroidLink();
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                                    alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.forceUpdateBlockMessage));

                                    alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.goToMarket), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (marketUrl != null && !marketUrl.isEmpty()) {
                                                try {
                                                    String packageName = marketUrl.substring(marketUrl.indexOf("?id=") + 4, marketUrl.length());
                                                    Uri marketUri = Uri.parse("market://details?id=" + packageName);
                                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                                    GalePressApplication.getInstance().getLibraryActivity().startActivity(marketIntent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                        }
                                    });
                                    alertDialog.setNegativeButton(GalePressApplication.getInstance().getString(R.string.IPTAL), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialog.show();
                                    if (appDetail.getForce() == R_AppDetail.FORCE_BLOCK_AND_DELETE) {
                                        // Delete all content
                                        deleteEverything();
                                    }
                                } else {
                                    isBlockedFromWS = false;
                                    Logout.e("Adem", "Do Nothing with : " + appDetail.getForce().toString());
                                }
                            } catch (Exception e) {
                                Logout.e("Adem", e.getMessage() + e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error != null && error.getMessage() != null) {
                                VolleyLog.e("Error: ", error.getMessage());
                            }

                        }
                    }
            );
            request.setShouldCache(Boolean.FALSE);
            requestQueue.add(request);
        }
    }

    public void deleteEverythingAndUpdateApplication() {

        List categories = getDatabaseApi().getAllCategories();
        for (int i = 0; i < categories.size(); i++) {
            L_Category category = (L_Category) categories.get(i);
            List contents = getDatabaseApi().getAllContentsByCategory(category);
            for (int j = 0; j < contents.size(); j++) {
                L_Content content = (L_Content) contents.get(j);
                deleteContent(content);
            }
            deleteCategory(category);
        }
        L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
        application.setVersion(-1);
        getDatabaseApi().updateApplication(application);
        updateApplication();

    }

    public void deleteEverything() {
        List categories = getDatabaseApi().getAllCategories();
        for (int i = 0; i < categories.size(); i++) {
            L_Category category = (L_Category) categories.get(i);
            List contents = getDatabaseApi().getAllContentsByCategory(category);
            for (int j = 0; j < contents.size(); j++) {
                L_Content content = (L_Content) contents.get(j);
                deleteContent(content);
            }
            deleteCategory(category);
        }
        L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
        application.setVersion(-1);
        getDatabaseApi().updateApplication(application);
    }

    private void downloadFile(final String remoteUrl, L_Content content) {
        DownloadPdfTask pdfTask = null;
        ArrayList<String> parameters = new ArrayList<String>();
        parameters.add(remoteUrl);
        parameters.add(content.getId().toString());
        parameters.add(content.getPdfFileName());

        downloadPdfTask = new DownloadPdfTask(null, content);
        pdfTask = downloadPdfTask;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pdfTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parameters);
        } else {
            pdfTask.execute(parameters);
        }
    }

    public void getCoverImage(L_Content content, final int type, int width, int height) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(content.getId().toString());
        uriBuilder.appendPath("cover-image");
        uriBuilder.appendQueryParameter("size", Integer.toString(type));
        uriBuilder.appendQueryParameter("width", Integer.toString(width));
        uriBuilder.appendQueryParameter("height", Integer.toString(height));

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentFileUrl contentCoverImage = new R_ContentFileUrl(response);
                            if (contentCoverImage.getError() != "") {
                                L_Content content = getDatabaseApi().getContent(contentCoverImage.getContentID());
                                if (type == 0) {
                                    content.updateWithImageDownloadUrl(contentCoverImage.getUrl(), true);
                                    getDatabaseApi().updateContent(content, false);

                                    if (GalePressApplication.getInstance().getLibraryActivity() != null)
                                        GalePressApplication.getInstance().getLibraryActivity().updateAdapterList(content, true);

                                } else {
                                    content.updateWithImageDownloadUrl(contentCoverImage.getUrl(), false);
                                    getDatabaseApi().updateContent(content, false);


                                }

                                getCoverImageVersionToUpdate(content.getId(), true, false);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void cancelDownload(Boolean confirmed, final Context context, final L_Content content) {
        if (confirmed) {
            downloadPdfTask.cancel(true);
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
            alertDialog.setMessage(context.getString(R.string.CONFIRM_3));

            alertDialog.setPositiveButton(context.getString(R.string.EVET), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cancelDownload(true, context, content);
                }
            });
            alertDialog.setNegativeButton(context.getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    //GalePressApplication.getInstance().getLibraryActivity().updateGridView();
                    GalePressApplication.getInstance().getLibraryActivity().updateAdapterList(content, false);
                    if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null)
                        GalePressApplication.getInstance().getContentDetailPopupActivity().update();
                }
            });
            alertDialog.show();
        }
    }

    public void getPdf(final L_Content content, final Context context) {
        //GalePressApplication.getInstance().getLibraryActivity().updateGridView();
        GalePressApplication.getInstance().getLibraryActivity().updateAdapterList(content, false);
        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null)
            GalePressApplication.getInstance().getContentDetailPopupActivity().update();
        content.setPdfDownloading(true);
        getDatabaseApi().updateContent(content, false);
        if (downloadPdfTask != null && (downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
            alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.CONFIRM_2));

            alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.EVET), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cancelDownload(true, context, content);
                    if (content.isPdfUpdateAvailable()) {
                        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                            GalePressApplication.getInstance().getContentDetailPopupActivity().contentHolder.updateButton.setEnabled(false);
                        }

                    } else {
                        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                            GalePressApplication.getInstance().getContentDetailPopupActivity().contentHolder.downloadButton.setEnabled(false);
                        }
                    }
                    if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                        GalePressApplication.getInstance().getContentDetailPopupActivity().contentHolder.downloadButton.setEnabled(false);
                    }
                    downloadPdf(content);
                }
            });
            alertDialog.setNegativeButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        } else {
            if (content.isProtected()) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.SIFRE));
                alert.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_2));
                final EditText input = new EditText(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        content.setPassword(input.getText().toString());
                        GalePressApplication.getInstance().getDatabaseApi().updateContent(content, false);
                        dialog.cancel();
                        downloadPdf(content);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        content.setPdfDownloading(false);
                        getDatabaseApi().updateContent(content, false);
                        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                            GalePressApplication.getInstance().getContentDetailPopupActivity().contentHolder.downloadButton.stopAnim();
                            GalePressApplication.getInstance().getContentDetailPopupActivity().update();
                        }
                        dialog.cancel();
                    }
                });
                alert.show();
            } else {
                downloadPdf(content);
            }
        }
    }

    public void deletePdf(final Integer id, Context context) {
        final L_Content content = getDatabaseApi().getContent(id);
        if (content != null) {
            if (content.isPdfDownloaded()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.CONFIRM_1));

                alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.EVET), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File directory = new File(GalePressApplication.getInstance().getFilesDir() + "/" + id);
                        deleteFolder(directory);
                        L_Content content = getDatabaseApi().getContent(id);
                        content.setPdfDownloaded(false);
                        content.setPdfUpdateAvailable(false);
                        getDatabaseApi().updateContent(content, true);

                        Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        String udid = UUID.randomUUID().toString();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Location location = GalePressApplication.getInstance().location;
                        L_Statistic statistic = new L_Statistic(udid, content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentDeleted, null, null, null);
                        GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

                        L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                        application.setVersion(application.getVersion() - 1);
                        getDatabaseApi().updateApplication(application);

                    /*
                     * Silinen icerigin son acilan sayfa bilgileri siliniyor.
                     * Eger silinmezse icerik tekrar indirilirse silinmeden onceki sayfadan basliyor.
                    * */
                        try {
                            File samplePdfFile = new File(content.getPdfPath(), "file.pdf");
                            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
                            String path = Uri.decode(uri.getEncodedPath());
                            int lastSlashPos = path.lastIndexOf('/');
                            int penultimateSlashPos = (path.substring(0, path.lastIndexOf("/"))).lastIndexOf('/');
                            String mFileName = new String((lastSlashPos == -1 || penultimateSlashPos == -1)
                                    ? path
                                    : path.substring(penultimateSlashPos + 1, lastSlashPos));

                            SharedPreferences prefs = GalePressApplication.getInstance().getApplicationContext().getSharedPreferences("pages", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = prefs.edit();

                            edit.putInt("page" + mFileName, 0);
                            edit.putInt("lastPortraitPageIndex" + mFileName, 0);
                            edit.commit();
                        } catch (Exception e) {
                            Log.e("Content_Delete", "" + e.toString());
                        }

                        //icerik silindigi zaman downloaded ekrani aciksa update etmek icin
                        if (GalePressApplication.getInstance().getMainActivity() != null) {
                            MainActivity act = GalePressApplication.getInstance().getMainActivity();
                            if (act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0)
                                act.getDownloadedLibraryFragment().updateGridView();
                        }


                        updateApplication();

                        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                            if (content.getId().compareTo(GalePressApplication.getInstance().getContentDetailPopupActivity().getContent().getId()) == 0)
                                GalePressApplication.getInstance().getContentDetailPopupActivity().setContent(content);
                            GalePressApplication.getInstance().getContentDetailPopupActivity().update();
                        }

                    }
                });
                alertDialog.setNegativeButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //GalePressApplication.getInstance().getLibraryActivity().updateGridView();
                        GalePressApplication.getInstance().getLibraryActivity().updateAdapterList(content, false);
                        if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                            GalePressApplication.getInstance().getContentDetailPopupActivity().update();
                        }
                    }
                });
                alertDialog.show();
            }
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.WARNING_0), Toast.LENGTH_SHORT).show();
            //icerik herhangi bir sebepten dolayi silenemezse popupekranlarini update etmek icin
            if (GalePressApplication.getInstance().getMainActivity() != null) {
                MainActivity act = GalePressApplication.getInstance().getMainActivity();
                if (act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0)
                    act.getDownloadedLibraryFragment().updateGridView();
            }

            if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                GalePressApplication.getInstance().getContentDetailPopupActivity().update();
            }
        }

    }

    public void downloadPdf(L_Content content) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();

        JsonObjectRequest request;

        //http://www.galepress.com/ws/v100/contents/1075/file
        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(content.getId().toString());
        uriBuilder.appendPath("file");

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentFileUrl contentPdfFile = new R_ContentFileUrl(response);
                            if (contentPdfFile.getError().isEmpty()) {
                                L_Content content = getDatabaseApi().getContent(contentPdfFile.getContentID());
                                if (content.isProtected() && content.getPassword() != null)
                                    downloadFile(contentPdfFile.getUrl() + content.getPassword(), content);
                                else
                                    downloadFile(contentPdfFile.getUrl(), content);
                            } else {
                                if (mContext != null) {
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(
                                            mContext);
                                    alert.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alert.setMessage(contentPdfFile.getError());
                                    alert.setCancelable(true);
                                    alert.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alert.show();

                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void downloadUpdatedImage(String url, final String fileName, final int id, final boolean isLarge) {
        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                Log.e("downloadTry", "" + id);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                saveImage(bitmap, fileName, id, isLarge);
                ImageLoader.getInstance().getMemoryCache().clear();
                Log.e("imageUpdate", "" + isLarge);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public void saveImage(Bitmap bitmap, String fileName, int id, boolean isLarge) {
        File f = new File(GalePressApplication.getInstance().getFilesDir(), fileName);
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            getCoverImageVersionToUpdate(id, false, isLarge);
        } catch (Exception e) {
            f.delete();
        }
    }

    public void getCoverImageVersionToUpdate(Integer id, final boolean islocalVersionUpdate, final boolean isLarge) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();

        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(id.toString());
        uriBuilder.appendPath("detail");

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentDetail remoteContent = new R_ContentDetail(response);
                            L_Content localContent = getDatabaseApi().getContent(remoteContent.getContentID());
                            if (islocalVersionUpdate) {
                                localContent.setCoverImageVersion(remoteContent.getContentCoverImageVersion());
                                if (localContent.getRemoteCoverImageVersion() == -1) {
                                    localContent.setCoverImageRemoteVersion(remoteContent.getContentCoverImageVersion());
                                }
                                if (localContent.getRemoteLargeCoverImageVersion() == -1) {
                                    localContent.setRemoteLargeCoverImageVersion(remoteContent.getContentCoverImageVersion());
                                }

                            } else {
                                if (isLarge)
                                    localContent.setRemoteLargeCoverImageVersion(remoteContent.getContentCoverImageVersion());
                                else
                                    localContent.setCoverImageRemoteVersion(remoteContent.getContentCoverImageVersion());
                            }
                            localContent.setVersion(remoteContent.getContentVersion());
                            getDatabaseApi().updateContent(localContent, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }


    public void startStatisticSend() {
        if ((statisticSendTask == null || statisticSendTask.getStatus() != AsyncTask.Status.RUNNING) && isConnectedToInternet() && !GalePressApplication.getInstance().isTestApplication()) {
            statisticSendTask = new StatisticSendTask();
            statisticSendTask.execute();
        }
    }

    public void stopStatisticSend() {
        if (statisticSendTask != null && statisticSendTask.getStatus() == AsyncTask.Status.RUNNING) {
            statisticSendTask.cancel(true);
        }
    }

    private void postStatistic(final L_Statistic statistic) {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = null;
        RequestQueue requestQueue = application.getRequestQueue4Statistic();
        JsonObjectRequest request;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("statistics");

        final String gcmRegisterId = GCMRegistrar.getRegistrationId(GalePressApplication.getInstance().getApplicationContext());

        final String id = UUID.randomUUID().toString();
        uriBuilder.appendQueryParameter("id", id);
        uriBuilder.appendQueryParameter("type", statistic.getType().toString());
        uriBuilder.appendQueryParameter("time", statistic.getTime());
        uriBuilder.appendQueryParameter("lat", statistic.getLat() != null ? statistic.getLat().toString() : "");
        uriBuilder.appendQueryParameter("lon", statistic.getLon() != null ? statistic.getLon().toString() : "");
        uriBuilder.appendQueryParameter("deviceID", gcmRegisterId);
        uriBuilder.appendQueryParameter("applicationID", applicationId.toString());
        uriBuilder.appendQueryParameter("contentID", statistic.getContentId() != null ? statistic.getContentId().toString() : "");
        uriBuilder.appendQueryParameter("page", statistic.getPage() != null ? statistic.getPage().toString() : "");
        uriBuilder.appendQueryParameter("param5", statistic.getParam5() != null ? statistic.getParam5() : "");
        uriBuilder.appendQueryParameter("param6", statistic.getParam6() != null ? statistic.getParam6() : "");
        uriBuilder.appendQueryParameter("param7", statistic.getParam7() != null ? statistic.getParam7() : "");

        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null, future, future);
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
        try {
            JSONObject response = future.get(); // this will block
            String error = response.getString("error");
            if (error != null && error.isEmpty()) {
                String response_id = response.getString("id");
                if (response_id != null && !response_id.isEmpty()) {
                    if (response_id.compareTo(id) == 0) {
                        getDatabaseApi().deleteStatistic(statistic);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeAllConCatsForContent(L_Content content) {
        List contentCategories = getDatabaseApi().getAllContentCategoryByContent(content);
        for (int i = 0; i < contentCategories.size(); i++) {
            L_ContentCategory contentCategory = (L_ContentCategory) contentCategories.get(i);
            getDatabaseApi().deleteContentCategory(contentCategory);
        }

    }

    private void createConCat(R_ContentDetail r_content, L_Content l_content) {
        ArrayList<R_Category> categories = r_content.getContentCategories();
        for (int i = 0; i < categories.size(); i++) {
            R_Category r_category = categories.get(i);
            L_Category l_category = databaseApi.getCategory(r_category.getCategoryID());
            L_ContentCategory l_contentCategory = new L_ContentCategory(l_category, l_content);
            getDatabaseApi().createContentCategory(l_contentCategory);
        }
    }

    private void getRemoteContent(R_Content content) {
        Logout.e("Adem", "INC");
        GalePressApplication.getInstance().incrementRequestCount();
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();

        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(content.getContentID().toString());
        uriBuilder.appendPath("detail");
        int seqno = requestQueue.getSequenceNumber();

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentDetail remoteContent = new R_ContentDetail(response);
                            L_Content localContent = getDatabaseApi().getContent(remoteContent.getContentID());
                            if (localContent == null) {
                                if (!remoteContent.isForceDelete()) {
                                    localContent = new L_Content(remoteContent);
                                    getDatabaseApi().createContent(localContent);
                                    createConCat(remoteContent, localContent);
                                }
                            } else {
                                if (remoteContent.isForceDelete()) {
                                    /*
                                    * Buraya contentbought kontrolu yapilip eger contentBought true ise silinmicek
                                    * */
                                    deleteContent(localContent);
                                } else {
                                    localContent.updateWithRemoteContent(remoteContent);
                                    getDatabaseApi().updateContent(localContent, true);
                                    removeAllConCatsForContent(localContent);
                                    createConCat(remoteContent, localContent);
                                }

                            }

                            if (!remoteContent.isForceDelete()) {
                                if (localContent.getPdfVersion() < remoteContent.getContentPdfVersion()) {
                                    // PDF Must be updated
                                    if (localContent.isPdfDownloaded()) {
                                        localContent.setPdfUpdateAvailable(true);
                                    }
                                    localContent.setPdfVersion(remoteContent.getContentPdfVersion());
                                    getDatabaseApi().updateContent(localContent, true);
                                }

                                if (localContent.getCoverImageVersion() < remoteContent.getContentCoverImageVersion()) {
                                    // cover image must be updated.
                                    float scale;
                                    try {
                                        DisplayMetrics metrics = GalePressApplication.getInstance().getApplicationContext().getResources().getDisplayMetrics();
                                        scale = metrics.density > 1 ? metrics.density : 1;
                                    } catch (Exception e) {
                                        scale = 1;
                                    }

                                    getCoverImage(localContent, 0, (int) (480 * scale), (int) (640 * scale));
                                    getCoverImage(localContent, 1, (int) (155 * scale), (int) (206 * scale));
                                } else {
                                    // Content Detail update edildi.
                                    localContent.setVersion(remoteContent.getContentVersion());
                                    getDatabaseApi().updateContent(localContent, true);
                                }
                                if (GalePressApplication.getInstance().getLibraryActivity() != null) {
                                    GalePressApplication.getInstance().getLibraryActivity().getContentHolderAdapter().notifyDataSetChanged();
                                }
                                if (GalePressApplication.getInstance().getContentDetailPopupActivity() != null) {
                                    if (localContent.getId().compareTo(GalePressApplication.getInstance().getContentDetailPopupActivity().getContent().getId()) == 0)
                                        GalePressApplication.getInstance().getContentDetailPopupActivity().setContent(localContent);
                                    GalePressApplication.getInstance().getContentDetailPopupActivity().update();
                                }
                            }


                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            VolleyLog.e("Error: ", error.getMessage());
                        }
                        Logout.e("Adem", "DECREMENT");
                        GalePressApplication.getInstance().decrementRequestCount();
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);

    }

    private void getRemoteAppCategories() {
        Logout.e("Adem", "INC");
        GalePressApplication.getInstance().incrementRequestCount();
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = application.getApplicationId();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;
        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("categories");


        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_AppCategories rAppCategories = new R_AppCategories(response);
                            for (R_Category category : rAppCategories.getCategories()) {
                                L_Category localCategory = getDatabaseApi().getCategory(category.getCategoryID());
                                if (localCategory == null) {
                                    localCategory = new L_Category(category);
                                    getDatabaseApi().createCategory(localCategory);
                                } else {
                                    localCategory.updateWithRemoteCategory(category);
                                    getDatabaseApi().updateCategory(localCategory);
                                }
                            }
                            // Category'nin sunucudan silinmis olmasi durumu icin local category'lerin sunucudan gelenler icinde olup olmadigini kontrol ediyoruz.
                            List<L_Category> localCategories = databaseApi.getAllCategories();
                            for (L_Category l_category : localCategories) {
                                Boolean deletedInServer = true;
                                for (R_Category r_category : rAppCategories.getCategories()) {
                                    if (l_category.getCategoryID().compareTo(r_category.getCategoryID()) == 0) {
                                        deletedInServer = false;
                                        break;
                                    }
                                }
                                if (deletedInServer) {
                                    deleteCategory(l_category);
                                }
                            }
                            getRemoteAppContents();
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                        Logout.e("Adem", "DECREMENT");
                        GalePressApplication.getInstance().decrementRequestCount();
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void restoreContentsAndSubscritonsFromServer(final Activity activity, final ProgressDialog progress) {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = null;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("contents");

        if (GalePressApplication.getInstance().isTestApplication()) {
            uriBuilder.appendQueryParameter("isTest", "1");
        }

        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_AppContents RAppContents = new R_AppContents(response);
                            if (!response.isNull("error") && response.getString("error").length() != 0) {
                                int code = response.getInt("status");
                                boolean isUpdate = false;
                                if (code == 160) { //Kullanici bulunamadi
                                    GalePressApplication.getInstance().setUserHaveActiveSubscription(false); // Kullanici yoksa server abonelik false
                                    GalePressApplication.getInstance().prepareMemberShipList();
                                    if (activity != null) {
                                        if (activity instanceof MainActivity)
                                            ((MainActivity) activity).updateMemberListAdapter();
                                    }
                                    List<L_Content> localContents = databaseApi.getAllContents(null);
                                    for (L_Content l_content : localContents) {
                                        if (l_content.isContentBought()) {
                                            l_content.setContentBought(false);
                                            getDatabaseApi().updateContent(l_content, false);
                                            isUpdate = true;
                                        }
                                    }
                                }

                                if (activity != null) {
                                    if (activity instanceof MainActivity) {
                                        if (progress != null && progress.isShowing())
                                            progress.dismiss();
                                        ((MainActivity) activity).logout();
                                        Toast.makeText(activity, activity.getResources().getString(R.string.WARNING_160), Toast.LENGTH_SHORT).show();
                                        if (isUpdate) {
                                            MainActivity act = (MainActivity) activity;
                                            if (act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0
                                                    || act.mTabHost.getCurrentTabTag().compareTo(MainActivity.LIBRARY_TAB_TAG) == 0)
                                                act.getLibraryFragment().updateGridView();
                                        }
                                    } else if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).customFailLoginWarning(activity.getResources().getString(R.string.WARNING_160));
                                    }
                                }
                            } else {

                                /*
                                * Eger kullanicinin aboneligi varsa ama servisten abonelik false gelmisse
                                * Kullanicinin aboneliginin sonladigi icin bilgilendirme yapiliyor.
                                * */
                                if (GalePressApplication.getInstance().getUserInformation() != null
                                        && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                                        && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0
                                        && GalePressApplication.getInstance().isUserHaveActiveSubscription() && !response.getBoolean("ActiveSubscription")) {
                                    if (GalePressApplication.getInstance().getCurrentActivity() != null) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getCurrentActivity());
                                        alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                        alertDialog.setMessage(GalePressApplication.getInstance().getCurrentActivity().getString(R.string.subscription_finish));
                                        alertDialog.setPositiveButton(GalePressApplication.getInstance().getCurrentActivity().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                    }
                                }

                                GalePressApplication.getInstance().setUserHaveActiveSubscription(response.getBoolean("ActiveSubscription")); // Server abonelik aliniyor
                                GalePressApplication.getInstance().prepareMemberShipList();
                                if (activity != null) {
                                    if (activity instanceof MainActivity)
                                        ((MainActivity) activity).updateMemberListAdapter();
                                }

                                List<L_Content> localContents = databaseApi.getAllContents(null);
                                boolean isUpdate = false;
                                for (L_Content l_content : localContents) {
                                    for (R_Content r_content : RAppContents.getContents()) {
                                        if (l_content.getId().compareTo(r_content.getContentID()) == 0) {
                                            if (l_content.isContentBought() != r_content.isContentBought()) {
                                                l_content.setContentBought(r_content.isContentBought());
                                                l_content.setBuyable(r_content.isBuyable());
                                                getDatabaseApi().updateContent(l_content, false);
                                                isUpdate = true;
                                            }
                                        }
                                    }
                                }

                                if (activity != null) {
                                    if (activity instanceof MainActivity) {
                                        if (progress != null && progress.isShowing())
                                            progress.dismiss();
                                        if (isUpdate) {
                                            MainActivity act = (MainActivity) activity;
                                            if (act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0
                                                    || act.mTabHost.getCurrentTabTag().compareTo(MainActivity.LIBRARY_TAB_TAG) == 0)
                                                act.getLibraryFragment().updateGridView();
                                        }
                                    } else if (activity instanceof UserLoginActivity) {
                                        ((UserLoginActivity) activity).closeActivityAndUpdateApplication();
                                    }
                                }
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            if (activity != null) {
                                if (activity instanceof MainActivity) {
                                    if (progress != null && progress.isShowing())
                                        progress.dismiss();
                                } else if (activity instanceof UserLoginActivity) {
                                    ((UserLoginActivity) activity).closeActivityAndUpdateApplication();
                                } else if (activity instanceof UserLoginActivity) {
                                    ((UserLoginActivity) activity).closeActivityAndUpdateApplication();
                                }
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                        if (activity != null) {
                            if (activity instanceof MainActivity) {
                                if (progress != null && progress.isShowing())
                                    progress.dismiss();
                            } else if (activity instanceof UserLoginActivity) {
                                ((UserLoginActivity) activity).closeActivityAndUpdateApplication();
                            }
                        }
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    private void getRemoteAppContents() {
        Logout.e("Adem", "INC");
        GalePressApplication.getInstance().incrementRequestCount();
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = null;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("contents");

        if (GalePressApplication.getInstance().isTestApplication()) {
            uriBuilder.appendQueryParameter("isTest", "1");
        }

        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int numberOfContentWillBeUpdated = 0;
                            R_AppContents RAppContents = new R_AppContents(response);

                            ApplicationThemeColor.getInstance().setParameters(response);
                            GalePressApplication.getInstance().setBannerLink(response);
                            GalePressApplication.getInstance().setTabList(response);


                            /*
                            * Eger kullanicinin aboneligi varsa ama servisten abonelik false gelmisse
                            * Kullanicinin aboneliginin sonladigi icin bilgilendirme yapiliyor.
                            * */
                            if (GalePressApplication.getInstance().getUserInformation() != null
                                    && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                                    && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0
                                    && GalePressApplication.getInstance().isUserHaveActiveSubscription() && !response.getBoolean("ActiveSubscription")) {
                                if (GalePressApplication.getInstance().getCurrentActivity() != null) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getCurrentActivity());
                                    alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alertDialog.setMessage(GalePressApplication.getInstance().getCurrentActivity().getString(R.string.subscription_finish));
                                    alertDialog.setPositiveButton(GalePressApplication.getInstance().getCurrentActivity().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.show();
                                }
                            }

                            GalePressApplication.getInstance().setUserHaveActiveSubscription(response.getBoolean("ActiveSubscription")); // Server abonelik aliniyor
                            GalePressApplication.getInstance().prepareMemberShipList();
                            if (GalePressApplication.getInstance().getCurrentActivity() != null) {
                                if (GalePressApplication.getInstance().getCurrentActivity() instanceof MainActivity)
                                    ((MainActivity) GalePressApplication.getInstance().getCurrentActivity()).updateMemberListAdapter();
                            }


                            if (GalePressApplication.getInstance().isTestApplication()) {
                                if (RAppContents.getError() == "120") {
                                    // TODO: Show Error from string file. WARNING 120 and redirect to testLoginPage
                                    numberOfContentWillBeUpdated = -1;

                                } else if (RAppContents.getError() == "140") {
                                    // TODO: Show Error from string file. WARNING 140 and redirect to testLoginPage
                                    numberOfContentWillBeUpdated = -1;
                                } else {
                                    for (R_Content content : RAppContents.getContents()) {
                                        L_Content localContent = getDatabaseApi().getContent(content.getContentID());
                                        if (content.isForceDelete()) {
                                            if (localContent != null && !localContent.isContentBought()) {
                                                removeAllConCatsForContent(localContent);
                                                deleteContent(localContent);
                                                numberOfContentWillBeUpdated++;
                                            }
                                        } else {
                                            if (GalePressApplication.getInstance().isTestApplication() || (content.getContentStatus() && !content.getContentBlocked())) {
                                                Integer remoteContentVersion = content.getContentVersion();
                                                if (localContent == null || (localContent.getVersion() < remoteContentVersion)) {
                                                    // Content updating
                                                    numberOfContentWillBeUpdated++;
                                                    getRemoteContent(content);
                                                }
                                            } else {
                                                if (localContent != null && !localContent.isPdfDownloaded()) {
                                                    removeAllConCatsForContent(localContent);
                                                    deleteContent(localContent);
                                                }
                                            }
                                        }

                                    }
                                }
                            } else {
                                /*ArrayList<R_Content> temp = new ArrayList<R_Content>();
                                for(int i = 0; i < 1000; i++) {
                                    temp.addAll(RAppContents.getContents());

                                }
                                RAppContents.getContents().clear();
                                RAppContents.getContents().addAll(temp);
                                RAppContents.getContents().addAll(RAppContents.getContents());*/
                                for (R_Content content : RAppContents.getContents()) {
                                    L_Content localContent = getDatabaseApi().getContent(content.getContentID());
                                    if (content.isForceDelete()) {
                                        if (localContent != null && !localContent.isContentBought()) {
                                            removeAllConCatsForContent(localContent);
                                            deleteContent(localContent);
                                            numberOfContentWillBeUpdated++;
                                        }
                                    } else {
                                        if (GalePressApplication.getInstance().isTestApplication() || (content.getContentStatus() && !content.getContentBlocked())) {
                                            Integer remoteContentVersion = content.getContentVersion();
                                            if (localContent == null || (localContent.getVersion() < remoteContentVersion)) {
                                                // Content updating
                                                numberOfContentWillBeUpdated++;
                                                getRemoteContent(content);
                                            }
                                        } else {
                                            if (localContent != null && !localContent.isPdfDownloaded()) {
                                                removeAllConCatsForContent(localContent);
                                                deleteContent(localContent);
                                            }
                                        }
                                    }
                                }
                            }

                            // Content'in sunucudan silinmis olmasi durumu icin local content'lerin sunucudan gelenler icinde olup olmadigini kontrol ediyoruz.
                            List<L_Content> localContents = databaseApi.getAllContents(null);
                            for (L_Content l_content : localContents) {
                                Boolean deletedInServer = true;
                                for (R_Content r_content : RAppContents.getContents()) {
                                    if (l_content.getId().compareTo(r_content.getContentID()) == 0) {
                                        deletedInServer = false;
                                        break;
                                    }
                                }
                                if (deletedInServer) {
                                    if (!l_content.isPdfDownloaded())
                                        deleteContent(l_content);
                                    else if (l_content.isMaster()) { // Localde master olan icerik eger serverdan silinmisse localden de master ozelligi kaldirilmasi gerekiyor
                                        l_content.setMaster(false);
                                        getDatabaseApi().updateContent(l_content, false);
                                    }
                                }
                            }

                            // Content'lerin hic biri update olmamissa. Uygulamanin local versiyonunu update ediyorum.
                            if (numberOfContentWillBeUpdated == 0) {
                                updateApplicationVersion();
                            }
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();

                        } catch (Exception e) {
                            e.printStackTrace();
                            ApplicationThemeColor.getInstance().setParameters(null);
                            GalePressApplication.getInstance().setBannerLink(null);
                            GalePressApplication.getInstance().setTabList(null);
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ApplicationThemeColor.getInstance().setParameters(null);
                        GalePressApplication.getInstance().setBannerLink(null);
                        GalePressApplication.getInstance().setTabList(null);
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                        Logout.e("Adem", "DECREMENT");
                        GalePressApplication.getInstance().decrementRequestCount();
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void getRemoteApplicationVersion() {
        Logout.e("Adem", "INC");
        GalePressApplication.getInstance().incrementRequestCount();
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationID;
        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationID = application.getApplicationId();
        }

        RequestQueue requestQueue = application.getRequestQueue();
        int seqNo = requestQueue.getSequenceNumber();
        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationID.toString());
        uriBuilder.appendPath("version");
        //uriBuilder.appendQueryParameter("clientLanguage", GalePressApplication.getInstance().getResources().getString(R.string.language));
        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());


        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response: %s", response.toString());
                            R_AppVersion r_appVersion = new R_AppVersion(response);

                            //Verification datalari burda aliniyor.
                            GalePressApplication.getInstance().setAgeVerificationQuestion(response.has("ConfirmationMessage") ? response.getString("ConfirmationMessage") : "");
                            GalePressApplication.getInstance().setAgeVerificationActive(response.has("ShowDashboard") ? response.getBoolean("ShowDashboard") : false);
                            if (!GalePressApplication.getInstance().isAgeVerificationActive()) {
                                GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                            }


                            L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                            if (r_appVersion.getApplicationVersion() != null && application.getVersion() != null) {
                                if (application.getVersion() < r_appVersion.getApplicationVersion()) {
                                    getRemoteAppCategories();
                                } else if (application.getVersion() > r_appVersion.getApplicationVersion()) {
                                    /*
                                    * kullanici bazli app version tutuldugu icin kullanici logout olursa local version, remote versiondan buyuk olabiliyor.
                                    * */
                                    application.setVersion(r_appVersion.getApplicationVersion());
                                    getDatabaseApi().updateApplication(application);
                                    getRemoteAppCategories();
                                }
                            }
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                        Logout.e("Adem", "DECREMENT");
                        GalePressApplication.getInstance().decrementRequestCount();
                    }
                }
        );

        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void updateApplicationVersion() {
        Logout.e("Adem", "INC");
        GalePressApplication.getInstance().incrementRequestCount();
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();

        Integer applicationID;
        if (application.isTestApplication()) {
            applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationID = application.getApplicationId();
        }

        JsonObjectRequest request;
        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationID.toString());
        uriBuilder.appendPath("version");
        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());


        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response: %s", response.toString());
                            R_AppVersion r_appVersion = new R_AppVersion(response);
                            L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                            application.setVersion(r_appVersion.getApplicationVersion());
                            getDatabaseApi().updateApplication(application);
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logout.e("Adem", "DECREMENT");
                            GalePressApplication.getInstance().decrementRequestCount();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.getMessage() != null) {
                            Logout.e("Adem", "Error : " + error.getMessage());
                            VolleyLog.e("Error: ", error.getMessage());
                        }

                        Logout.e("Adem", "DECREMENT");
                        GalePressApplication.getInstance().decrementRequestCount();

                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);

    }

    private void deleteContent(L_Content content) {

        File coverImage = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());
        if (coverImage.exists()) {
            coverImage.delete();
        }
        File largeCoverImage = new File(GalePressApplication.getInstance().getFilesDir(), content.getBigCoverImageFileName());
        if (largeCoverImage.exists()) {
            largeCoverImage.delete();
        }
        File contentFolder = new File(GalePressApplication.getInstance().getFilesDir(), content.getId().toString());
        if (contentFolder.exists()) {
            deleteFolder(contentFolder);
        }
        if (content.getGridThumbCoverImagePath() != null) {
            File thumnailFolder = new File(GalePressApplication.getInstance().getFilesDir(), content.getGridThumbCoverImagePath());
            if (thumnailFolder.exists()) {
                deleteFolder(thumnailFolder);
            }
        }

        getDatabaseApi().deleteContent(content);
    }

    private void deleteCategory(L_Category category) {
        getDatabaseApi().deleteCategory(category);
    }

    private void deleteFolder(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteFolder(child);

        fileOrDirectory.delete();

    }

    public ContentHolderAdapter.ViewHolder getViewHolderForContent(L_Content content) {

        HeaderGridView gridView = GalePressApplication.getInstance().getLibraryActivity().gridview;
        for (int i = 0; i < gridView.getChildCount(); i++) {
            View view = gridView.getChildAt(i);
            if (view != null) {
                ContentHolderAdapter.ViewHolder viewHolder = (ContentHolderAdapter.ViewHolder) view.getTag();
                if (viewHolder != null && viewHolder.content != null && viewHolder.content.getId().compareTo(content.getId()) == 0) {
                    return viewHolder;
                }
            }
        }
        return null;
    }

    private String getErrorMessageFromXMLFile(File directory, String pdfFileName) {
        String errorMessage = null;
        try {
            File fXmlFile = new File(directory, pdfFileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Response");
            Node nNode = nList.item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                Node errorElementNode = eElement.getElementsByTagName("Error").item(0);
                Element errorElement = (Element) errorElementNode;
                String errorCodeString = errorElement.getAttribute("code");
                if (errorCodeString.equalsIgnoreCase("101")) {
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_101);
                } else if (errorCodeString.equalsIgnoreCase("102")) {
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_102);
                } else if (errorCodeString.equalsIgnoreCase("103")) {
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_103);
                } else if (errorCodeString.equalsIgnoreCase("104")) {
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_104);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

    public void sendReceipt(final String productId, final String purchaseToken, final String packageName, final ProgressDialog dialog, final Activity activity) {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = null;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("receipt");

        uriBuilder.appendQueryParameter("platformType", "android");
        uriBuilder.appendQueryParameter("productId", productId);
        uriBuilder.appendQueryParameter("packageName", packageName);
        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());
        uriBuilder.appendQueryParameter("purchaseToken", purchaseToken);

        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getString("error").length() == 0 && response.getInt("status") == 0) {
                                if (activity != null && dialog != null) {
                                    if (activity instanceof MainActivity)
                                        ((MainActivity) activity).completePurchase();
                                    else
                                        ((ContentDetailPopupActivity) activity).completePurchase();
                                    dialog.dismiss();
                                }
                            }
                        } catch (Exception e) {
                            if (dialog != null) {
                                Toast.makeText(activity, activity.getResources().getString(R.string.purchase_validation_fail), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (dialog != null) {
                            Toast.makeText(activity, activity.getResources().getString(R.string.purchase_validation_fail), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void restoreReceipt(final String productIds, final String purchaseTokens, final String packageName) {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = null;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("androidrestore");

        uriBuilder.appendQueryParameter("platformType", "android");
        uriBuilder.appendQueryParameter("productIds", productIds);
        uriBuilder.appendQueryParameter("packageName", packageName);
        if (GalePressApplication.getInstance().getUserInformation() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0)
            uriBuilder.appendQueryParameter("accessToken", GalePressApplication.getInstance().getUserInformation().getAccessToken());
        uriBuilder.appendQueryParameter("purchaseTokens", purchaseTokens);

        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getString("error").length() == 0 && response.getInt("status") == 0) {
                                /*
                                * Islem basarili olursa uygulama update ediliyor. Eger bi degisiklik olursa appversion artiyor kontrol ediliyor.
                                * */
                                updateApplication();
                            }
                        } catch (Exception e) {
                            Log.e("androidrestore", "parse error :" + response.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("androidrestore", error != null ? error.getMessage() : "");
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        int socketTimeout = 30000;// 30 sec. Array gonderdigimiz ve server tarafinda array islemi yapildigi icin request timeout oluyordu bunu engelledim (MG)
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }


    public void fullTextSearch(final String text, final MainActivity mainActivity){
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getFullTextSearchWebServiceUrlBuilder();
        uriBuilder.appendQueryParameter("query", text);
        uriBuilder.appendQueryParameter("applicationID", applicationId.toString());

        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /*if(mainActivity.searchDialog.isShowing()) {

                        }*/
                        try {
                            if (response != null && response.getInt("status") == 1) {
                                GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                                ArrayList<MenuSearchResult> contentList = new ArrayList<MenuSearchResult>();

                                JSONArray result = response.getJSONArray("result");

                                for (int i = 0; i < result.length(); i++) {
                                    try {
                                        JSONObject jsonObjectItem = result.getJSONObject(i);

                                        if (contentList.size() != 0) {

                                            boolean isArrayContainsContent = true;
                                            for (int j = 0; j < contentList.size(); j++) {
                                                if (contentList.get(j).getContentId().contains(jsonObjectItem.getString("contentId"))) {

                                                    MenuSearchResult temp = new MenuSearchResult();
                                                    temp.setContentId(jsonObjectItem.getString("contentId"));
                                                    temp.setContentTitle(jsonObjectItem.getString("contentId"));
                                                    temp.setPage(jsonObjectItem.getInt("page"));
                                                    temp.setText(jsonObjectItem.getString("highlightedText"));
                                                    GalePressApplication.getInstance().getMenuSearchResult().add(temp);

                                                    isArrayContainsContent = true;
                                                } else {
                                                    isArrayContainsContent = false;
                                                }
                                            }

                                            if (!isArrayContainsContent) {
                                                L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.parseInt(jsonObjectItem.getString("contentId")));
                                                if (content != null && content.isContentStatus()) {
                                                    MenuSearchResult temp = new MenuSearchResult();
                                                    temp.setContentId(content.getId().toString());
                                                    temp.setContentTitle(content.getName());
                                                    temp.setPage(-1);
                                                    GalePressApplication.getInstance().getMenuSearchResult().add(temp);
                                                    contentList.add(temp);

                                                    MenuSearchResult temp2 = new MenuSearchResult();
                                                    temp2.setContentId(content.getId().toString());
                                                    temp2.setContentTitle(content.getName());
                                                    temp2.setPage(jsonObjectItem.getInt("page"));
                                                    temp2.setText(jsonObjectItem.getString("highlightedText"));
                                                    GalePressApplication.getInstance().getMenuSearchResult().add(temp2);
                                                }
                                            }
                                        } else {
                                            L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.parseInt(jsonObjectItem.getString("contentId")));
                                            if (content != null && content.isContentStatus()) {
                                                MenuSearchResult temp = new MenuSearchResult();
                                                temp.setContentId(content.getId().toString());
                                                temp.setContentTitle(content.getName());
                                                temp.setPage(-1);
                                                GalePressApplication.getInstance().getMenuSearchResult().add(temp);
                                                contentList.add(temp);

                                                MenuSearchResult temp2 = new MenuSearchResult();
                                                temp2.setContentId(content.getId().toString());
                                                temp2.setContentTitle(content.getName());
                                                temp2.setPage(jsonObjectItem.getInt("page"));
                                                temp2.setText(jsonObjectItem.getString("highlightedText"));
                                                GalePressApplication.getInstance().getMenuSearchResult().add(temp2);
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                int contentIndex = 0;
                                ArrayList<MenuSearchResult> tempList = GalePressApplication.getInstance().getMenuSearchResult();
                                List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(text);
                                if (contents.size() > 0) {
                                    for (int i = 0; i < contents.size(); i++) {
                                        boolean searchListContainContent = false;
                                        for(int k = 0; k < contentList.size(); k++) {
                                            if(((L_Content)contents.get(i)).getId().toString().contains(contentList.get(k).getContentId())) {
                                                searchListContainContent = true;
                                            }
                                        }
                                        if(!searchListContainContent){
                                            MenuSearchResult temp = new MenuSearchResult();
                                            temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                                            temp.setContentTitle(((L_Content) contents.get(i)).getName());
                                            temp.setPage(-1);
                                            tempList.add(contentIndex,temp);
                                            contentIndex++;
                                        }
                                    }
                                }
                                GalePressApplication.getInstance().setMenuSearchResult(tempList);


                                mainActivity.complateSearch(true, true);
                            } else {
                                GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                                List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(text);
                                if (contents.size() > 0) {
                                    for (int i = 0; i < contents.size(); i++) {
                                        MenuSearchResult temp = new MenuSearchResult();
                                        temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                                        temp.setContentTitle(((L_Content) contents.get(i)).getName());
                                        temp.setPage(-1);
                                        GalePressApplication.getInstance().getMenuSearchResult().add(temp);
                                    }
                                }
                                mainActivity.complateSearch(true, true);
                            }
                        } catch (Exception e) {
                            GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                            List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(text);
                            if (contents.size() > 0) {
                                for (int i = 0; i < contents.size(); i++) {
                                    MenuSearchResult temp = new MenuSearchResult();
                                    temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                                    temp.setContentTitle(((L_Content) contents.get(i)).getName());
                                    temp.setPage(-1);
                                    GalePressApplication.getInstance().getMenuSearchResult().add(temp);
                                }
                            }
                            mainActivity.complateSearch(true, true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                        List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(text);
                        if (contents.size() > 0) {
                            for (int i = 0; i < contents.size(); i++) {
                                MenuSearchResult temp = new MenuSearchResult();
                                temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                                temp.setContentTitle(((L_Content) contents.get(i)).getName());
                                temp.setPage(-1);
                                GalePressApplication.getInstance().getMenuSearchResult().add(temp);
                            }
                        }
                        mainActivity.complateSearch(true, true);
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        int socketTimeout = 30000;// 30 sec. Array gonderdigimiz ve server tarafinda array islemi yapildigi icin request timeout oluyordu bunu engelledim (MG)
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }


    public void fullTextSearchForReader(final String text, final String contentId, final MuPDFActivity muPDFActivity) {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId;
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationId = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationId = application.getApplicationId();
        }

        Uri.Builder uriBuilder = getFullTextSearchWebServiceUrlBuilder();
        uriBuilder.appendQueryParameter("query", text);
        uriBuilder.appendQueryParameter("applicationID", applicationId.toString());
        uriBuilder.appendQueryParameter("contentID", contentId);

        request = new JsonObjectRequest(Request.Method.POST, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null && response.getInt("status") == 1) {
                                muPDFActivity.setReaderSearchResult(new ArrayList<ReaderSearchResult>());
                                JSONArray result = response.getJSONArray("result");
                                L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.valueOf(contentId));

                                ReaderSearchResult pageItem;
                                for (int i = 0; i < result.length(); i++) {
                                    try {
                                        JSONObject jsonObjectItem = result.getJSONObject(i);
                                        pageItem = new ReaderSearchResult();

                                        if(content != null) {
                                            pageItem.setPage(jsonObjectItem.getInt("page"));
                                            pageItem.setText(jsonObjectItem.getString("highlightedText"));
                                            muPDFActivity.getReaderSearchResult().add(pageItem);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        muPDFActivity.setReaderSearchResult(new ArrayList<ReaderSearchResult>());
                                        muPDFActivity.complateSearch(true);
                                    }
                                }
                                muPDFActivity.complateSearch(true);
                            } else {
                                muPDFActivity.setReaderSearchResult(new ArrayList<ReaderSearchResult>());
                                muPDFActivity.complateSearch(true);
                            }
                        } catch (Exception e) {
                            muPDFActivity.setReaderSearchResult(new ArrayList<ReaderSearchResult>());
                            muPDFActivity.complateSearch(true);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        muPDFActivity.setReaderSearchResult(new ArrayList<ReaderSearchResult>());
                        muPDFActivity.complateSearch(true);
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        int socketTimeout = 30000;// 30 sec. Array gonderdigimiz ve server tarafinda array islemi yapildigi icin request timeout oluyordu bunu engelledim (MG)
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }
}

