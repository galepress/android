package ak.detaysoft.galepress;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
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

import org.xwalk.core.XWalkView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.web_views.BannerAndTabbarWebView;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebViewWithCrosswalk;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    public ContentHolderAdapter contentHolderAdapter;
    public HeaderGridView gridview;
    public LinearLayout banner;
    public BannerAndTabbarWebView bannerWebView;
    public BannerAndTabbarWebViewWithCrosswalk bannerWebViewWithCrosswalk;
    private LayoutInflater layoutInflater;
    private boolean isOnlyDownloaded;
    private List contents;
    public String searchQuery = new String("");
    ArrayList<L_Category> selectedCategories;
    L_Category selectedCategory = null;
    private View v;

    private static final int KITKAT = 19; // Android 4.4

    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        /*if(savedInstanceState != null){
            selectedCategories = (ArrayList<L_Category>)savedInstanceState.getSerializable("categoryList");
            searchQuery = savedInstanceState.getString("queryString");
        }*/

        try{
            isOnlyDownloaded = this.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG)==0;
        }catch (NullPointerException exception){
            isOnlyDownloaded = false;
        }
        super.onCreate(savedInstanceState);
        if(((MainActivity)this.getActivity()).content_id !=null){
            viewContent(GalePressApplication.getInstance().getDatabaseApi().getContent(((MainActivity)this.getActivity()).content_id));
            ((MainActivity)this.getActivity()).content_id = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // cihaz orientation degistiginde banner boyutu yeniden ayarlaniyor ve reload ediliyor. (MG)
        if (!getResources().getBoolean(R.bool.portrait_only) &&
                (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            banner.setLayoutParams(prepareBannerSize());
            if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
                bannerWebView.reload();
                bannerWebView.setVisibility(View.INVISIBLE);
            } else {
                bannerWebViewWithCrosswalk.reload(XWalkView.RELOAD_NORMAL);
                bannerWebViewWithCrosswalk.setVisibility(View.INVISIBLE);
            }
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

        if(savedInstanceState != null){
            selectedCategories = (ArrayList<L_Category>)savedInstanceState.getSerializable("categoryList");
            searchQuery = savedInstanceState.getString("queryString");
        }

        GalePressApplication.getInstance().setLibraryActivity(this);
        GalePressApplication.getInstance().setCurrentFragment(this);
        ((MainActivity)this.getActivity()).prepareActionBarForCustomTab(null, false, false);
        if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet())
            GalePressApplication.getInstance().getDataApi().updateApplication();

        v = inflater.inflate(R.layout.library_layout, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.gridview);
        gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
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

        banner =  (LinearLayout)LayoutInflater.from(this.getActivity()).inflate(R.layout.library_banner, null, false);

        if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
            bannerWebView = new BannerAndTabbarWebView(this.getActivity());
            bannerWebView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            bannerWebView.loadUrl(GalePressApplication.getInstance().getBannerLink());
            banner.addView(bannerWebView);
        } else {
            bannerWebViewWithCrosswalk = new BannerAndTabbarWebViewWithCrosswalk(this.getActivity());
            bannerWebViewWithCrosswalk.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            bannerWebViewWithCrosswalk.load(GalePressApplication.getInstance().getBannerLink(), null);
            banner.addView(bannerWebViewWithCrosswalk);
        }




        banner.setLayoutParams(prepareBannerSize());
        gridview.addHeaderView(banner);

        //Ilk secilen kategori genel oldugu icin ilk create icin listeye eklendi (MG)
        if(selectedCategories == null) {
            selectedCategories = new ArrayList<L_Category>();
            if(GalePressApplication.getInstance().getDatabaseApi().getAllCategories() != null && GalePressApplication.getInstance().getDatabaseApi().getAllCategories().size() > 0) {
                selectedCategory = (L_Category)GalePressApplication.getInstance().getDatabaseApi().getAllCategories().get(0);
                selectedCategories.add(selectedCategory);
            }
        }

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContent(isOnlyDownloaded, searchQuery, selectedCategories);
        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        return v;
    }

    public void updateBanner(){
        banner.setLayoutParams(prepareBannerSize());

        if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
            bannerWebView.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        } else {
            bannerWebViewWithCrosswalk.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        }
        gridview.invalidateViews();
    }

    public FrameLayout.LayoutParams prepareBannerSize(){
        Display display = ((MainActivity)getActivity()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //Banner height degeri her zaman portrait duruma gore ayarlaniyor
        int heightReference = 0;
        if(size.x < size.y){
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
        int bannerHeight = (int)(heightReference*(320f/740f));

        FrameLayout.LayoutParams bannerParams;
        if(getTag().compareTo(MainActivity.LIBRARY_TAB_TAG) == 0 && GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, bannerHeight);
            gridview.setPadding(gridview.getPaddingLeft(), (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())),gridview.getPaddingRight(), gridview.getPaddingBottom());
        } else {
            gridview.setPadding(gridview.getPaddingLeft(), (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())),gridview.getPaddingRight(), gridview.getPaddingBottom());
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, 0);
        }

        return bannerParams;
    }

    public void updateCategoryList(){
        if(selectedCategory.getCategoryID().compareTo(-1) == 0){

            if(selectedCategories.size() == GalePressApplication.getInstance().getDatabaseApi().getAllCategories().size()+1){
                selectedCategories.clear();
            } else {
                selectedCategories.clear();
                selectedCategories.addAll(GalePressApplication.getInstance().getDatabaseApi().getAllCategories());
                selectedCategories.add(selectedCategory);
            }


        } else {
            for(int i =  0; i < selectedCategories.size(); i++){
                L_Category item = selectedCategories.get(i);
                if(item.getCategoryID().compareTo(-1) == 0)
                    selectedCategories.remove(item);
            }

            boolean isCategorySelectedBefore = false;
            for(int i =  0; i < selectedCategories.size(); i++) {
                L_Category item = selectedCategories.get(i);
                if (item.getCategoryID().compareTo(selectedCategory.categoryID) == 0){
                    if(selectedCategories.size() > 1){
                        selectedCategories.remove(item);
                        isCategorySelectedBefore = true;
                    } else {
                        isCategorySelectedBefore = true;
                    }

                }
            }

            if(!isCategorySelectedBefore){
                selectedCategories.add(selectedCategory);
            }

        }
    }

    public void updateGridView(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContent(isOnlyDownloaded,searchQuery,selectedCategories);
                contentHolderAdapter.notifyDataSetChanged();
                gridview.invalidateViews();
            }
        });
    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged){

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContent(isOnlyDownloaded,searchQuery,selectedCategories);

        ContentHolderAdapter.ViewHolder holder = GalePressApplication.getInstance().getDataApi().getViewHolderForContent(content);
        if(holder != null){
            if(!content.isPdfDownloading()) {
                holder.progressBar.setVisibility(View.GONE);
                holder.progressBar.invalidate();
            }
            holder.content = content;
            if(isImagePathChanged)
                holder.refreshImageLoading();
        }
    }

    public ContentHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }

    public void viewContent(L_Content content){
        File samplePdfFile = new File(content.getPdfPath(),"file.pdf");
        if(content!=null && content.isPdfDownloaded() && samplePdfFile.exists()){

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, content.getId(), location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, null, dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_contentOpened, null,null,null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivityForResult(intent, 101);
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }

    public void viewContentDetail(L_Content content, float xPoint, float yPoint){
        if(content!=null){

            float animX = xPoint/gridview.getWidth();
            float animY = yPoint/gridview.getHeight();
            Intent intent = new Intent(getActivity(), ContentPopupActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("animationStartX",0.5f);
            intent.putExtra("animationStartY",0.5f);
            startActivity(intent);
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }

    public List getContents() {
        return contents;
    }
}
