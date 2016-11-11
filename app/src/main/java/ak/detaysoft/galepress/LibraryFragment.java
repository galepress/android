package ak.detaysoft.galepress;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomCategoryRecyclerView;
import ak.detaysoft.galepress.util.CustomDownloadButton;
import ak.detaysoft.galepress.util.CustomPulseProgress;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebView;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebViewWithCrosswalk;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    public ContentHolderAdapter contentHolderAdapter;
    public HeaderGridView gridview;
    private LayoutInflater layoutInflater;
    public boolean isDownloaded = false;
    public List contents;
    private View v;

    public final static int BILLING_RESPONSE_RESULT_OK = 0;
    public final static int RESULT_USER_CANCELED = 1;
    public final static int RESULT_BILLING_UNAVAILABLE = 3;
    public final static int RESULT_ITEM_UNAVAILABLE = 4;
    public final static int RESULT_DEVELOPER_ERROR = 5;
    public final static int RESULT_ERROR = 6;
    public final static int RESULT_ITEM_ALREADY_OWNED = 7;
    public final static int RESULT_ITEM_NOT_OWNED = 8; //For consumable product

    private HeaderContentHolder headerContentHolder;
    private View contentHeader;

    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    public LibraryFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e("oncreate", "libraryfragment");
        if (((MainActivity) this.getActivity()).content_id != null) {
            viewContent(GalePressApplication.getInstance().getDatabaseApi().getContent(((MainActivity) this.getActivity()).content_id));
            ((MainActivity) this.getActivity()).content_id = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // cihaz orientation degistiginde banner boyutu yeniden ayarlaniyor ve reload ediliyor. (MG)
        if (!getResources().getBoolean(R.bool.portrait_only) &&
                (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            contentHeader.setLayoutParams(resizeHeaderContent());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        GalePressApplication.getInstance().setCurrentFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        onViewStateRestored(savedInstanceState);

        GalePressApplication.getInstance().setLibraryActivity(this);
        GalePressApplication.getInstance().setCurrentFragment(this);

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationId(GalePressApplication.getInstance().getSelectedCustomerApplication().getId(), isDownloaded);
        v = inflater.inflate(R.layout.library_fragment, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.gridview);
        if(contents == null || contents.size() == 0)
            v.findViewById(R.id.library_mask_view).setVisibility(View.VISIBLE);
        gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!GalePressApplication.getInstance().getDataApi().isBlockedFromWS) {

                    if (gridview.getHeaderViewCount() != 0)
                        position = position - gridview.getNumColumns();
                    int[] values = new int[2];
                    v.getLocationInWindow(values);
                    L_Content content;
                    if(!isDownloaded)
                        content = (L_Content) contents.get(position+1);
                    else {
                        content = (L_Content) contents.get(position);
                    }
                    viewContentDetail(content, values[0] + v.getWidth(), values[1]);
                }
            }
        });

        contentHeader = (RelativeLayout) LayoutInflater.from(this.getActivity()).inflate(R.layout.header_content, null, false);
        contentHeader.setLayoutParams(resizeHeaderContent());
        if(!isDownloaded)
            gridview.addHeaderView(contentHeader);

        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        GalePressApplication.getInstance().getDataApi().getApplicationContents(GalePressApplication.getInstance().getSelectedCustomerApplication().getId()
                , String.valueOf(GalePressApplication.getInstance().getApplicationFragment().selectedCategory.getId()));

        return v;
    }

    public FrameLayout.LayoutParams resizeHeaderContent() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int headerContentWidth = size.x
                - gridview.getPaddingLeft()
                - gridview.getPaddingRight();
        int headerContentHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.library_header_cell_height);

        FrameLayout.LayoutParams headerContentParams;

        headerContentParams = new FrameLayout.LayoutParams(headerContentWidth, headerContentHeight);

        return headerContentParams;
    }


    public void updateGridView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationId(GalePressApplication.getInstance().getSelectedCustomerApplication().getId(), isDownloaded);
                if(!isDownloaded)
                    initHeaderContent();
                contentHolderAdapter.notifyDataSetChanged();
                if (gridview != null) {
                    gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                    gridview.invalidateViews();
                }

                if(contents != null && contents.size() > 0)
                    v.findViewById(R.id.library_mask_view).setVisibility(View.GONE);
            }
        });
    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged) {

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationId(GalePressApplication.getInstance().getSelectedCustomerApplication().getId(), isDownloaded);
        ContentHolderAdapter.ViewHolder holder = GalePressApplication.getInstance().getDataApi().getViewHolderForContent(content);
        if (holder != null) {
            if (!content.isPdfDownloading()) {
                holder.downloadStatus.setVisibility(View.GONE);
                holder.overlay.setVisibility(View.GONE);
            }
            holder.content = content;
            if (isImagePathChanged)
                holder.refreshImageLoading();
        }
    }

    public ContentHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }

    public void viewContent(L_Content content) {
        File samplePdfFile = new File(content.getPdfPath(), "file.pdf");
        if (content != null && content.isPdfDownloaded() && samplePdfFile.exists()) {

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentOpened, null, null, null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            getActivity().startActivityForResult(intent, 101);
        }
    }


    public void viewContentDetail(L_Content content, float xPoint, float yPoint) {
        if (content != null) {

            float animX = xPoint / gridview.getWidth();
            float animY = yPoint / gridview.getHeight();
            Intent intent = new Intent(getActivity(), ContentDetailPopupActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("animationStartX", 0.5f);
            intent.putExtra("animationStartY", 0.5f);
            getActivity().startActivityForResult(intent, 103);
        }
    }

    public List getContents() {

        if(!isDownloaded){
            List<L_Content> subContents = new ArrayList<L_Content>();
            subContents.addAll(contents);
            if(subContents.size() > 0)
                subContents.remove(0);
            return subContents;
        } else {
            return contents;
        }


    }

    public void initHeaderContent() {
        if (contents != null && contents.size() > 0) {

            headerContentHolder = new HeaderContentHolder();
            headerContentHolder.content = (L_Content) contents.get(0);

            headerContentHolder.nameLabel = ((TextView) contentHeader.findViewById(R.id.header_nameLabel));
            headerContentHolder.nameLabel.setText(headerContentHolder.content.getName());
            headerContentHolder.nameLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            headerContentHolder.monthLabel = ((TextView) contentHeader.findViewById(R.id.header_monthLabel));
            headerContentHolder.monthLabel.setText(headerContentHolder.content.getMonthlyName());
            headerContentHolder.monthLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            headerContentHolder.detailLabel = ((TextView) contentHeader.findViewById(R.id.header_detailLabel));
            headerContentHolder.detailLabel.setText(headerContentHolder.content.getDetail());
            headerContentHolder.detailLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            headerContentHolder.updateButton = (Button) contentHeader.findViewById(R.id.header_content_update);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                headerContentHolder.updateButton.setBackground(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_UPDATE));
            else
                headerContentHolder.updateButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_UPDATE));
            headerContentHolder.updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DataApi.isConnectedToInternet()) {
                        v.setEnabled(false);
                        v.setVisibility(View.GONE);
                        GalePressApplication.getInstance().getDataApi().getPdf(headerContentHolder.content, getActivity());
                    }
                }
            });

            headerContentHolder.deleteButton = (Button) contentHeader.findViewById(R.id.header_content_delete);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                headerContentHolder.deleteButton.setBackground(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_DELETE));
            else
                headerContentHolder.deleteButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_DELETE));
            headerContentHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    GalePressApplication.getInstance().getDataApi().deletePdf(headerContentHolder.content.getId(), getActivity());
                }
            });

            headerContentHolder.readButton = (Button) contentHeader.findViewById(R.id.header_content_view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                headerContentHolder.readButton.setBackground(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_READ));
            else
                headerContentHolder.readButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getHeaderContentDrawable(getActivity(), ApplicationThemeColor.HEADER_CONTENT_READ));
            headerContentHolder.readButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (headerContentHolder.content != null && headerContentHolder.content.isPdfDownloaded())
                        viewContent(headerContentHolder.content);
                }
            });

            headerContentHolder.downloadButton = (CustomDownloadButton) contentHeader.findViewById(R.id.header_content_download);
            headerContentHolder.downloadButton.isHeaderContentDownload = true;
            initDownloadButton(headerContentHolder.content);
            headerContentHolder.downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DataApi.isConnectedToInternet()) {
                        headerContentHolder.downloadButton.setEnabled(false);
                        headerContentHolder.downloadButton.setClickable(false);
                        if (headerContentHolder.content.isBuyable()) {
                            if (headerContentHolder.content.isContentBought() || GalePressApplication.getInstance().isUserHaveActiveSubscription()) {
                                if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                        || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)) {
                                    headerContentHolder.downloadButton.startAnim();
                                }
                                GalePressApplication.getInstance().getDataApi().getPdf(headerContentHolder.content, getActivity());
                            } else {
/*
                            * Login olmayan kullanici urun alamaz
                            * */
                                if (GalePressApplication.getInstance().getUserInformation() != null
                                        && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                                        && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0) {
                                    if (!GalePressApplication.getInstance().isBlnBind() && GalePressApplication.getInstance().getmService() == null) {
                                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                                                .show();
                                        return;
                                    }

                                    try {
                                        headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
                                        Bundle buyIntentBundle = GalePressApplication.getInstance().getmService().getBuyIntent(3, getActivity().getPackageName(),
                                                headerContentHolder.content.getIdentifier(), "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                                        if (buyIntentBundle.getInt("RESPONSE_CODE") == BILLING_RESPONSE_RESULT_OK) { // Urun satin alinmamis
                                            // Start purchase flow (this brings up the Google Play UI).
                                            // Result will be delivered through onActivityResult().
                                            getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
                                                    1002, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                                    Integer.valueOf(0));
                                        } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ITEM_ALREADY_OWNED) { // Urun daha once alinmis
                                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                                                    .show();
                                            if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                                    || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)) {
                                                headerContentHolder.downloadButton.startAnim();
                                            }
                                            GalePressApplication.getInstance().getDataApi().getPdf(headerContentHolder.content, getActivity());
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_USER_CANCELED) { // Hata var
                                            Toast.makeText(getContext(), getActivity().getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                                                    .show();
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_BILLING_UNAVAILABLE) { // Hata var
                                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                                                    .show();
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ITEM_UNAVAILABLE) { // Hata var
                                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                                                    .show();
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ERROR) { // Hata var
                                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                                                    .show();
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        } else { //  Beklenmedik Hata var
                                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                                                    .show();
                                            headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                        }

                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } catch (IntentSender.SendIntentException e) {
                                        e.printStackTrace();
                                        headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        headerContentHolder.downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    }
                                } else {
                                    //Giris yapin uyarisi
                                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.login_warning_inapp_billing), Toast.LENGTH_SHORT)
                                            .show();
                                    /*
                                    * Mainactivity onActivityResult da logine yonlendirme yapacagiz
                                    * */
                                    Intent intent = new Intent(getActivity(), UserLoginActivity.class);
                                    intent.putExtra("action", UserLoginActivity.ACTION_MENU);
                                    intent.putExtra("isLaunchOpen", false);
                                    startActivityForResult(intent, 102);
                                }
                            }
                        } else {
                            if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                    || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)) {
                                headerContentHolder.downloadButton.startAnim();
                            }
                            GalePressApplication.getInstance().getDataApi().getPdf(headerContentHolder.content, getActivity());
                        }
                    }
                }
            });


            headerContentHolder.loading = (CustomPulseProgress) contentHeader.findViewById(R.id.header_content_image_loading);
            headerContentHolder.loading.startAnim();


            headerContentHolder.downloadPercentage = (TextView) contentHeader.findViewById(R.id.header_content_download_percentage);
            headerContentHolder.downloadPercentage.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));
            headerContentHolder.downloadPercentage.setTextColor(ApplicationThemeColor.getInstance().getGridItemDetailLabelColor());


            headerContentHolder.downloadStatus = (RelativeLayout) contentHeader.findViewById(R.id.header_content_download_status);
            headerContentHolder.downloadStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    headerContentHolder.downloadStatus.setEnabled(true);
                    headerContentHolder.downloadStatus.setClickable(true);
                    GalePressApplication.getInstance().getDataApi().cancelDownload(false, getActivity(), headerContentHolder.content);
                }
            });

            headerContentHolder.overlay = (ImageView) contentHeader.findViewById(R.id.header_content_download_overlay);

            headerContentHolder.coverImageView = (ImageView) contentHeader.findViewById(R.id.header_coverImage);

            File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), headerContentHolder.content.getCoverImageFileName());
            if (coverImageFile.exists()) {
                displayImage(false, headerContentHolder.coverImageView, headerContentHolder.loading, "file://" + coverImageFile.getPath(), headerContentHolder.content);
            } else if (headerContentHolder.content.getSmallCoverImageDownloadPath() != null) {
                displayImage(true, headerContentHolder.coverImageView, headerContentHolder.loading, headerContentHolder.content.getSmallCoverImageDownloadPath(), headerContentHolder.content);
            } else {
                Log.e("imageDisplayed", "noimage");
            }

            updateHeaderContent();
        }
    }

    private void initDownloadButton(final L_Content content) {


        if (content.isBuyable()) {
            if (content.isContentBought() || GalePressApplication.getInstance().isUserHaveActiveSubscription()) {
                headerContentHolder.downloadButton.init(CustomDownloadButton.RESTORE, "");
            } else {
                AsyncTask<Void, Void, String> getPrice = new AsyncTask<Void, Void, String>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        headerContentHolder.downloadButton.init(CustomDownloadButton.PURCHASE, "");
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        String price = "";
                        if (GalePressApplication.getInstance().isUserHaveActiveSubscription() || GalePressApplication.getInstance().getmService() == null) {
                            return price;
                        } else {
                            //Satin alinabilen urunse fiyati kontrol ediliyor
                            ArrayList<String> skuList = new ArrayList<String>();
                            skuList.add(content.getIdentifier());
                            Bundle querySkus = new Bundle();
                            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                            Bundle skuDetails;
                            try {
                                skuDetails = GalePressApplication.getInstance().getmService().getSkuDetails(3, getActivity().getPackageName(), "inapp", querySkus);

                                int response = skuDetails.getInt("RESPONSE_CODE");

                                if (response == 0) {
                                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                                    if (responseList.size() != 0) {
                                        for (String thisResponse : responseList) {
                                            JSONObject object = null;
                                            try {
                                                object = new JSONObject(thisResponse);
                                                price = object.getString("price");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                price = "";
                            }
                            if (price == null || price.length() == 0) {
                                price = (content.getMarketPrice() == null || content.getMarketPrice().length() == 0) ? "" : content.getMarketPrice();
                            }
                        }
                        return price;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        if (s.compareTo("") != 0)
                            headerContentHolder.downloadButton.getPriceTextView().setText(s);
                        else {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.product_price_error), Toast.LENGTH_SHORT).show();
                            headerContentHolder.downloadButton.getPriceTextView().setText(content.getPrice());
                        }
                        //downloadButton.getPriceTextView().setText("12.99 TL");
                        headerContentHolder.downloadButton.invalidate();
                    }
                };
                getPrice.execute();
            }
        } else {
            headerContentHolder.downloadButton.init(CustomDownloadButton.FREE, "");
        }

    }

    public void displayImage(final boolean isDownload, final ImageView image, final CustomPulseProgress loading, String imagePath, final L_Content content) {
        DisplayImageOptions displayConfig;
        if (isDownload) {
            displayConfig = new DisplayImageOptions.Builder()
                    .showImageOnFail(ApplicationThemeColor.getInstance().paintIcons(getActivity(), ApplicationThemeColor.INTERNET_CONNECTION_ERROR))
                    .cacheInMemory(true).build();
        } else {
            displayConfig = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).build();
        }

        ImageLoader.getInstance().displayImage(imagePath, image, displayConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                image.setImageBitmap(null);
                loading.setVisibility(View.VISIBLE);
                loading.startAnim();
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                loading.setVisibility(View.GONE);
                if (isDownload)
                    GalePressApplication.getInstance().getDataApi().saveImage(bitmap, content.getCoverImageFileName(), content.getId(), false);
                else if (content.getRemoteCoverImageVersion() < content.getCoverImageVersion())
                    GalePressApplication.getInstance().getDataApi().downloadUpdatedImage(content.getSmallCoverImageDownloadPath()
                            , content.getCoverImageFileName()
                            , content.getId(), false);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void updateHeaderContent() {

        if (headerContentHolder == null)
            return;

        boolean downloaded = headerContentHolder.content.isPdfDownloaded();
        boolean updateAvailable = headerContentHolder.content.isPdfUpdateAvailable();
        boolean downloading = headerContentHolder.content.isPdfDownloading()
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask != null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content != null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content.getId().compareTo(headerContentHolder.content.getId()) == 0;

        //Cancel butonu aktif oldugu her durumda download butonunun animasyonunu durdurmak icin
        if (headerContentHolder.downloadStatus.getVisibility() == View.VISIBLE) {
            headerContentHolder.downloadButton.stopAnim();
        }

        if (downloaded) {
            // Content is downloaded and ready to view.
            headerContentHolder.downloadButton.setVisibility(View.GONE);

            headerContentHolder.readButton.setVisibility(View.VISIBLE);
            headerContentHolder.readButton.setEnabled(true);

            headerContentHolder.deleteButton.setVisibility(View.VISIBLE);
            headerContentHolder.deleteButton.setEnabled(true);

            headerContentHolder.downloadStatus.setVisibility(View.GONE);
            headerContentHolder.downloadStatus.setEnabled(false);
            headerContentHolder.overlay.setVisibility(View.GONE);
            headerContentHolder.overlay.setEnabled(false);
            headerContentHolder.downloadButton.stopAnim();

            if (updateAvailable) {
                headerContentHolder.updateButton.setVisibility(View.VISIBLE);
                headerContentHolder.updateButton.setEnabled(true);

                if (downloading) {
                    // update downloading
                    headerContentHolder.updateButton.setVisibility(View.GONE);
                    headerContentHolder.readButton.setVisibility(View.GONE);
                    headerContentHolder.deleteButton.setVisibility(View.GONE);
                    headerContentHolder.downloadStatus.setEnabled(true);
                    headerContentHolder.downloadStatus.setVisibility(View.VISIBLE);
                    headerContentHolder.overlay.setVisibility(View.VISIBLE);
                    headerContentHolder.overlay.setEnabled(true);
                    headerContentHolder.downloadButton.stopAnim();
                    headerContentHolder.updateButton.setVisibility(View.GONE);
                    headerContentHolder.deleteButton.setVisibility(View.GONE);
                }
            } else {
                // update not available
                headerContentHolder.updateButton.setVisibility(View.GONE);
            }
        } else {
            // not downloaded
            if (downloading) {
                //Content is not downloaded but downloading
                headerContentHolder.downloadStatus.setVisibility(View.VISIBLE);
                headerContentHolder.downloadStatus.setEnabled(true);
                headerContentHolder.overlay.setVisibility(View.VISIBLE);
                headerContentHolder.overlay.setEnabled(true);
                headerContentHolder.downloadButton.setEnabled(false);
                headerContentHolder.downloadButton.setVisibility(View.GONE);
                headerContentHolder.downloadButton.stopAnim();
                headerContentHolder.updateButton.setVisibility(View.GONE);
                headerContentHolder.deleteButton.setVisibility(View.GONE);
                headerContentHolder.readButton.setVisibility(View.GONE);
            } else {
                // Content Download edilmemis. ilk acildigi durum.
                headerContentHolder.downloadButton.setVisibility(View.VISIBLE);
                headerContentHolder.downloadButton.setEnabled(true);
                headerContentHolder.downloadButton.setClickable(true);
                headerContentHolder.deleteButton.setVisibility(View.GONE);
                headerContentHolder.updateButton.setVisibility(View.GONE);
                headerContentHolder.readButton.setVisibility(View.GONE);
                headerContentHolder.downloadStatus.setVisibility(View.GONE);
                headerContentHolder.downloadStatus.setEnabled(false);
                headerContentHolder.overlay.setVisibility(View.GONE);
                headerContentHolder.overlay.setEnabled(false);
            }
        }

        if (headerContentHolder.readButton.getVisibility() == View.VISIBLE) {
            headerContentHolder.downloadStatus.setVisibility(View.GONE);
            headerContentHolder.downloadStatus.setEnabled(false);
            headerContentHolder.overlay.setVisibility(View.GONE);
            headerContentHolder.overlay.setEnabled(false);
            headerContentHolder.downloadButton.stopAnim();
        }
        contentHeader.invalidate();
    }

    public class HeaderContentHolder {
        public ImageView coverImageView;
        public TextView nameLabel;
        public TextView monthLabel;
        public TextView detailLabel;
        public Button readButton;
        public Button updateButton;
        public Button deleteButton;
        public CustomDownloadButton downloadButton;
        public ImageView overlay;
        public RelativeLayout downloadStatus;
        public TextView downloadPercentage;
        public CustomPulseProgress loading;
        public L_Content content;

        public void refreshImageLoading() {
            displayImage(true, coverImageView, loading, content.getSmallCoverImageDownloadPath(), content);
        }

    }

    public HeaderContentHolder getHeaderContentHolder() {
        return headerContentHolder;
    }

    public void setHeaderContentHolder(HeaderContentHolder headerContentHolder) {
        this.headerContentHolder = headerContentHolder;
    }
}
