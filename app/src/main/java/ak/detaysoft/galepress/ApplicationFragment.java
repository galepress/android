package ak.detaysoft.galepress;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.xwalk.core.XWalkView;

import java.util.ArrayList;
import java.util.List;

import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_CustomerApplication;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomCategoryRecyclerView;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebView;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebViewWithCrosswalk;

/**
 * Created by p1025 on 08.11.2016.
 */

public class ApplicationFragment extends Fragment {

    private View v;
    public ApplicationHolderAdapter contentHolderAdapter;
    public HeaderGridView gridview;
    private LayoutInflater layoutInflater;
    private List applications;
    public LinearLayout banner;
    public BannerAndTabbarWebView bannerWebView;
    public BannerAndTabbarWebViewWithCrosswalk bannerWebViewWithCrosswalk;
    L_Category selectedCategory = null;
    public int selectedCategoryPosition = 0;
    private int categoriesItemWidth = 0;


    private RecyclerView categoryView;
    private float categoryViewEnableYPosition = 0;
    private float categoryViewDisableYPosition = 0;
    private float categoryViewLastYPosition = 0;
    private CategoryAdapter categoryAdapter;
    private float lastScrollY = 0;
    private CustomCategoryRecyclerView mLayoutManager;

    public ApplicationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            selectedCategory = (L_Category) savedInstanceState.getSerializable("selectedCategory");
            selectedCategoryPosition = savedInstanceState.getInt("selectedCategoryPosition");
        }

        GalePressApplication.getInstance().setApplicationFragment(this);
        GalePressApplication.getInstance().setCurrentFragment(this);

        v = inflater.inflate(R.layout.application_fragment, container, false);

        gridview = (HeaderGridView) v.findViewById(R.id.application_gridview);
        gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!GalePressApplication.getInstance().getDataApi().isBlockedFromWS) {

                    if (gridview.getHeaderViewCount() != 0)
                        position = position - gridview.getNumColumns();
                    int[] values = new int[2];
                    v.getLocationInWindow(values);
                    L_CustomerApplication application = (L_CustomerApplication) applications.get(position);
                    ((MainActivity)getActivity()).getActionbarTitle().setText(application.getAppName().toUpperCase());
                    ((MainActivity)getActivity()).openLibraryFragment();
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


        if (GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
            /*
            * TODO burada cekilen icerige gore kontrol yapilacak eger dergi icerikleri gosteriliyosa buraya content_header eklenecek
            * ve categoryView disable edilecek ve layoutparamslar ona gore duzenlenecek.
            * ya content_header olacak yada categoryView
            * slider simdilik yok kesin eklerler. kod dursun.
            * */
            banner.setLayoutParams(resizeSliderBanner());
            gridview.addHeaderView(banner);
        }

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

        if (selectedCategory == null)
            selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getAllCategories().get(0);

        applications = GalePressApplication.getInstance().getDatabaseApi().getAllCustomerApplicationsByCategory(selectedCategory.getId().intValue());
        this.contentHolderAdapter = new ApplicationHolderAdapter(this);
        gridview.setAdapter(this.contentHolderAdapter);
        updateGridView();

        return v;
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

        if (GalePressApplication.getInstance().getBannerLink().length() > 0 && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
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

                applications = GalePressApplication.getInstance().getDatabaseApi().getAllCustomerApplicationsByCategory(selectedCategory.getId().intValue());
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

    /*
    * LeftMenuCategoryAdapter classinda kullanildi.
    * https://fabric.io/galepress/android/apps/ak.detaysoft.yeryuzudergidis/issues/56d3205ff5d3a7f76b2cef6d
    * Seklinde bi hata vardi. selectedCategories null olmasi ihtimaline karsi bende ilk createde oldugu gibi genel kategorisini set ettim
    * */
    public void repairSelectedCategory() {
        //Ilk secilen kategori genel oldugu icin ilk create icin listeye eklendi (MG)
        if (GalePressApplication.getInstance().getDatabaseApi().getAllCategories() != null && GalePressApplication.getInstance().getDatabaseApi().getAllCategories().size() > 0) {
            selectedCategory = (L_Category) GalePressApplication.getInstance().getDatabaseApi().getAllCategories().get(0);
            selectedCategoryPosition = 0;
            updateGridView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // cihaz orientation degistiginde banner boyutu yeniden ayarlaniyor ve reload ediliyor. (MG)
        if (!getResources().getBoolean(R.bool.portrait_only) &&
                (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            banner.setLayoutParams(resizeSliderBanner());
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
    public void onResume() {
        super.onResume();
        GalePressApplication.getInstance().setCurrentFragment(this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("selectedCategory", selectedCategory);
        outState.putInt("selectedCategoryPosition", selectedCategoryPosition);
        super.onSaveInstanceState(outState);
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
                        updateGridView();
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
            if (categories.get(position).getId().intValue() == selectedCategory.getId().intValue()) {
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
            holder.text.setText(categories.get(position).getName().toUpperCase());
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


    public ApplicationHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }

    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    public List getApplications() {
        return applications;
    }
}
