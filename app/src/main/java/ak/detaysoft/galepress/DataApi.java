package ak.detaysoft.galepress;

/**
 * Created by adem on 11/02/14.
 */


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gcm.GCMRegistrar;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_ContentCategory;
import ak.detaysoft.galepress.database_models.TestApplicationInf;
import ak.detaysoft.galepress.service_models.R_AppCategories;
import ak.detaysoft.galepress.service_models.R_AppContents;
import ak.detaysoft.galepress.service_models.R_AppDetail;
import ak.detaysoft.galepress.service_models.R_AppVersion;
import ak.detaysoft.galepress.service_models.R_Category;
import ak.detaysoft.galepress.service_models.R_Content;
import ak.detaysoft.galepress.service_models.R_ContentDetail;
import ak.detaysoft.galepress.service_models.R_ContentFileUrl;


public class DataApi extends Object {
    //http://galepress.com/ws/v100/applications/20/detail

    private static final String webServisVersion = "v101";
    private static final String buildVersion = "v101";
    private static final String domainUrl = "http://www.galepress.com";
    private static final String webServiceUrl = domainUrl + "/rest/";
    public static final Integer MESSAGE_TYPE_COVER_IMAGE = 1;
    public static final Integer MESSAGE_TYPE_COVER_PDF_DOWNLOAD = 2;
    public boolean isBlockedFromWS = false;

    static final String GCM_SENDER_ID = "151896860923";  // Place here your Google project id

    private DatabaseApi databaseApi = null;
    public DownloadPdfTask downloadPdfTask;

    public String getBuildVersion(){
        return String.valueOf(BuildConfig.VERSION_CODE);
    }



    private Uri.Builder getWebServiceUrlBuilder (){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.galepress.com")
                .appendPath("ws")
                .appendPath(webServisVersion);
        return builder;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_TYPE_COVER_IMAGE) {
                getCoverImageVersionToUpdate(msg.arg2);
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
        try{
            boolean result;
            ConnectivityManager connectivityManager = (ConnectivityManager) GalePressApplication.getInstance().getSystemService(GalePressApplication.getInstance().CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            if(!result){
                Toast.makeText(GalePressApplication.getInstance(),GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_1) , Toast.LENGTH_LONG).show();
            }
            return result;
        }catch (Exception e){
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
        if(isConnectedToInternet() && !isBlockedFromWS){
            getRemoteApplicationVersion();
        }
    }

    public void getAppDetail() {
        if(isConnectedToInternet()){
            getBuildVersion();

            GalePressApplication application = GalePressApplication.getInstance();
            RequestQueue requestQueue = application.getRequestQueue();

            Integer applicationID;
            if (GalePressApplication.getInstance().isTestApplication()) {
                applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
            } else {
                applicationID = application.getApplicationId();
            }

            String osVersion = "";
            String release = Build.VERSION.RELEASE;
            int sdkVersion = Build.VERSION.SDK_INT;
            osVersion = sdkVersion + "_" + release;

            final String gcmRegisterId = GCMRegistrar.getRegistrationId(GalePressApplication.getInstance().getApplicationContext());

            JsonObjectRequest request;

            //http://www.galepress.com/ws/v100/applications/20/detail?deviceType=android&osVersion=19_4.4.4&deviceDetail=LG Nexus 5&deviceToken=a;lskdfjla;skjdf;laksjdf;laksdf;
            Uri.Builder uriBuilder = getWebServiceUrlBuilder();
            uriBuilder.appendPath("applications");
            uriBuilder.appendPath(applicationID.toString());
            uriBuilder.appendPath("detail");
            uriBuilder.appendQueryParameter("deviceType", "android");
            uriBuilder.appendQueryParameter("osVersion", osVersion);
            uriBuilder.appendQueryParameter("deviceDetail", getDeviceName());
            uriBuilder.appendQueryParameter("deviceToken", gcmRegisterId);
            uriBuilder.appendQueryParameter("buildVersion", getBuildVersion());

            request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                R_AppDetail appDetail= new R_AppDetail(response);
                                if(appDetail.getForce() == R_AppDetail.FORCE_WARN){
                                    // Warn user to update app.
                                    final String marketUrl = appDetail.getAndroidLink();
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                                    alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.forceUpdateWarnMessage));

                                    alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(marketUrl!=null && !marketUrl.isEmpty()){
                                                try{
                                                    String packageName = marketUrl.substring(marketUrl.indexOf("?id=")+4, marketUrl.length());
                                                    Uri marketUri = Uri.parse("market://details?id=" + packageName);
                                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                                    GalePressApplication.getInstance().getLibraryActivity().startActivity(marketIntent);
                                                }
                                                catch (Exception e){
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
                                }
                                else if(appDetail.getForce() == R_AppDetail.FORCE_BLOCK_APP || appDetail.getForce() == R_AppDetail.FORCE_BLOCK_AND_DELETE){
                                    // App is blocked. Lock all content features.
                                    isBlockedFromWS = true;
                                    final String marketUrl = appDetail.getAndroidLink();
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                                    alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
                                    alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.forceUpdateBlockMessage));

                                    alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.goToMarket), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(marketUrl!=null && !marketUrl.isEmpty()){
                                                try{
                                                    String packageName = marketUrl.substring(marketUrl.indexOf("?id=")+4, marketUrl.length());
                                                    Uri marketUri = Uri.parse("market://details?id=" + packageName);
                                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                                    GalePressApplication.getInstance().getLibraryActivity().startActivity(marketIntent);
                                                }
                                                catch (Exception e){
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
                                    if(appDetail.getForce() == R_AppDetail.FORCE_BLOCK_AND_DELETE){
                                        // Delete all content
                                        deleteEverything();
                                    }
                                }

                                else{
                                    Logout.e("Adem", "Do Nothing with : "+appDetail.getForce().toString());
                                }
                            } catch (Exception e) {
                                Logout.e("Adem", e.getMessage() + e.getLocalizedMessage() );
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.e("Error: ", error.getMessage());
                        }
                    }
            );
            request.setShouldCache(Boolean.FALSE);
            requestQueue.add(request);
        }
    }

    private void deleteEverything() {
        List categories = getDatabaseApi().getAllCategories();
        for(int i=0; i<categories.size(); i++){
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

    private void downloadCoverImage(String remoteUrl, L_Content content) {
        Intent i = new Intent(GalePressApplication.getInstance(), CoverImageDownloader.class);
        i.setData(Uri.parse(remoteUrl));
        i.putExtra(CoverImageDownloader.EXTRA_MESSENGER, new Messenger(handler));
        i.putExtra("id", Integer.toString(content.getId()));
        i.putExtra("coverImageName", content.getCoverImageFileName());
        GalePressApplication.getInstance().startService(i);
    }

    private void downloadFile(final String remoteUrl, L_Content content) {
        ArrayList<String> parameters = new ArrayList<String>();
        parameters.add(remoteUrl);
        parameters.add(content.getId().toString());
        parameters.add(content.getPdfFileName());
        downloadPdfTask = new DownloadPdfTask(null, content);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            downloadPdfTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,parameters);
        } else {
            downloadPdfTask.execute(parameters);
        }
    }

    private void getCoverImage(Integer id) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;


        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(id.toString());
        uriBuilder.appendPath("cover-image");
        uriBuilder.appendQueryParameter("size","1");

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentFileUrl contentCoverImage = new R_ContentFileUrl(response);
                            if (contentCoverImage.getError() != "") {
                                L_Content content = getDatabaseApi().getContent(contentCoverImage.getContentID());
                                downloadCoverImage(contentCoverImage.getUrl(), content);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    public void cancelDownload(Boolean confirmed) {
        if (confirmed) {
            downloadPdfTask.cancel(true);
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
            alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
            alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.CONFIRM_3));

            alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.EVET), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cancelDownload(true);
                }
            });
            alertDialog.setNegativeButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    GalePressApplication.getInstance().getLibraryActivity().updateGridView();
                }
            });
            alertDialog.show();
        }
    }

    public void getPdf(final L_Content content) {
        GalePressApplication.getInstance().getLibraryActivity().updateGridView();
        content.setPdfDownloading(true);
        getDatabaseApi().updateContent(content,false);
        if (downloadPdfTask != null && (downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
            alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
            alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.CONFIRM_2));

            alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.EVET), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cancelDownload(true);
                    ContentHolderAdapter.ViewHolder vh = getViewHolderForContent(content);
                    if (content.isPdfUpdateAvailable()) {
                        vh.updateButton.setEnabled(false);
                    } else {
                        vh.downloadButton.setEnabled(false);
                    }
                    vh.downloadButton.setEnabled(false);
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
            if(content.isProtected()){
                final AlertDialog.Builder alert = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                alert.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.SIFRE));
                alert.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_2));
                final EditText input = new EditText(GalePressApplication.getInstance().getLibraryActivity().getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        content.setPassword(input.getText().toString());
                        GalePressApplication.getInstance().getDatabaseApi().updateContent(content,false);
                        dialog.cancel();
                        downloadPdf(content);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
            else{
                downloadPdf(content);
            }
        }

    }

    public void deletePdf(final Integer id) {
        L_Content content = getDatabaseApi().getContent(id);
        if (content.isPdfDownloaded()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalePressApplication.getInstance().getLibraryActivity().getActivity());
            alertDialog.setTitle(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.UYARI));
            alertDialog.setMessage(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.CONFIRM_1));

            alertDialog.setPositiveButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.EVET), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    File directory = new File(GalePressApplication.getInstance().getFilesDir() + "/" + id);
                    deleteFolder(directory);
                    L_Content content = getDatabaseApi().getContent(id);
                    content.setPdfDownloaded(false);
                    content.setPdfUpdateAvailable(false);
                    getDatabaseApi().updateContent(content,true);

                    L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                    application.setVersion(application.getVersion()-1);
                    getDatabaseApi().updateApplication(application);
                    updateApplication();

                }
            });
            alertDialog.setNegativeButton(GalePressApplication.getInstance().getLibraryActivity().getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    GalePressApplication.getInstance().getLibraryActivity().updateGridView();
                }
            });
            alertDialog.show();
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
                            if (contentPdfFile.getError() != "") {
                                L_Content content = getDatabaseApi().getContent(contentPdfFile.getContentID());
                                if(content.isProtected() && content.getPassword()!=null)
                                    downloadFile(contentPdfFile.getUrl()+content.getPassword(), content);
                                else
                                    downloadFile(contentPdfFile.getUrl(), content);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);
    }

    private void getCoverImageVersionToUpdate(Integer id) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(id.toString());
        uriBuilder.appendPath("detail");

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString() , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentDetail remoteContent = new R_ContentDetail(response);
                            L_Content localContent = getDatabaseApi().getContent(remoteContent.getContentID());
                            localContent.setCoverImageVersion(remoteContent.getContentCoverImageVersion());
                            localContent.setVersion(remoteContent.getContentVersion());
                            getDatabaseApi().updateContent(localContent,true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);requestQueue.add(request);
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

    private void getRemoteContent(Integer contentID) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("contents");
        uriBuilder.appendPath(contentID.toString());
        uriBuilder.appendPath("detail");

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentDetail remoteContent = new R_ContentDetail(response);
                            L_Content localContent = getDatabaseApi().getContent(remoteContent.getContentID());
                            if (localContent == null) {
                                localContent = new L_Content(remoteContent);
                                getDatabaseApi().createContent(localContent);
                                createConCat(remoteContent, localContent);
                            } else {
                                localContent.updateWithRemoteContent(remoteContent);
                                getDatabaseApi().updateContent(localContent, true);
                                removeAllConCatsForContent(localContent);
                                createConCat(remoteContent, localContent);
                            }
                            if (localContent.getPdfVersion() < remoteContent.getContentPdfVersion()) {
                                // PDF Must be updated
                                if (localContent.isPdfDownloaded()) {
                                    localContent.setPdfUpdateAvailable(true);
                                }
                                localContent.setPdfVersion(remoteContent.getContentPdfVersion());
                                getDatabaseApi().updateContent(localContent,true);
                            }

                            if (localContent.getCoverImageVersion() < remoteContent.getContentCoverImageVersion()) {
                                // cover image must be updated.
                                // localContent.setCoverImageUpdateAvailable(true);
                                getCoverImage(localContent.getId());
                            } else {
                                // Content Detail update edildi.
                                localContent.setVersion(remoteContent.getContentVersion());
                                getDatabaseApi().updateContent(localContent,true);
                            }
                            GalePressApplication.getInstance().getLibraryActivity().getContentHolderAdapter().notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);
        requestQueue.add(request);

    }

    private void getRemoteAppCategories() {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = application.getApplicationId();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;
        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationId.toString());
        uriBuilder.appendPath("categories");


        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString() ,null,
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

                            getRemoteAppConents();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);requestQueue.add(request);
    }


    private void getRemoteAppConents() {
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
            TestApplicationInf testApplicationInf = GalePressApplication.getInstance().getTestApplicationLoginInf();
            uriBuilder.appendQueryParameter("username",testApplicationInf.getUsername());
            uriBuilder.appendQueryParameter("password",testApplicationInf.getPassword());
        }

        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString() , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int numberOfContentWillBeUpdated = 0;
                            R_AppContents RAppContents = new R_AppContents(response);
                            if (GalePressApplication.getInstance().isTestApplication()) {
                                if (RAppContents.getError() == "120") {
                                    // TODO: Show Error from string file. WARNING 120 and redirect to testLoginPage
                                    numberOfContentWillBeUpdated = -1;

                                } else if (RAppContents.getError() == "140") {
                                    // TODO: Show Error from string file. WARNING 140 and redirect to testLoginPage
                                    numberOfContentWillBeUpdated = -1;
                                }
                            } else {
                                for (R_Content content : RAppContents.getContents()) {
                                    L_Content localContent = getDatabaseApi().getContent(content.getContentID());
                                    if (GalePressApplication.getInstance().isTestApplication() || (content.getContentStatus() && !content.getContentBlocked())) {
                                        Integer remoteContentVersion = content.getContentVersion();
                                        if (localContent == null || (localContent.getVersion() < remoteContentVersion)) {
                                            // Content updating
                                            numberOfContentWillBeUpdated++;
                                            getRemoteContent(content.getContentID());
                                        }
                                    } else {
                                        if (localContent != null && !localContent.isPdfDownloaded()) {
                                            removeAllConCatsForContent(localContent);
                                            deleteContent(localContent);
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
                                if (deletedInServer && !l_content.isPdfDownloaded()) {
                                    deleteContent(l_content);
                                }
                            }

                            // Content'lerin hic biri update olmamissa. Uygulamanin local versiyonunu update ediyorum.
                            if (numberOfContentWillBeUpdated == 0) {
                                updateApplicationVersion();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);requestQueue.add(request);
    }


    public void getRemoteApplicationVersion() {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationID;
        if (GalePressApplication.getInstance().isTestApplication()) {
            applicationID = new Integer(application.getTestApplicationLoginInf().getApplicationId());
        } else {
            applicationID = application.getApplicationId();
        }

        RequestQueue requestQueue = application.getRequestQueue();

        JsonObjectRequest request;

        Uri.Builder uriBuilder = getWebServiceUrlBuilder();
        uriBuilder.appendPath("applications");
        uriBuilder.appendPath(applicationID.toString());
        uriBuilder.appendPath("version");


        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response: %s", response.toString());
                            R_AppVersion r_appVersion = new R_AppVersion(response);
                            L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                            if (r_appVersion.getApplicationVersion() != null && application.getVersion() != null) {
                                if (application.getVersion() < r_appVersion.getApplicationVersion()) {
                                    getRemoteAppCategories();
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
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);requestQueue.add(request);
    }

    public void updateApplicationVersion() {
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


        request = new JsonObjectRequest(Request.Method.GET, uriBuilder.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response: %s", response.toString());
                            R_AppVersion r_appVersion = new R_AppVersion(response);
                            L_Application application = getDatabaseApi().getApplication(GalePressApplication.getInstance().getApplicationId());
                            application.setVersion(r_appVersion.getApplicationVersion());
                            getDatabaseApi().updateApplication(application);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logout.e("Adem", "Error : " + error.getMessage());
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }
        );
        request.setShouldCache(Boolean.FALSE);requestQueue.add(request);
    }

    private void deleteContent(L_Content content) {
        File coverImage = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());
        if (coverImage.exists()) {
            coverImage.delete();
        }
        File contentFolder = new File(GalePressApplication.getInstance().getFilesDir(), content.getId().toString());
        if(contentFolder.exists()){
            deleteFolder(contentFolder);
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

    private void progressUpdate(L_Content content, long total, long fileLength) {
        ContentHolderAdapter.ViewHolder viewHolder = getViewHolderForContent(content);

        if(viewHolder!=null){
            if (viewHolder.content.getId().compareTo(content.getId()) == 0) {
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.progressLabel.setVisibility(View.VISIBLE);
                viewHolder.cancelButton.setVisibility(View.VISIBLE);
                viewHolder.cancelButton.setEnabled(true);
                viewHolder.downloadButton.setVisibility(View.INVISIBLE);
                viewHolder.progressBar.setProgress((int) (total * 100 / fileLength));
                String progressLabelText1 = String.format("%.2f", total / (1024.00 * 1024.00));
                String progressLabelText2 = String.format("%.2f", fileLength / (1024.00 * 1024.00));
                viewHolder.progressLabel.setText(progressLabelText1 + "MB / " + progressLabelText2 + "MB");
            }
        }
    }

    private ContentHolderAdapter.ViewHolder getViewHolderForContent(L_Content content) {

        GridView gridView = GalePressApplication.getInstance().getLibraryActivity().gridview;
        int childCount= gridView.getChildCount();
        for (int i = 0 ; i < childCount; i++) {
            View view = gridView.getChildAt(i);
            if (view != null) {
                ContentHolderAdapter.ViewHolder viewHolder = (ContentHolderAdapter.ViewHolder) view.getTag();
                if (viewHolder.content.getId().compareTo(content.getId()) == 0) {
                    return viewHolder;
                }
            }
        }
        return null;
    }

    public class DownloadPdfTask extends AsyncTask<ArrayList<String>, Integer, String> {
        File tempDirectory = null;
        File directory = null;
        L_Content content = null;
        long total;

        public DownloadPdfTask(Activity cosntext, L_Content c) {
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
Logout.e("Adem","Response Code : "+connection.getResponseCode() + " Response Message : "+connection.getResponseMessage());
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
                        if (lastCtm + 1000 < ctm) {
                            lastCtm = ctm;
                            publishProgress((int) total, (int) fileLength);
                        }
                    }
                    output.write(data, 0, count);
                }
                publishProgress((int) total, (int) fileLength);
                if (directory.exists()) {
                    deleteFolder(directory);
                }

                tempDirectory.renameTo(directory);
                Decompress decompressor = new Decompress(directory + "/" + pdfFileName, directory + "/");
                Logout.e("Adem","Content Directory : "+directory.getPath()+"");
                decompressor.unzip();
                if(checkDownloadSuccessfull(directory)){
                    new File(directory + "/" + pdfFileName).delete();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            content.setPdfUpdateAvailable(false);
                            getDatabaseApi().updateContent(content,true);
                        }
                    });
                }
                else{
                    final String errorMessage = getErrorMessageFromXMLFile(directory,pdfFileName);
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
                if (tempDirectory != null) {
                    deleteFolder(tempDirectory);
                }
            }

            return null;

        }

        private boolean checkDownloadSuccessfull(File directory) {
            int fileCount = directory.list().length;
            return fileCount > 1;
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressUpdate(content, progress[0], progress[1]);
        }

        @Override
        protected void onPreExecute() {
            GalePressApplication.getInstance().getLibraryActivity().updateGridView();
        }

        @Override
        protected void onCancelled() {
            if (tempDirectory != null) {
                deleteFolder(tempDirectory);
            }
            content.setPdfDownloading(false);
            getDatabaseApi().updateContent(content,true);
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(String a) {
            this.content.setPdfDownloaded(true);
            this.content.setPdfDownloading(false);
            getDatabaseApi().updateContent(this.content,true);
        }

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
                if(errorCodeString.equalsIgnoreCase("101")){
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_101);
                }
                else if(errorCodeString.equalsIgnoreCase("102")){
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_102);
                }
                else if(errorCodeString.equalsIgnoreCase("103")){
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_103);
                }
                else if(errorCodeString.equalsIgnoreCase("104")){
                    errorMessage = GalePressApplication.getInstance().getLibraryActivity().getString(R.string.WARNING_104);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

}

