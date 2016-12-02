package ak.detaysoft.galepress;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_CustomerApplication;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.search_models.MenuSearchResult;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    public ContentHolderAdapter contentHolderAdapter;
    public HeaderGridView gridview;
    private LayoutInflater layoutInflater;
    public boolean isDownloaded = false;
    public MenuSearchResult searchResult;
    public boolean isSearchOpened = false;
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
    private L_CustomerApplication application;

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        onViewStateRestored(savedInstanceState);

        GalePressApplication.getInstance().setLibraryFragment(this);
        GalePressApplication.getInstance().setCurrentFragment(this);

        if (searchResult != null) {
            contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                    , -1, isDownloaded);
        } else {
            if (GalePressApplication.getInstance().getSelectedCustomerApplication() != null) {
                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                        , GalePressApplication.getInstance().getApplicationFragment().selectedCategory.getId(), isDownloaded);
            }
        }

        if (contents != null && contents.size() > 0) {
            application = GalePressApplication.getInstance().getDatabaseApi().getCustomerApplication(Integer.valueOf(((L_Content) contents.get(0)).getApplicationId()));
        }

        v = inflater.inflate(R.layout.library_fragment, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.gridview);
        if (contents == null || contents.size() == 0)
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
                    if (!isDownloaded)
                        content = (L_Content) contents.get(position + 1);
                    else {
                        content = (L_Content) contents.get(position);
                    }
                    viewContentDetail(content, values[0] + v.getWidth(), values[1]);
                }
            }
        });

        contentHeader = LayoutInflater.from(this.getActivity()).inflate(R.layout.header_content, null, false);
        contentHeader.setLayoutParams(resizeHeaderContent());
        if (!isDownloaded)
            gridview.addHeaderView(contentHeader);

        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        if (searchResult != null) {
            GalePressApplication.getInstance().getDataApi().getApplicationContents(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                    , String.valueOf(-1));
        } else {
            if (GalePressApplication.getInstance().getSelectedCustomerApplication() != null) {
                GalePressApplication.getInstance().getDataApi().getApplicationContents(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                        , String.valueOf(GalePressApplication.getInstance().getApplicationFragment().selectedCategory.getId()));
            }
        }


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
                if (searchResult != null) {
                    contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                            , -1, isDownloaded);
                } else {
                    if (GalePressApplication.getInstance().getSelectedCustomerApplication() != null) {
                        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                                , GalePressApplication.getInstance().getApplicationFragment().selectedCategory.getId(), isDownloaded);
                    }
                }

                if (!isDownloaded)
                    initHeaderContent();
                contentHolderAdapter.notifyDataSetChanged();
                if (gridview != null) {
                    gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                    gridview.invalidateViews();
                }

                if (contents != null && contents.size() > 0)
                    v.findViewById(R.id.library_mask_view).setVisibility(View.GONE);

                if (searchResult != null && !isSearchOpened) {
                    openSearchResult();
                }
            }
        });
    }

    public void openSearchResult() {
        final L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.valueOf(searchResult.getContentId()));
        if (content != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] values = new int[2];
                    v.getLocationInWindow(values);
                    if (searchResult.getPage() != -1) {
                        if (content.isPdfDownloaded()) {
                            viewContent(content);
                        } else {
                            viewContentDetail(content, values[0], values[1]);
                        }
                    } else {
                        viewContentDetail(content, values[0], values[1]);
                    }
                }
            }, 750);
            isSearchOpened = true;
        } else {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.cannot_open_document), Toast.LENGTH_SHORT).show();
        }

    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged) {
        if (searchResult != null) {
            contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                    , -1, isDownloaded);
        } else {
            if (GalePressApplication.getInstance().getSelectedCustomerApplication() != null) {
                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsForApplicationIdAndcategoryId(GalePressApplication.getInstance().getSelectedCustomerApplication().getApplication().getId()
                        , GalePressApplication.getInstance().getApplicationFragment().selectedCategory.getId(), isDownloaded);
            }
        }

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
            if (searchResult != null) {
                intent.putExtra("searchPage", searchResult.getPage());
                intent.putExtra("searchQuery", GalePressApplication.getInstance().getSearchQuery());
            }
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
            if (searchResult != null) {
                intent.putExtra("searchPage", searchResult.getPage());
                intent.putExtra("searchQuery", GalePressApplication.getInstance().getSearchQuery());
            }
            getActivity().startActivityForResult(intent, 103);
        }
    }

    public List getContents() {

        if (!isDownloaded) {
            List<L_Content> subContents = new ArrayList<L_Content>();
            if(contents != null)
                subContents.addAll(contents);
            if (subContents.size() > 0)
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
            headerContentHolder.nameLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamMedium(getActivity()));

            headerContentHolder.monthLabel = ((TextView) contentHeader.findViewById(R.id.header_monthLabel));
            headerContentHolder.monthLabel.setText(headerContentHolder.content.getMonthlyName());
            headerContentHolder.monthLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            headerContentHolder.detailLabel = ((TextView) contentHeader.findViewById(R.id.header_detailLabel));
            headerContentHolder.detailLabel.setText(headerContentHolder.content.getDetail());
            headerContentHolder.detailLabel.setMovementMethod(new ScrollingMovementMethod());
            headerContentHolder.detailLabel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        headerContentHolder.detailLabel.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        headerContentHolder.detailLabel.getParent().requestDisallowInterceptTouchEvent(false);
                    }

                    return headerContentHolder.detailLabel.onTouchEvent(event);
                }
            });
            headerContentHolder.detailLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            headerContentHolder.playLinkButton = (ImageView) contentHeader.findViewById(R.id.header_content_play_link);
            headerContentHolder.playLinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (application != null && application.getPlayUrl() != null && application.getPlayUrl().length() > 0) {
                        String appPackageName = Uri.parse(application.getPlayUrl()).getQueryParameter("id");
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                }
            });
            if (application != null && application.getPlayUrl() != null && application.getPlayUrl().length() != 0) {
                ((RelativeLayout) headerContentHolder.playLinkButton.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((RelativeLayout) headerContentHolder.playLinkButton.getParent()).setVisibility(View.GONE);
            }

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
            headerContentHolder.coverImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (headerContentHolder.content != null) {

                        int[] values = new int[2];
                        v.getLocationInWindow(values);
                        viewContentDetail(headerContentHolder.content, values[0] + v.getWidth(), values[1]);

                    }
                }
            });

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
                    GalePressApplication.getInstance().getDataApi().saveImage(bitmap, content.getCoverImageFileName());
                else if (content.getRemoteCoverImageVersion() < content.getCoverImageVersion())
                    GalePressApplication.getInstance().getDataApi().downloadUpdatedImage(content.getSmallCoverImageDownloadPath()
                            , content.getCoverImageFileName());
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

        if (downloaded) {
            // Content is downloaded and ready to view.

            headerContentHolder.downloadStatus.setVisibility(View.GONE);
            headerContentHolder.downloadStatus.setEnabled(false);
            headerContentHolder.overlay.setVisibility(View.GONE);
            headerContentHolder.overlay.setEnabled(false);

            if (updateAvailable) {

                if (downloading) {
                    // update downloading
                    headerContentHolder.downloadStatus.setEnabled(true);
                    headerContentHolder.downloadStatus.setVisibility(View.VISIBLE);
                    headerContentHolder.overlay.setVisibility(View.VISIBLE);
                    headerContentHolder.overlay.setEnabled(true);
                }
            } else {

            }
        } else {
            // not downloaded
            if (downloading) {
                //Content is not downloaded but downloading
                headerContentHolder.downloadStatus.setVisibility(View.VISIBLE);
                headerContentHolder.downloadStatus.setEnabled(true);
                headerContentHolder.overlay.setVisibility(View.VISIBLE);
                headerContentHolder.overlay.setEnabled(true);
            } else {
                // Content Download edilmemis. ilk acildigi durum.
                headerContentHolder.downloadStatus.setVisibility(View.GONE);
                headerContentHolder.downloadStatus.setEnabled(false);
                headerContentHolder.overlay.setVisibility(View.GONE);
                headerContentHolder.overlay.setEnabled(false);
            }
        }

        if (application != null && (application.getPlayUrl() == null || application.getPlayUrl().length() == 0)) {
            headerContentHolder.playLinkButton.setVisibility(View.GONE);
        } else {
            headerContentHolder.playLinkButton.setVisibility(View.VISIBLE);
        }

        contentHeader.invalidate();
    }

    public class HeaderContentHolder {
        public ImageView coverImageView;
        public TextView nameLabel;
        public TextView monthLabel;
        public TextView detailLabel;
        public ImageView playLinkButton;
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
