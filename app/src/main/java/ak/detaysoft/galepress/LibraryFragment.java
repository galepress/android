package ak.detaysoft.galepress;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.artifex.mupdfdemo.MuPDFActivity;

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
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebView;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    public ContentHolderAdapter contentHolderAdapter;
    public HeaderGridView gridview;
    public LinearLayout banner;
    public BannerAndTabbarWebView bannerWebView;
    private LayoutInflater layoutInflater;
    public boolean isOnlyDownloaded;
    private List contents;
    public String searchQuery = new String("");
    ArrayList<L_Category> selectedCategories;
    L_Category selectedCategory = null;
    private View v;
    final int KITKAT = 19; // Android 5.0

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        /*if(savedInstanceState != null){
            selectedCategories = (ArrayList<L_Category>)savedInstanceState.getSerializable("categoryList");
            searchQuery = savedInstanceState.getString("queryString");
        }*/

        try {
            isOnlyDownloaded = this.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0;
        } catch (NullPointerException exception) {
            isOnlyDownloaded = false;
        }
        super.onCreate(savedInstanceState);
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
            banner.setLayoutParams(prepareBannerSize());
            bannerWebView.reload();
            bannerWebView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("categoryList", selectedCategories);
        outState.putString("queryString", searchQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            selectedCategories = (ArrayList<L_Category>) savedInstanceState.getSerializable("categoryList");
            searchQuery = savedInstanceState.getString("queryString");
        }

        GalePressApplication.getInstance().setLibraryActivity(this);
        GalePressApplication.getInstance().setCurrentFragment(this);
        ((MainActivity) this.getActivity()).prepareActionBarForCustomTab(null, false, false);
        if (GalePressApplication.getInstance().getDataApi().isConnectedToInternet())
            GalePressApplication.getInstance().getDataApi().updateApplication();

        v = inflater.inflate(R.layout.library_layout, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.gridview);
        gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getLibraryGridViewColor());
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!GalePressApplication.getInstance().getDataApi().isBlockedFromWS) {
                    if (gridview.getHeaderViewCount() != 0)
                        position = position - gridview.getNumColumns();
                    int[] values = new int[2];
                    v.getLocationInWindow(values);
                    L_Content content = (L_Content) contents.get(position);
                    viewContentDetail(content, values[0] + v.getWidth(), values[1]);
                }
            }
        });

        banner = (LinearLayout) LayoutInflater.from(this.getActivity()).inflate(R.layout.library_banner, null, false);
        bannerWebView = new BannerAndTabbarWebView(this.getActivity());
        bannerWebView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        bannerWebView.loadUrl(GalePressApplication.getInstance().getBannerLink());
        banner.addView(bannerWebView);

        banner.setLayoutParams(prepareBannerSize());
        gridview.addHeaderView(banner);

        //Ilk secilen kategori genel oldugu icin ilk create sirasinda listeye eklendi (MG)
        if (selectedCategories == null) {
            selectedCategories = new ArrayList<L_Category>();

            /*
            * Uygulama ilk acildiginda indirilenler sekmesi acilmadan kategori secimi yapilirsa indirilenler sekmesinin selectedCategories duzenlendi.
            * */
            if (isOnlyDownloaded && ((MainActivity) getActivity()).getLibraryFragment() != null && ((MainActivity) getActivity()).getLibraryFragment().getSelectedCategories() != null) {
                selectedCategories.addAll(((MainActivity) getActivity()).getLibraryFragment().getSelectedCategories());
            } else {
                if (GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent() != null && GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().size() > 0) {
                    selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().get(0);
                    selectedCategories.add(selectedCategory);
                }
            }
        }

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(isOnlyDownloaded, searchQuery, selectedCategories);
        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        return v;
    }

    public void updateBanner() {
        banner.setLayoutParams(prepareBannerSize());
        bannerWebView.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        gridview.invalidateViews();
    }

    public FrameLayout.LayoutParams prepareBannerSize() {
        Display display = ((MainActivity) getActivity()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //Banner height degeri her zaman portrait duruma gore ayarlaniyor
        int heightReference = 0;
        if (size.x < size.y) {
            heightReference = size.x
                    - gridview.getPaddingLeft()
                    - gridview.getPaddingRight();
        } else {
            heightReference = size.y
                    - gridview.getPaddingLeft()
                    - gridview.getPaddingRight();
        }

        int bannerWidth = size.x
                - gridview.getPaddingLeft()
                - gridview.getPaddingRight();
        int bannerHeight = (int) (heightReference * (320f / 740f));

        FrameLayout.LayoutParams bannerParams;

        if (getTag().compareTo(MainActivity.LIBRARY_TAB_TAG) == 0 && GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, bannerHeight);
            gridview.setPadding(gridview.getPaddingLeft(), (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
        } else {
            gridview.setPadding(gridview.getPaddingLeft(), (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, 0);
        }

        return bannerParams;
    }

    public void updateSelectedCategoriesList() {
        if (selectedCategory.getCategoryID().compareTo(-1) == 0) {
            List categoriesOnlyHaveContent = GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent();
            if (categoriesOnlyHaveContent == null) {

                /*
                * Burasi sqlite dan cekilen listenin bos olmasi durumunda hem secili kagetorileri hemde sol medude kategori listesini refresh etmek icin (MG)
                * */
                repairSelectedCategories();
                ((MainActivity) getActivity()).getCategoriesAdapter().notifyDataSetChanged();
            } else {
                if (selectedCategories.size() == categoriesOnlyHaveContent.size() + 1) {
                    selectedCategories.clear();
                } else {
                    selectedCategories.clear();
                    selectedCategories.addAll(categoriesOnlyHaveContent);
                    selectedCategories.add(selectedCategory);
                }
            }

        } else {
            for (int i = 0; i < selectedCategories.size(); i++) {
                L_Category item = selectedCategories.get(i);
                if (item.getCategoryID().compareTo(-1) == 0)
                    selectedCategories.remove(item);
            }

            boolean isCategorySelectedBefore = false;
            for (int i = 0; i < selectedCategories.size(); i++) {
                L_Category item = selectedCategories.get(i);
                if (item.getCategoryID().compareTo(selectedCategory.categoryID) == 0) {
                    if (selectedCategories.size() > 1) {
                        selectedCategories.remove(item);
                        isCategorySelectedBefore = true;
                    } else {
                        isCategorySelectedBefore = true;
                    }

                }
            }

            if (!isCategorySelectedBefore) {
                selectedCategories.add(selectedCategory);
            }
        }
    }

    public void updateGridView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(isOnlyDownloaded, searchQuery, selectedCategories);
                /*for(int i = 0; i < 20; i++){
                    contents.addAll(contents);
                }*/
                contentHolderAdapter.notifyDataSetChanged();
                if (gridview != null) {
                    gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                    gridview.invalidateViews();
                }

            }
        });
    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged) {

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(isOnlyDownloaded, searchQuery, selectedCategories);
        /*for(int i = 0; i < 20; i++){
            contents.addAll(contents);
        }*/
        ContentHolderAdapter.ViewHolder holder = GalePressApplication.getInstance().getDataApi().getViewHolderForContent(content);
        if (holder != null) {
            if (!content.isPdfDownloading()) {
                holder.progressBar.setVisibility(View.GONE);
                holder.overlay.setVisibility(View.GONE);
                holder.progressBar.invalidate();
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
            GalePressApplication.getInstance().getDataApi().updateApplication();
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
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }

    public List getContents() {
        return contents;
    }


    /*
    * LeftMenuCategoryAdapter classinda kullanildi.
    * https://fabric.io/galepress/android/apps/ak.detaysoft.yeryuzudergidis/issues/56d3205ff5d3a7f76b2cef6d
    * Seklinde bi hata vardi. selectedCategories null olmasi ihtimaline karsi bende ilk createde oldugu gibi genel kategorisini set ettim
    * */
    public void repairSelectedCategories() {
        //Ilk secilen kategori genel oldugu icin ilk create icin listeye eklendi (MG)
        selectedCategories = new ArrayList<L_Category>();
        if (GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent() != null && GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().size() > 0) {
            selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().get(0);
            selectedCategories.add(selectedCategory);
            updateGridView();
        }
    }

    public ArrayList<L_Category> getSelectedCategories() {
        return selectedCategories;
    }
}
