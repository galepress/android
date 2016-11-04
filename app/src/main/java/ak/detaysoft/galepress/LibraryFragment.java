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
    public LinearLayout banner;
    public BannerAndTabbarWebView bannerWebView;
    public BannerAndTabbarWebViewWithCrosswalk bannerWebViewWithCrosswalk;
    private LayoutInflater layoutInflater;
    private List contents;
    L_Category selectedCategory = null;
    private View v;
    public int selectedCategoryPosition = 0;
    private int categoriesItemWidth = 0;


    public final static int BILLING_RESPONSE_RESULT_OK = 0;
    public final static int RESULT_USER_CANCELED = 1;
    public final static int RESULT_BILLING_UNAVAILABLE = 3;
    public final static int RESULT_ITEM_UNAVAILABLE = 4;
    public final static int RESULT_DEVELOPER_ERROR = 5;
    public final static int RESULT_ERROR = 6;
    public final static int RESULT_ITEM_ALREADY_OWNED = 7;
    public final static int RESULT_ITEM_NOT_OWNED = 8; //For consumable product


    private RecyclerView categoryView;
    private float categoryViewEnableYPosition = 0;
    private float categoryViewDisableYPosition = 0;
    private float categoryViewLastYPosition = 0;
    private CategoryAdapter categoryAdapter;
    private float lastScrollY = 0;
    private CustomCategoryRecyclerView mLayoutManager;
    public RelativeLayout contentHeader;
    public boolean isHeaderContentIsEnable = false;

    private HeaderContentHolder headerContentHolder;


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
            banner.setLayoutParams(resizeSliderBanner());
            contentHeader.setLayoutParams(resizeHeaderContent());
            resizeGridPadding();
            //4.4
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
        outState.putSerializable("selectedCategory", selectedCategory);
        outState.putInt("selectedCategoryPosition", selectedCategoryPosition);
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
            selectedCategory = (L_Category) savedInstanceState.getSerializable("selectedCategory");
            selectedCategoryPosition = savedInstanceState.getInt("selectedCategoryPosition");
        }

        GalePressApplication.getInstance().setLibraryActivity(this);
        GalePressApplication.getInstance().setCurrentFragment(this);
/*        if (GalePressApplication.getInstance().getDataApi().isConnectedToInternet())
            GalePressApplication.getInstance().getDataApi().updateApplication();*/

        v = inflater.inflate(R.layout.library_fragment, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.gridview);
        gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!GalePressApplication.getInstance().getDataApi().isBlockedFromWS) {

                    if (gridview.getHeaderViewCount() != 0)
                        position = position - gridview.getNumColumns() * 2;
                    int[] values = new int[2];
                    v.getLocationInWindow(values);
                    L_Content content;
                    if(isHeaderContentIsEnable)
                        content = (L_Content) contents.get(position+1);
                    else
                        content = (L_Content) contents.get(position);
                    if(isHeaderContentIsEnable){
                        viewContentDetail(content, values[0] + v.getWidth(), values[1]);
                    } else {
                        recreateFragment(true, content.getName());
                    }
                }
            }
        });

        gridview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    lastScrollY = gridview.computeVerticalScrollOffset();
                    categoryViewLastYPosition = categoryView.getY();
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (gridview != null && categoryView != null) {
                    if (lastScrollY > gridview.computeVerticalScrollOffset()) {
                        //gorunecek
                        float scrollDistance = lastScrollY - gridview.computeVerticalScrollOffset();
                        if (categoryView.getY() <= categoryViewDisableYPosition && categoryView.getY() >= categoryViewEnableYPosition && (categoryViewLastYPosition + scrollDistance * 4) <= categoryViewDisableYPosition) {
                            categoryView.setY(categoryViewLastYPosition + scrollDistance * 4);
                        } else {
                            categoryView.setY(categoryViewDisableYPosition);
                            lastScrollY = gridview.computeVerticalScrollOffset();
                            categoryViewLastYPosition = categoryView.getY();
                        }
                    } else if (lastScrollY < gridview.computeVerticalScrollOffset()) {
                        //saklanacak
                        float scrollDistance = gridview.computeVerticalScrollOffset() - lastScrollY;
                        if (categoryView.getY() <= categoryViewDisableYPosition && categoryView.getY() >= categoryViewEnableYPosition && (categoryViewLastYPosition - scrollDistance * 4) >= categoryViewEnableYPosition) {
                            categoryView.setY(categoryViewLastYPosition - scrollDistance * 4);
                        } else {
                            categoryView.setY(categoryViewEnableYPosition);
                            lastScrollY = gridview.computeVerticalScrollOffset();
                            categoryViewLastYPosition = categoryView.getY();
                        }
                    }
                }
            }
        });

        categoryView = (RecyclerView) v.findViewById(R.id.category_slider_recyclerview);
        categoryView.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        mLayoutManager = new CustomCategoryRecyclerView(categoryView, getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoryView.setLayoutManager(mLayoutManager);
        categoryView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    categoryAdapter.notifyDataSetChanged();
                }
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        ArrayList<L_Category> categories = new ArrayList<L_Category>();
        categories.addAll(((MainActivity) getActivity()).getCategoryListWithAll());
        categoryAdapter = new CategoryAdapter(categories);
        categoryView.setAdapter(categoryAdapter);

        banner = (LinearLayout) LayoutInflater.from(this.getActivity()).inflate(R.layout.slider_banner, null, false);
        //4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

        if(selectedCategory == null)
            selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().get(0);


        /*
        * TODO burada cekilen icerige gore kontrol yapilacak eger dergi icerikleri gosteriliyosa buraya content_header eklenecek
        * ve categoryView disable edilecek ve layoutparamslar ona gore duzenlenecek.
        * ya content_header olacak yada categoryView
        * slider simdilik yok kesin eklerler. kod dursun.
        * */
        banner.setLayoutParams(resizeSliderBanner());
        gridview.addHeaderView(banner);

        contentHeader = (RelativeLayout) LayoutInflater.from(this.getActivity()).inflate(R.layout.header_content, null, false);
        contentHeader.setLayoutParams(resizeHeaderContent());
        gridview.addHeaderView(contentHeader);
        resizeGridPadding();

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        return v;
    }

    public void recreateFragment(boolean isHeaderContentIsEnable, String title){
        this.isHeaderContentIsEnable = isHeaderContentIsEnable;
        ((MainActivity) getActivity()).getActionbarTitle().setText(title.toUpperCase());
        ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().detach(((MainActivity) getActivity()).getLibraryFragment()).attach(((MainActivity) getActivity()).getLibraryFragment()).commit();
    }

    public void updateBanner() {
        banner.setLayoutParams(resizeSliderBanner());
        contentHeader.setLayoutParams(resizeHeaderContent());
        resizeGridPadding();
        //4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bannerWebView.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        } else {
            bannerWebViewWithCrosswalk.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        }
        gridview.invalidateViews();
    }

    public void resizeGridPadding() {
        if (isHeaderContentIsEnable) {
            categoryView.setVisibility(View.GONE);
            gridview.setPadding(gridview.getPaddingLeft(),
                    (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
        } else {
            categoryView.setVisibility(View.VISIBLE);
            if (GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
                gridview.setPadding(gridview.getPaddingLeft(),
                        (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
            } else {
                gridview.setPadding(gridview.getPaddingLeft(),
                        (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
            }
        }
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

        if (isHeaderContentIsEnable) {
            headerContentParams = new FrameLayout.LayoutParams(headerContentWidth, headerContentHeight);
        } else {
            headerContentParams = new FrameLayout.LayoutParams(headerContentWidth, 0);
        }

        return headerContentParams;
    }

    public FrameLayout.LayoutParams resizeSliderBanner() {
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

        if (!isHeaderContentIsEnable && GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, bannerHeight);
        } else {
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, 0);
        }

        return bannerParams;
    }

    public void updateGridView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
                if (isHeaderContentIsEnable) {
                    initHeaderContent();
                }
                contentHolderAdapter.notifyDataSetChanged();
                if (gridview != null) {
                    gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                    gridview.invalidateViews();
                    if (categoryAdapter != null) {
                        final int scrollDistance = categoriesItemWidth * (selectedCategoryPosition + 1) - categoryView.computeHorizontalScrollOffset() - ApplicationThemeColor.getInstance().getScreenSizes(getActivity()).widthPixels / 2
                                - categoriesItemWidth / 2;
                        categoryView.scrollBy(scrollDistance, 0);
                        categoryAdapter.notifyDataSetChanged();
                    }
                    categoryViewEnableYPosition = categoryView.getY() - categoryView.getLayoutParams().height - ((RelativeLayout.LayoutParams) categoryView.getLayoutParams()).topMargin;
                    categoryViewDisableYPosition = ((RelativeLayout.LayoutParams) categoryView.getLayoutParams()).topMargin;
                }
            }
        });
    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged) {

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
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
        if(!isHeaderContentIsEnable)
            return contents;
        else {
            List<L_Content> subContents = new ArrayList<L_Content>();
            subContents.addAll(contents);
            subContents.remove(0);
            return subContents;
        }
    }


    /*
    * LeftMenuCategoryAdapter classinda kullanildi.
    * https://fabric.io/galepress/android/apps/ak.detaysoft.yeryuzudergidis/issues/56d3205ff5d3a7f76b2cef6d
    * Seklinde bi hata vardi. selectedCategories null olmasi ihtimaline karsi bende ilk createde oldugu gibi genel kategorisini set ettim
    * */
    public void repairSelectedCategory() {
        //Ilk secilen kategori genel oldugu icin ilk create icin listeye eklendi (MG)
        if (GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent() != null && GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().size() > 0) {
            selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().get(0);
            selectedCategoryPosition = 0;
            updateGridView();
        }
    }


    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
        private ArrayList<L_Category> categories;
        private CategoryAdapter.MyViewHolder selectedItem;

        public CategoryAdapter(ArrayList<L_Category> searchList) {
            this.categories = searchList;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public RelativeLayout border;
            public TextView text;
            public ImageView icon;
            public L_Category category;
            public int position;

            public MyViewHolder(View view) {
                super(view);
                border = (RelativeLayout) view.findViewById(R.id.library_category_border);
                icon = (ImageView) view.findViewById(R.id.library_category_icon);
                text = (TextView) view.findViewById(R.id.library_category_title);
                if (position == 0) {
                    view.setPadding(gridview.getPaddingLeft(), gridview.getPaddingLeft(), 0, gridview.getPaddingLeft());
                } else {
                    view.setPadding(0, gridview.getPaddingLeft(), gridview.getPaddingLeft(), gridview.getPaddingLeft());
                }
                if (categoriesItemWidth == 0)
                    categoriesItemWidth = view.getLayoutParams().width;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedCategory = categories.get(position);
                        selectedCategoryPosition = position;
                        ((MainActivity) getActivity()).choseCategory(position);
                        if(isHeaderContentIsEnable) {
                            recreateFragment(false, selectedCategory.getCategoryName());
                        } else {
                            updateGridView();
                        }
                    }
                });
            }
        }

        // Create new views (invoked by the layout manager)
        @Override
        public CategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.library_category_item, parent, false);

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.position = position;
            if (categories.get(position).getCategoryID().intValue() == selectedCategory.getCategoryID().intValue()) {
                selectedItem = holder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    holder.border.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.library_category_active));
                else
                    holder.border.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.library_category_active));

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    holder.border.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.library_category_passive));
                else
                    holder.border.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.library_category_passive));
            }

            holder.category = categories.get(position);
            holder.text.setText(categories.get(position).getCategoryName().toUpperCase());
            holder.text.setTextColor(Color.WHITE);
            holder.text.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(getActivity()));

            holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer(5))
                    .build();
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.library_category_icon, holder.icon, options);


        }

        public MyViewHolder getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getItemCount() {
            return categories.size();
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
