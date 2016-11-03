package ak.detaysoft.galepress;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

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


    private RecyclerView categoryView;
    private float categoryViewEnableYPosition = 0;
    private float categoryViewDisableYPosition = 0;
    private float categoryViewLastYPosition = 0;
    private CategoryAdapter categoryAdapter;
    private float lastScrollY = 0;
    private CustomCategoryRecyclerView mLayoutManager;


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
            banner.setLayoutParams(prepareBannerSize());
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
        if (GalePressApplication.getInstance().getDataApi().isConnectedToInternet())
            GalePressApplication.getInstance().getDataApi().updateApplication();

        v = inflater.inflate(R.layout.library_fragment, container, false);

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

        gridview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    lastScrollY = gridview.computeVerticalScrollOffset();
                    categoryViewLastYPosition = categoryView.getY();
                    Log.e("denemedeneme", "finsih :" + lastScrollY + " -- " + categoryViewLastYPosition + " --" + categoryViewEnableYPosition + " -- " + categoryViewDisableYPosition);
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

        banner.setLayoutParams(prepareBannerSize());
        gridview.addHeaderView(banner);

        selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getCategoriesOnlyHaveContent().get(0);
        selectedCategoryPosition = 0;

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
        this.contentHolderAdapter = new ContentHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        return v;
    }

    public void updateBanner() {
        banner.setLayoutParams(prepareBannerSize());

        //4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bannerWebView.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        } else {
            bannerWebViewWithCrosswalk.loadBannerUrl(GalePressApplication.getInstance().getBannerLink());
        }
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

        if (GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, bannerHeight);
            gridview.setPadding(gridview.getPaddingLeft(), (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
        } else {
            gridview.setPadding(gridview.getPaddingLeft(), (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())), gridview.getPaddingRight(), gridview.getPaddingBottom());
            bannerParams = new FrameLayout.LayoutParams(bannerWidth, 0);
        }

        return bannerParams;
    }

    public void updateGridView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
                contentHolderAdapter.notifyDataSetChanged();
                if (gridview != null) {
                    gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
                    gridview.invalidateViews();
                    if (categoryAdapter != null) {
                        final int scrollDistance = categoriesItemWidth*(selectedCategoryPosition+1)-categoryView.computeHorizontalScrollOffset()- ApplicationThemeColor.getInstance().getScreenSizes(getActivity()).widthPixels/2
                                - categoriesItemWidth/2;
                        categoryView.scrollBy(scrollDistance, 0);
                        categoryView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                categoryAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                    categoryViewEnableYPosition = categoryView.getY() - categoryView.getLayoutParams().height - ((RelativeLayout.LayoutParams) categoryView.getLayoutParams()).topMargin;
                    categoryViewDisableYPosition = ((RelativeLayout.LayoutParams) categoryView.getLayoutParams()).topMargin;
                }

            }
        });
    }

    public void updateAdapterList(L_Content content, boolean isImagePathChanged) {

        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(selectedCategory);
        /*for(int i = 0; i < 20; i++){
            contents.addAll(contents);
        }*/
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
        return contents;
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
                if(categoriesItemWidth == 0)
                    categoriesItemWidth = view.getLayoutParams().width;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedCategory = categories.get(position);
                        selectedCategoryPosition = position;
                        updateGridView();
                        ((MainActivity) getActivity()).choseCategory(position);
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
            if (categories.get(position).getCategoryID() == selectedCategory.getCategoryID()) {
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
}
