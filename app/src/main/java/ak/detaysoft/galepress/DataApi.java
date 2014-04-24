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
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_ContentCategory;
import ak.detaysoft.galepress.database_models.TestApplicationInf;
import ak.detaysoft.galepress.service_models.R_AppCategories;
import ak.detaysoft.galepress.service_models.R_AppContents;
import ak.detaysoft.galepress.service_models.R_AppVersion;
import ak.detaysoft.galepress.service_models.R_Category;
import ak.detaysoft.galepress.service_models.R_Content;
import ak.detaysoft.galepress.service_models.R_ContentDetail;
import ak.detaysoft.galepress.service_models.R_ContentFileUrl;


public class DataApi extends Object {
    private static final String domainUrl = "http://www.galepress.com";
    private static final String webServiceUrl = domainUrl + "/rest/";
    public static final Integer MESSAGE_TYPE_COVER_IMAGE = 1;
    public static final Integer MESSAGE_TYPE_COVER_PDF_DOWNLOAD = 2;
    private DatabaseApi databaseApi = null;
    public DownloadPdfTask downloadPdfTask;

    private static final String TAG = "Adem - " + DataApi.class.getSimpleName() + " ";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_TYPE_COVER_IMAGE) {
                Toast.makeText(GalePressApplication.getInstance(), "Cover image " + Integer.toString(msg.arg1) + " for content =" + Integer.toString(msg.arg2), Toast.LENGTH_LONG).show();
                getCoverImageVersionToUpdate(msg.arg2);
            } else if (msg.what == MESSAGE_TYPE_COVER_PDF_DOWNLOAD) {
                Toast.makeText(GalePressApplication.getInstance(), "PDF Downloaded Result" + Integer.toString(msg.arg1) + " for content =" + Integer.toString(msg.arg2), Toast.LENGTH_LONG).show();
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

    public void updateApplication() {
        if(isConnectedToInternet()){
            getRemoteApplicationVersion();
        }
    }

    private void downloadCoverImage(String remoteUrl, L_Content content) {
        Intent i = new Intent(GalePressApplication.getInstance(), CoverImageDownloader.class);
        i.setData(Uri.parse(remoteUrl));
        i.putExtra(CoverImageDownloader.EXTRA_MESSENGER, new Messenger(handler));
        i.putExtra("id", Integer.toString(content.getId()));
        i.putExtra("coverImageName", content.getCoverImageFileName());
        GalePressApplication.getInstance().startService(i);
    }

    private void downloadFile(String remoteUrl, L_Content content) {
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
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("contentID", id);
            parameters.put("size", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getContentCoverImage", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
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
            downloadPdf(content);
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
                    getDatabaseApi().updateContent(content,true);
                    GalePressApplication.getInstance().getLibraryActivity().updateGridView();

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
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("contentID", content.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getContentFile", parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            R_ContentFileUrl contentPdfFile = new R_ContentFileUrl(response);
                            if (contentPdfFile.getError() != "") {
                                L_Content content = getDatabaseApi().getContent(contentPdfFile.getContentID());
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
    }

    private void getCoverImageVersionToUpdate(Integer id) {
        GalePressApplication application = GalePressApplication.getInstance();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("contentID", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getContentDetail", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
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
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("contentID", contentID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getContentDetailWithCategories", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);

    }

    private void getRemoteAppCategories() {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = application.getApplicationId();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("applicationID", applicationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getAppCategories", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
    }


    private void getRemoteAppConents() {
        GalePressApplication application = GalePressApplication.getInstance();
        Integer applicationId = application.getApplicationId();
        RequestQueue requestQueue = application.getRequestQueue();
        JsonObjectRequest request;
        JSONObject parameters = new JSONObject();
        try {
            if (GalePressApplication.getInstance().isTestApplication()) {
                TestApplicationInf testApplicationInf = GalePressApplication.getInstance().getTestApplicationLoginInf();
                parameters.put("username", testApplicationInf.getUsername());
                parameters.put("password", testApplicationInf.getPassword());
                applicationId = new Integer(testApplicationInf.getApplicationId());
            } else {
                parameters.put("applicationID", applicationId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getAppContents", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
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
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("applicationID", applicationID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getAppVersion", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
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
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("applicationID", applicationID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        request = new JsonObjectRequest(Request.Method.POST, webServiceUrl + "getAppVersion", parameters,
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        requestQueue.add(request);
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
                if (viewHolder.progressBar.getVisibility() == View.INVISIBLE) {
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.progressLabel.setVisibility(View.VISIBLE);
                    viewHolder.cancelButton.setVisibility(View.VISIBLE);
                    viewHolder.cancelButton.setEnabled(true);
                }
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

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                long fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(outputFile.getPath());

                byte data[] = new byte[1024];
                long total = 0;
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
                decompressor.unzip();
                new File(directory + "/" + pdfFileName).delete();
            } catch (Exception e) {
                Logout.e("Error", e.getLocalizedMessage());
                if (tempDirectory != null) {
                    deleteFolder(tempDirectory);
                }
            }

            return null;

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

}

