package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gcm.GCMRegistrar;
import com.mogoweb.chrome.WebView;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ak.detaysoft.galepress.custom_models.ApplicationIds;
import ak.detaysoft.galepress.custom_models.ApplicationPlist;
import ak.detaysoft.galepress.custom_models.Subscription;
import ak.detaysoft.galepress.custom_models.TabbarItem;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by adem on 31/03/14.
 */
public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener{

    public static final int GENEL_CATEGORY_ID = 0;
    public static final int SHOW_ALL_CATEGORY_ID = -1;
    public static final int CONTEXT_MENU_GROUP_ID = 1;
    public static final String LIBRARY_TAB_TAG = "LIBRARY_TAB";
    public static final String DOWNLOADED_LIBRARY_TAG = "DOWNLOADED_TAB";
    public static final String HOME_TAB_TAG = "HOME_TAB";
    public static final String INFO_TAB_TAG = "INFO_TAB";
    public FragmentTabHost mTabHost;
    private EditText searchView;
    private ImageView menuButton;
    AsyncTask<Void, Void, Void> mRegisterTask;
    public Integer content_id = null;


    private MenuDrawer mDrawer;

    //Bağlantılar sekmesi
    private LeftMenuSocialAdapter linksAdapter;
    private ImageView linksListViewCloseIcon;
    private RelativeLayout linksTitleLayout;
    private ListView linksListView;


    //Kategori sekmesi
    private LeftMenuCategoryAdapter categoriesAdapter;
    private ListView categoriesListView;
    private List<L_Category> categoryListWithAll;
    private RelativeLayout categoriesTitleLayout;
    private ImageView categoriesListViewCloseIcon;


    //Üyelik sekmesi
    private LeftMenuMembershipAdapter membershipAdapter;
    private ListView membershipListView;
    private RelativeLayout membershipTitleLayout;
    private ImageView membershipListViewCloseIcon;

    private ImageView clearSearch;

    private LinearLayout leftLayout;
    private Button logoutButton;

    private ImageButton geriButton;
    private ImageButton ileriButton;
    private ImageButton refreshButton;
    public boolean isTabFirstInit = true;
    ArrayList<TabHost.TabSpec> specList = new ArrayList<TabHost.TabSpec>();
    private Subscription selectedSubscription;

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setCustomTabs();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra("content_id")){
            this.content_id = Integer.valueOf(intent.getStringExtra("content_id"));
        }
        else{
            this.content_id = null;
        }

        try{
            registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e){
            Log.e("ConnectivityManager", e.toString());
        }

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        mDrawer.setContentView(R.layout.activity_main);
        mDrawer.setMenuView(R.layout.left_menu);
        mDrawer.setDropShadowColor(ApplicationThemeColor.getInstance().getMenuShadowColor());
        mDrawer.setDropShadowSize(2);
        mDrawer.setMenuSize((int) getResources().getDimension(R.dimen.left_menu_size));
        mDrawer.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                hideKeyboard(searchView);
            }
        });

        leftLayout = (LinearLayout)findViewById(R.id.left_menu_layout);
        categoriesTitleLayout = (RelativeLayout)findViewById(R.id.left_categories_layout);
        categoriesTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(searchView);
                if (categoriesListView.getVisibility() == View.VISIBLE) {
                    categoriesListView.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        categoriesListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
                    else
                        categoriesListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));

                } else {
                    categoriesListView.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        categoriesListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                    else
                        categoriesListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                }

            }
        });

        categoryListWithAll = GalePressApplication.getInstance().getDatabaseApi().getAllCategories();
        categoryListWithAll.add(0, new L_Category(-1, getString(R.string.show_all)));
        categoriesListView = (ListView)findViewById(R.id.left_menu_category_list);

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                hideKeyboard(searchView);

                if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()) {
                    mTabHost.getTabWidget().setCurrentTab(1);
                }
                else
                    mTabHost.getTabWidget().setCurrentTab(0);
                mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);
                L_Category selectedCategory = (L_Category)categoryListWithAll.get(position);
                if(selectedCategory.getCategoryName().compareTo(getString(R.string.show_all))!=0){
                    for(int i =0; i<categoryListWithAll.size(); i++){
                        L_Category category = (L_Category)categoryListWithAll.get(i);
                        if(category.getCategoryName().compareTo(selectedCategory.getCategoryName())==0){
                            selectedCategory = category;
                        }
                    }
                }
                else{
                    selectedCategory = new L_Category(-1, getString(R.string.show_all));
                }
                LibraryFragment libraryFragment = getCurrentLibraryFragment();
                libraryFragment.selectedCategory = selectedCategory;
                libraryFragment.updateCategoryList();
                libraryFragment.updateGridView();
                categoriesAdapter.notifyDataSetChanged();
            }
        });
        categoriesListViewCloseIcon = (ImageView)findViewById(R.id.left_menu_categories_close);

        categoriesAdapter = new LeftMenuCategoryAdapter(this, categoryListWithAll);
        categoriesListView.setAdapter(categoriesAdapter);

        linksListView = (ListView)findViewById(R.id.left_menu_social_list);
        linksTitleLayout = (RelativeLayout)findViewById(R.id.left_social_layout);
        linksTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(searchView);
                if (linksListView.getVisibility() == View.VISIBLE) {
                    linksListView.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        linksListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
                    else
                        linksListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));

                } else {
                    linksListView.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        linksListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                    else
                        linksListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                }

            }
        });

        linksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mDrawer.closeMenu(true);
                ApplicationPlist item = GalePressApplication.getInstance().getApplicationPlist().get(position);

                if (item.getKey().toString().toLowerCase().contains("mail")) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    //intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{item.getValue().toString()});
                    intent.putExtra(Intent.EXTRA_SUBJECT, " ");
                    intent.putExtra(Intent.EXTRA_TEXT, " ");
                    try {
                        startActivity(Intent.createChooser(intent, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, ExtraWebViewActivity.class);
                    intent.putExtra("url", item.getValue().toString());
                    intent.putExtra("isMainActivitIntent", true);
                    startActivity(intent);
                    overridePendingTransition(R.animator.left_to_right_translate, 0);
                }

            }
        });

        linksListViewCloseIcon = (ImageView)findViewById(R.id.left_menu_social_close);
        linksAdapter = new LeftMenuSocialAdapter(this, GalePressApplication.getInstance().getApplicationPlist());
        linksListView.setAdapter(linksAdapter);



        membershipTitleLayout = (RelativeLayout)findViewById(R.id.left_membership_layout);
        membershipTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(searchView);

                if (membershipListView.getVisibility() == View.VISIBLE) {
                    membershipListView.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        membershipListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
                    else
                        membershipListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));

                } else {
                    membershipListView.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        membershipListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                    else
                        membershipListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
                }
            }
        });
        membershipListView = (ListView)findViewById(R.id.left_menu_membership_list);
        membershipListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectMembershipListItem(position);
            }
        });
        membershipListViewCloseIcon = (ImageView)findViewById(R.id.left_menu_membership_close);

        membershipAdapter = new LeftMenuMembershipAdapter(this);
        membershipListView.setAdapter(membershipAdapter);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        setTabs();

        searchView = (EditText)findViewById(R.id.left_menu_search_edit_text);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    clearSearch.setVisibility(View.GONE);
                    ((ImageView)findViewById(R.id.left_menu_search_icon)).setVisibility(View.VISIBLE);
                }
                else {
                    clearSearch.setVisibility(View.VISIBLE);
                    ((ImageView)findViewById(R.id.left_menu_search_icon)).setVisibility(View.GONE);
                }
                changeSearchViewColor(true);
                LibraryFragment libraryFragment = getCurrentLibraryFragment();
                libraryFragment.searchQuery = s.toString();
                libraryFragment.updateGridView();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });
        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeSearchViewColor(true);
                return false;
            }
        });


        clearSearch = (ImageView)findViewById(R.id.left_menu_search_clear);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
                LibraryFragment libraryFragment = getCurrentLibraryFragment();
                libraryFragment.searchQuery = "";
                libraryFragment.updateGridView();
            }
        });

        menuButton = (ImageView)findViewById(R.id.menu_button);
        ((LinearLayout)findViewById(R.id.menu_button_layout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mDrawer.isMenuVisible())
                    mDrawer.closeMenu(true);
                else {
                    mDrawer.openMenu(true);
                }
            }
        });


        logoutButton = (Button)findViewById(R.id.left_menu_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FacebookSdk.isInitialized())
                    LoginManager.getInstance().logOut();
                if(GalePressApplication.getInstance().getDataApi().downloadPdfTask != null){
                    if(GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)
                        GalePressApplication.getInstance().getDataApi().downloadPdfTask.cancel(true);
                    GalePressApplication.getInstance().getDataApi().downloadPdfTask = null;
                }
                GalePressApplication.getInstance().setTestApplicationLoginInf("","","0","","",false);
                Intent i = new Intent(MainActivity.this, ViewerLoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
        if(GalePressApplication.getInstance().isTestApplication()){
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            logoutButton.setVisibility(View.GONE);
        }

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            Resources resources = Resources.getSystem();
            InputStream is = resources.openRawResource(R.raw.application);
            xr.parse(new InputSource(is));

        } catch (Exception e) {
            e.printStackTrace();
        }
        GCMRegistrar.checkManifest(this);
        registerReceiver(mHandleMessageReceiver, new IntentFilter("ak.detaysoft.galepress.DISPLAY_MESSAGE"));

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(this);

        // Check if regid already presents
        if (regId.equals("")) {
            // Register with GCM
            Logout.e("Adem","Is Registered " + (GCMRegistrar.isRegisteredOnServer(GalePressApplication.getInstance().getApplicationContext()) ? "YES" : "NO"));
            GCMRegistrar.register(GalePressApplication.getInstance().getApplicationContext(), DataApi.GCM_SENDER_ID);
        } else {
            if (!GCMRegistrar.isRegisteredOnServer(GalePressApplication.getInstance().getApplicationContext())) {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.

                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        GCMRegistrar.register(GalePressApplication.getInstance().getApplicationContext(), DataApi.GCM_SENDER_ID);
                        // Register on our server
                        // On server creates a new user
//                        aController.register(context, name, email, regId);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };

                // execute AsyncTask
                mRegisterTask.execute(null, null, null);
            }
        }

        invalidateActivityViewAndAdapter(true);

        GalePressApplication.getInstance().setCurrentActivity(this);
        GalePressApplication.getInstance().setMainActivity(this);
        GalePressApplication.getInstance().getDataApi().getAppDetail(this);

    }

    /*
    * Renk degismedigi zaman servisten gelen tabbar ikonları invalidate edilmiyor.
    * Yoksa uygulama icinde her update oldugunda ikonlarda yeniden load edildigi cini kotu gorunuyor
    * */
    public void invalidateActivityViewAndAdapter(boolean isColorChanged){

        leftLayout.setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());

        //Kategori sekmesi
        categoriesTitleLayout.setBackgroundColor(Color.TRANSPARENT);
        ((TextView)(findViewById(R.id.left_menu_category_text))).setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        ((TextView)(findViewById(R.id.left_menu_category_text))).setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            categoriesListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
        else
            categoriesListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView)findViewById(R.id.left_menu_category_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_CATEGORY));
        else
            ((ImageView)findViewById(R.id.left_menu_category_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_CATEGORY));

        categoryListWithAll = GalePressApplication.getInstance().getDatabaseApi().getAllCategories();
        categoryListWithAll.add(0, new L_Category(-1, getString(R.string.show_all)));

        categoriesAdapter.setmCategory(categoryListWithAll);
        categoriesAdapter.notifyDataSetChanged();
        categoriesListView.invalidate();


        //Bağlantılar sekmesi
        linksAdapter.notifyDataSetChanged();
        linksListView.invalidate();

        if(!GalePressApplication.getInstance().isTestApplication()){
            ((RelativeLayout)findViewById(R.id.left_social_layout)).setBackgroundColor(Color.TRANSPARENT);
            ((RelativeLayout)findViewById(R.id.left_membership_layout)).setBackgroundColor(Color.TRANSPARENT);
        } else {
            ((RelativeLayout)findViewById(R.id.left_social_layout)).setVisibility(View.GONE);
            ((RelativeLayout)findViewById(R.id.left_membership_layout)).setVisibility(View.GONE);
        }

        ((TextView)(findViewById(R.id.left_menu_social_text))).setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        ((TextView)(findViewById(R.id.left_menu_social_text))).setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView)findViewById(R.id.left_menu_link_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_LINK));
        else
            ((ImageView)findViewById(R.id.left_menu_link_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_LINK));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            linksListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
        else
            linksListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));



        //Üyelik sekmesi
        membershipAdapter.notifyDataSetChanged();
        membershipListView.invalidate();
        ((TextView)(findViewById(R.id.left_menu_membership_text))).setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        ((TextView)(findViewById(R.id.left_menu_membership_text))).setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView)findViewById(R.id.left_menu_membership_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_LINK));
        else
            ((ImageView)findViewById(R.id.left_menu_membership_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_LINK));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            membershipListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));
        else
            membershipListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_UP));

        //Kategori ve baglantilar listviewlerin height hesaplamasi. Scroll engelleyebilmek icin
        LayoutInflater mInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View listItemView = mInflater.inflate(R.layout.left_menu_category_item, null);
        listItemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int listHeight = 0;
        for(int i = 0 ; i < categoryListWithAll.size(); i++){
            listHeight += listItemView.getMeasuredHeight();
        }
        categoriesListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));

        if(!GalePressApplication.getInstance().isTestApplication()){
            listHeight = 0;
            for(int i = 0 ; i < GalePressApplication.getInstance().getApplicationPlist().size(); i++){
                listHeight += listItemView.getMeasuredHeight();
            }
            linksListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));
        } else {
            linksListView.setVisibility(View.GONE);
        }


        View membershipListItemView = mInflater.inflate(R.layout.left_menu_membership_item, null);
        membershipListItemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        if(!GalePressApplication.getInstance().isTestApplication()){
            listHeight = 0;
            for(int i = 0 ; i < GalePressApplication.getInstance().getMembershipMenuList().size(); i++){
                listHeight += membershipListItemView.getMeasuredHeight();
            }
            membershipListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));
        } else {
            membershipListView.setVisibility(View.GONE);
        }

        TextView title = (TextView)findViewById(R.id.action_bar_title_text_view);
        title.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        title.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        title.setText(title.getText().toString().toUpperCase());

        ((LinearLayout)findViewById(R.id.custom_actionbar_layout)).setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColorWithAlpha(98));

        searchView.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        searchView.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));
        searchView.setHintTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)searchView.getParent()).setBackground(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(this));
        else
            ((RelativeLayout)searchView.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(this));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
        else
            ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            clearSearch.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
        else
            clearSearch.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            menuButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MENU_ICON));
        else
            menuButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MENU_ICON));

        logoutButton.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        logoutButton.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            logoutButton.setBackground(ApplicationThemeColor.getInstance().getLogoutButtonDrawable(this));
        else
            logoutButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLogoutButtonDrawable(this));


        prepareTabBars(isColorChanged);

        if(getCurrentLibraryFragment() != null && getCurrentLibraryFragment().gridview != null)
            getCurrentLibraryFragment().gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());

        ileriButton = (ImageButton) findViewById(R.id.main_webview_ileri_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        else
            ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        geriButton = (ImageButton) findViewById(R.id.main_webview_geri_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        else
            geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        refreshButton = (ImageButton) findViewById(R.id.main_webview_refresh_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
        else
            refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));

        ileriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GalePressApplication.getInstance().getWebFragment().getCustomWebView().canGoForward()) {
                    GalePressApplication.getInstance().getWebFragment().getCustomWebView().goForward();
                }
            }
        });
        geriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GalePressApplication.getInstance().getWebFragment().getCustomWebView().canGoBack()){
                    GalePressApplication.getInstance().getWebFragment().getCustomWebView().goBack();
                }
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalePressApplication.getInstance().getWebFragment().getCustomWebView().reload();
            }
        });

    }

    public void prepareTabBars(boolean isColorChanged){
        mTabHost.getTabWidget().setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColor());

        int tabIndex = 0;
        //Home yoksa (+2 yapilmasinin sebebi static olan iki tab icin (library ve downloaded))
        if(mTabHost.getTabWidget().getTabCount() == 2 || mTabHost.getTabWidget().getTabCount() == GalePressApplication.getInstance().getTabList().size()+2){
            if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()){
                isTabFirstInit = true;
                mTabHost.clearAllTabs();
                setTabs();
                setCustomTabs();
                mTabHost.getTabWidget().setCurrentTab(1);
                mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);
                tabIndex++;
            }
        } else {// Home varsa
            if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()){
                isTabFirstInit = false;
                ((ImageView)((LinearLayout)mTabHost.getTabWidget().getChildAt(tabIndex)).getChildAt(0)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON),
                        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON_SELECTED)));
                tabIndex++;
            } else {
                mTabHost.clearAllTabs();
                setTabs();
                setCustomTabs();
                mTabHost.getTabWidget().setCurrentTab(0);
                mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);
            }
        }

        ((ImageView)((LinearLayout)mTabHost.getTabWidget().getChildAt(tabIndex)).getChildAt(0)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON_SELECTED)));
        tabIndex++;

        ((ImageView)((LinearLayout)mTabHost.getTabWidget().getChildAt(tabIndex)).getChildAt(0)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON_SELECTED)));
        tabIndex++;

        /*
        *((ImageView)((LinearLayout)mTabHost.getTabWidget().getChildAt(2)).getChildAt(0)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON),
        *        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON_SELECTED)));
        *tabIndex++;
        */

        //Renk ve tablist lerde bi degisiklik varsa custom tablari update ediyoruz
        if(isColorChanged && GalePressApplication.getInstance().getDataApi().isConnectedToInternet() && GalePressApplication.getInstance().getTabList() != null){
            int customIndex = tabIndex;
            for(TabbarItem item : GalePressApplication.getInstance().getTabList()){
                ImageView img = ((ImageView)((LinearLayout)mTabHost.getTabWidget().getChildAt(customIndex)).getChildAt(0));
                TextView txt = ((TextView)((LinearLayout)mTabHost.getTabWidget().getChildAt(customIndex)).getChildAt(1));
                txt.setText(item.getTitle());
                ApplicationThemeColor.getInstance().paintRemoteIcon(this, item, img);
                customIndex++;
            }
        }
    }

    private void setTabs() {
        //gecici bir nesne atayip ordan devam edersek sorun cozulur
        mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.getTabWidget().setDividerDrawable(new ColorDrawable(Color.TRANSPARENT));
        mTabHost.getTabWidget().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()){
            addTab(getResources().getString(R.string.HOME),HOME_TAB_TAG, createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON),
                    ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON_SELECTED)), HomeFragment.class, null);
        }

        addTab(getResources().getString(R.string.LIBRARY), LIBRARY_TAB_TAG, createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON_SELECTED)), LibraryFragment.class, null);
        addTab(getResources().getString(R.string.DOWNLOADED),DOWNLOADED_LIBRARY_TAG,createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON_SELECTED)),LibraryFragment.class, null);
        /*addTab(getResources().getString(R.string.INFO),INFO_TAB_TAG,createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON_SELECTED)),InfoPageFragment.class, null);*/

        if(GalePressApplication.getInstance().getTabList() != null && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
            int index = 0;
            for(TabbarItem item : GalePressApplication.getInstance().getTabList()){
                addTab(item.getTitle(),""+index, createDrawable(true, null, null), WebFragment.class, item);
                index++;
            }
        } else if(getSupportFragmentManager().getFragments() != null) {
            for(Fragment fragment : getSupportFragmentManager().getFragments()){
                if(fragment != null && fragment.getTag().compareTo(LIBRARY_TAB_TAG) != 0 && fragment.getTag().compareTo(DOWNLOADED_LIBRARY_TAG) != 0 && fragment.getTag().compareTo(INFO_TAB_TAG) != 0)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }

        if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()) {
            mTabHost.getTabWidget().setCurrentTab(1);
        }
        else
            mTabHost.getTabWidget().setCurrentTab(0);
        mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);

    }

    public void setCustomTabs(){

        if(getSupportFragmentManager().getFragments() != null){
            if(GalePressApplication.getInstance().getTabList() != null && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
                int tabCount = 2;
                if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded())
                    tabCount = 3;
                while(mTabHost.getTabWidget().getChildCount() > tabCount){
                    int tabSize = mTabHost.getTabWidget().getChildCount();
                    mTabHost.getTabWidget().removeView(mTabHost.getTabWidget().getChildTabViewAt(tabSize-1));
                }

                int index = 0;
                for(TabbarItem item : GalePressApplication.getInstance().getTabList()){
                    addTab(item.getTitle(),""+index, createDrawable(true, null, null), WebFragment.class, item);
                    index++;
                }
            } else {
                int tabCount = 2;
                if(GalePressApplication.getInstance().getDataApi().getMasterContent() != null && GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded()) {
                    mTabHost.getTabWidget().setCurrentTab(1);
                    tabCount = 3;
                }
                else
                    mTabHost.getTabWidget().setCurrentTab(0);
                mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);
                while(mTabHost.getTabWidget().getChildCount() > tabCount){
                    int tabSize = mTabHost.getTabWidget().getChildCount();
                    mTabHost.getTabWidget().removeView(mTabHost.getTabWidget().getChildTabViewAt(tabSize-1));
                }

            }
        }
    }


    public void selectMembershipListItem(int position){
        hideKeyboard(searchView);

        if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LeftMenuMembershipAdapter.LOGIN) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 102);
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LeftMenuMembershipAdapter.RESTORE) {
            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getResources().getString(R.string.Restore) + "...");
            progress.setCancelable(false);
            progress.show();
            GalePressApplication.getInstance().restorePurchasedProductsFromMarket(false, true, this, progress);
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LeftMenuMembershipAdapter.SUBSCRIPTION) {
            if(GalePressApplication.getInstance().getSubscriptions().size() > 0){
                ProgressDialog progress = new ProgressDialog(this);
                progress.setMessage(getResources().getString(R.string.subscription_check) + "...");
                progress.setCancelable(false);
                progress.show();
                GalePressApplication.getInstance().restoreSubscriptions(false, this, progress);
            } else {
                Toast.makeText(this, getResources().getString(R.string.subscription_warning), Toast.LENGTH_SHORT).show();
            }
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LeftMenuMembershipAdapter.LOGOUT){

            //Facebook logout
            FacebookSdk.sdkInitialize(this.getApplicationContext());
            LoginManager.getInstance().logOut();

            GalePressApplication.getInstance().editMemberShipList(false, null);
            membershipAdapter.notifyDataSetChanged();
            LayoutInflater mInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View membershipListItemView = mInflater.inflate(R.layout.left_menu_membership_item, null);
            membershipListItemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int listHeight = 0;
            for(int i = 0 ; i < GalePressApplication.getInstance().getMembershipMenuList().size(); i++){
                listHeight += membershipListItemView.getMeasuredHeight();
            }
            membershipListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));
            membershipListView.invalidate();
        }

    }


    public void openSubscriptionChooser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_subscription));
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_singlechoice);
        for(Subscription subs : GalePressApplication.getInstance().getSubscriptions()){

            String price = "";
            if(subs.getMarketPrice() != null && subs.getMarketPrice().compareTo("") != 0){
                price = subs.getMarketPrice();
            } else {
                price = subs.getPrice();
            }

            if(subs.getType() == Subscription.WEEK){
                if(!subs.isOwned()){
                    arrayAdapter.add(getResources().getString(R.string.WEEK) +" " + price);
                } else {
                    arrayAdapter.add(getResources().getString(R.string.subscription_type_owned , ""+getResources().getString(R.string.WEEK)));
                }
            } else if(subs.getType() == Subscription.MONTH){
                if(!subs.isOwned()){
                    arrayAdapter.add(getResources().getString(R.string.MONTH) +" " + price);
                } else {
                    arrayAdapter.add(getResources().getString(R.string.subscription_type_owned ,""+getResources().getString(R.string.MONTH)));
                }

            } else {
                if(!subs.isOwned()){
                    arrayAdapter.add(getResources().getString(R.string.YEAR) +" " + price);
                } else {
                    arrayAdapter.add(getResources().getString(R.string.subscription_type_owned ,""+getResources().getString(R.string.YEAR)));
                }
            }
        }
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedSubscription = GalePressApplication.getInstance().getSubscriptions().get(which);
                if(!selectedSubscription.isOwned())
                    subscribe();
                else
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.subscription_owned), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void subscribe(){

        try {
            Bundle buyIntentBundle = GalePressApplication.getInstance().getmService().getBuyIntent(3, getPackageName(),
                    selectedSubscription.getIdentifier(), "subs", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.BILLING_RESPONSE_RESULT_OK) { // Urun satin alinmamis
                // Start purchase flow (this brings up the Google Play UI).
                // Result will be delivered through onActivityResult().
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ITEM_ALREADY_OWNED){ // Urun daha once alinmis
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                        .show();
                selectedSubscription.setOwned(true);
                for (Subscription subs : GalePressApplication.getInstance().getSubscriptions())
                    if(subs.getIdentifier().compareTo(selectedSubscription.getIdentifier()) == 0)
                        subs.setOwned(true);
                GalePressApplication.getInstance().prepareSubscriptions(null);
            } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_USER_CANCELED){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                        .show();
            } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_BILLING_UNAVAILABLE){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
            } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ITEM_UNAVAILABLE){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
            } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ERROR){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                        .show();
            } else { //  Beklenmedik Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                        .show();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Drawable createDrawable(boolean isSelected, Drawable res, Drawable selectedRes) {
        if(res != null && selectedRes != null){
            StateListDrawableWithColorFilter states = new StateListDrawableWithColorFilter(isSelected,res, selectedRes);
            return states;
        }
        return null;
    }

    private ColorStateList createTabTitleColorStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors = new int[] {
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColor()
        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    private View createTabIndicator(String titleText, Drawable drawable, TabbarItem item) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, mTabHost.getTabWidget(), false);
        tabIndicator.setBackgroundColor(Color.TRANSPARENT);
        tabIndicator.setLayoutParams(new FrameLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())));
        ImageView imgIcon = (ImageView) tabIndicator.findViewById(R.id.image_view_tab_icon);
        imgIcon.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics())));

        TextView title = (TextView) tabIndicator.findViewById(R.id.text_view_tab_title);
        title.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
        title.setTextColor(createTabTitleColorStateList());
        title.setClickable(true);
        title.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        title.setText(titleText);

        if(drawable != null)
            imgIcon.setImageDrawable(drawable);
        else
            ApplicationThemeColor.getInstance().paintRemoteIcon(this, item, imgIcon);

        return tabIndicator;
    }

    private void addTab(String title, String tag, Drawable drawable,Class classy, TabbarItem item) {
        TabHost.TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setIndicator(createTabIndicator(title, drawable, item));
        mTabHost.addTab(spec,classy,null);
    }

    public void prepareActionBarForCustomTab(WebView webView, boolean isWebFragment, boolean isPageLoadFinish){
        if(isWebFragment){
            ileriButton.setVisibility(View.VISIBLE);
            geriButton.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
            menuButton.setVisibility(View.GONE);
            mDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
            ((TextView)findViewById(R.id.action_bar_title_text_view)).setVisibility(View.GONE);
        } else{
            ileriButton.setVisibility(View.INVISIBLE);
            geriButton.setVisibility(View.INVISIBLE);
            refreshButton.setVisibility(View.INVISIBLE);
            menuButton.setVisibility(View.VISIBLE);
            mDrawer.setTouchMode(MenuDrawer.FOCUSABLES_TOUCH_MODE);
            ((TextView)findViewById(R.id.action_bar_title_text_view)).setVisibility(View.VISIBLE);
        }

        if(isPageLoadFinish){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
            else
                refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
        }

        if(webView != null){
            // if has previous page, enable the back button
            if(webView.canGoBack()){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
                else
                    geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
            }else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
                else
                    geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
            }
            // if has next page, enable the next button
            if(webView.canGoForward()){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
                else
                    ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
            } else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
                else
                    ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));
            else
                refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));
        }
    }

    public void hideKeyboard(View view) {
        changeSearchViewColor(false);
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void changeSearchViewColor(boolean hasFocus){
        if(hasFocus){
            searchView.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            searchView.setHintTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)searchView.getParent()).setBackground(ApplicationThemeColor.getInstance().getActiveSearchViewDrawable(MainActivity.this));
            else
                ((RelativeLayout)searchView.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getActiveSearchViewDrawable(MainActivity.this));

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.SEARCH_ICON));
            else
                ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.SEARCH_ICON));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                clearSearch.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
            else
                clearSearch.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
        } else {
            searchView.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));
            searchView.setHintTextColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(50));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)searchView.getParent()).setBackground(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(MainActivity.this));
            else
                ((RelativeLayout)searchView.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(MainActivity.this));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
            else
                ((ImageView)findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                clearSearch.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_CLEAR_ICON));
            else
                clearSearch.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_CLEAR_ICON));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GalePressApplication.getInstance().onActivityResult(requestCode,resultCode,data);

        if(requestCode == 1001){
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK && responseCode == GalePressApplication.BILLING_RESPONSE_RESULT_OK) {
                try {
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESPONSE_RESULT_OK), Toast.LENGTH_SHORT)
                            .show();
                    selectedSubscription.setOwned(true);

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    for (Subscription subs : GalePressApplication.getInstance().getSubscriptions())
                        if(subs.getIdentifier().compareTo(selectedSubscription.getIdentifier()) == 0)
                            subs.setOwned(true);
                }
                catch (JSONException e) {
                    Toast.makeText(this, "act result json parse error - "+e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if(resultCode == RESULT_OK && responseCode == GalePressApplication.RESULT_ITEM_ALREADY_OWNED){
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                        .show();
                selectedSubscription.setOwned(true);
                for (Subscription subs : GalePressApplication.getInstance().getSubscriptions())
                    if(subs.getIdentifier().compareTo(selectedSubscription.getIdentifier()) == 0)
                        subs.setOwned(true);
            } else if (responseCode == GalePressApplication.RESULT_USER_CANCELED){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                        .show();
            } else if (responseCode == GalePressApplication.RESULT_BILLING_UNAVAILABLE){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
            } else if (responseCode == GalePressApplication.RESULT_ITEM_UNAVAILABLE){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
            } else if (responseCode == GalePressApplication.RESULT_ERROR){ // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                        .show();
            } else { //  Beklenmedik Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                        .show();
            }
            GalePressApplication.getInstance().prepareSubscriptions(null);
        } else {
            if(resultCode == 101) { //reader view return
                int type = data.getIntExtra("SelectedTab", 0);
                //mTabHost.getTabWidget().setCurrentTab(type);

                if(type == 0){
                    mTabHost.setCurrentTabByTag(HOME_TAB_TAG);
                } else if(type == 1){
                    mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG);
                } else if(type == 2) {
                    mTabHost.setCurrentTabByTag(DOWNLOADED_LIBRARY_TAG);
                } else if(type == 3){
                    mTabHost.setCurrentTabByTag(INFO_TAB_TAG);
                } else { // 100+ type olanlar servisten gelen buttonlar
                    String customTabTag = ""+(type- 100);
                    mTabHost.setCurrentTabByTag(customTabTag);
                }
            } else if(resultCode == 102) { //Login return
                membershipAdapter.notifyDataSetChanged();
                LayoutInflater mInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                View membershipListItemView = mInflater.inflate(R.layout.left_menu_membership_item, null);
                membershipListItemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int listHeight = 0;
                for(int i = 0 ; i < GalePressApplication.getInstance().getMembershipMenuList().size(); i++){
                    listHeight += membershipListItemView.getMeasuredHeight();
                }
                membershipListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));
                membershipListView.invalidate();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        GalePressApplication.getInstance().setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Cancel AsyncTask
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        try {
            // Unregister Broadcast Receiver
            unregisterReceiver(mHandleMessageReceiver);

            //Clear internal resources.
            GCMRegistrar.onDestroy(this);

        } catch (Exception e) {
            Logout.e("UnRegister Receiver Error", "> " + e.getMessage());
        }

        clearReferences();
        GalePressApplication.getInstance().setMainActivity(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        GalePressApplication.getInstance().destroyBillingServices();
        try{
            unregisterReceiver(mConnReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        L_Category selectedCategory = null;
        if(item.getTitle().toString().compareTo(getString(R.string.show_all))!=0){
            List categories = GalePressApplication.getInstance().getDatabaseApi().getAllCategories();
            for(int i =0; i<categories.size(); i++){
                L_Category category = (L_Category)categories.get(i);
                if(category.getCategoryName().compareTo(item.getTitle().toString())==0){
                    selectedCategory = category;
                }
            }
        }
        else{
            selectedCategory = new L_Category(-1, getString(R.string.show_all));
        }
        LibraryFragment libraryFragment = getCurrentLibraryFragment();
        libraryFragment.selectedCategory = selectedCategory;
        libraryFragment.updateCategoryList();
        libraryFragment.updateGridView();

        Logout.e("Adem", "OnPopUpMenuItem Clicked:"+item.getTitle().toString());
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        return super.onOptionsItemSelected(item);

}

    public LibraryFragment getCurrentLibraryFragment(){
        if(getSupportFragmentManager().getFragments() != null){
            int count = getSupportFragmentManager().getFragments().size();
            for(int i=0; i< count; i++){
                LibraryFragment fragment = (LibraryFragment) getSupportFragmentManager().getFragments().get(i);
                if(fragment != null && fragment.getTag().compareTo(LIBRARY_TAB_TAG) == 0 || fragment.getTag().compareTo(DOWNLOADED_LIBRARY_TAG) == 0)
                    return (LibraryFragment)fragment;
            }
        }
        return null;
    }

    public LibraryFragment getCurrentDownloadedLibraryFragment(){
        if(getSupportFragmentManager().getFragments() != null){
            int count = getSupportFragmentManager().getFragments().size();
            for(int i=0; i< count; i++){
                LibraryFragment fragment = (LibraryFragment) getSupportFragmentManager().getFragments().get(i);
                if(fragment != null && fragment.getTag().compareTo(DOWNLOADED_LIBRARY_TAG) == 0)
                    return (LibraryFragment)fragment;
            }
        }
        return null;
    }

    // Create a broadcast receiver to get message and show on screen
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String newMessage = intent.getExtras().getString("Message");

            // Waking up mobile if it is sleeping
            GalePressApplication.getInstance().acquireWakeLock(getApplicationContext());

            // Display message on the screen
//            lblMessage.append(newMessage + "\n.");

//            Toast.makeText(getApplicationContext(), "Got Message: " + newMessage, Toast.LENGTH_LONG).show();

            // Releasing wake lock
//            aController.releaseWakeLock();
            GalePressApplication.getInstance().releaseWakeLock();
        }
    };

    private void clearReferences(){
        Activity currActivity = GalePressApplication.getInstance().getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            GalePressApplication.getInstance().setCurrentActivity(null);
    }

}