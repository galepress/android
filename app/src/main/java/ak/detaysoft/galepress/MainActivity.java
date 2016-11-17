package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gcm.GCMRegistrar;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ak.detaysoft.galepress.custom_models.Subscription;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.search_models.MenuSearchResult;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by adem on 31/03/14.
 */
public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener {

    public static final int GENEL_CATEGORY_ID = 0;
    public static final int SHOW_ALL_CATEGORY_ID = -1;
    public static final int CONTEXT_MENU_GROUP_ID = 1;
    private EditText searchEdittext;
    private ImageView menuButton;
    private ImageView searchMenuButton;
    AsyncTask<Void, Void, Void> mRegisterTask;
    public Integer content_id = null;
    private ArrayList<Fragment> fragmentList;


    private SlidingMenu leftMenu;

    //Kategori sekmesi
    private LeftMenuCategoryAdapter categoriesAdapter;
    private ListView categoriesListView;
    private List<L_Category> categoryListWithAll;
    private RelativeLayout categoriesTitleLayout;
    private ImageView categoriesListViewCloseIcon;

    private ImageView searchClear;

    private LinearLayout leftMenuBaseLayout;


    private Subscription selectedSubscription;
    public ProgressBar searchProgress;
    private LibraryFragment libraryFragment;
    private ApplicationFragment applicationFragment;
    private TextView actionbarTitle;


    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("oncreate", "mainActivity");

        Intent intent = getIntent();
        if (intent.hasExtra("content_id")) {
            this.content_id = Integer.valueOf(intent.getStringExtra("content_id"));
        } else {
            this.content_id = null;
        }

        try {
            registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {
            Log.e("ConnectivityManager", e.toString());
        }

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        applicationFragment = new ApplicationFragment();
        fragmentTransaction.add(R.id.fragment_container, applicationFragment, "APPLICATION").addToBackStack(null);
        fragmentTransaction.commit();

        leftMenu = new SlidingMenu(this);
        leftMenu.setMode(SlidingMenu.LEFT_RIGHT);
        leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        leftMenu.setTouchmodeMarginThreshold(50);
        leftMenu.setFadeDegree(0.35f);
        leftMenu.setBehindWidth((int) getResources().getDimension(R.dimen.left_menu_size));
        leftMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        leftMenu.setMenu(R.layout.left_menu);
        leftMenu.setSecondaryMenu(R.layout.right_menu);

        leftMenuBaseLayout = (LinearLayout) findViewById(R.id.left_menu_layout);
        categoriesTitleLayout = (RelativeLayout) findViewById(R.id.left_categories_layout);
        categoriesTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(searchEdittext);
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
        categoryListWithAll.add(0, new L_Category(-1, getResources().getString(R.string.DOWNLOADED).toUpperCase()));
        categoriesListView = (ListView) findViewById(R.id.left_menu_category_list);

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                hideKeyboard(searchEdittext);
                GalePressApplication.getInstance().setSelectedCustomerApplication(null);
                GalePressApplication.getInstance().setLibraryFragment(null);
                actionbarTitle.setText(categoryListWithAll.get(position).getName().toUpperCase());
                applicationFragment.selectedCategory = categoryListWithAll.get(position);
                if(applicationFragment.selectedCategory.getId().intValue() == -1)
                    applicationFragment.isDownloaded = true;
                else
                    applicationFragment.isDownloaded = false;
                applicationFragment.selectedCategoryPosition = position;
                if(libraryFragment != null && libraryFragment.isVisible()) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, applicationFragment).addToBackStack(null).commit();
                }

                applicationFragment.updateGridView();
                categoriesAdapter.notifyDataSetChanged();
            }
        });
        categoriesListViewCloseIcon = (ImageView) findViewById(R.id.left_menu_categories_close);

        categoriesAdapter = new LeftMenuCategoryAdapter(this, categoryListWithAll);
        categoriesListView.setAdapter(categoriesAdapter);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        searchEdittext = (EditText) findViewById(R.id.left_menu_search_edit_text);
        //React to Done button on keyboard
        searchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && searchEdittext.getText().length() > 0) {
                    GalePressApplication.getInstance().setSearchQuery(searchEdittext.getText().toString());
                    GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                    List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(GalePressApplication.getInstance().getSearchQuery());
                    for (int i = 0; i < contents.size(); i++) {
                        MenuSearchResult temp = new MenuSearchResult();
                        temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                        temp.setContentTitle(((L_Content) contents.get(i)).getName());
                        temp.setPage(-1);
                        GalePressApplication.getInstance().getMenuSearchResult().add(temp);

                    }
                    searchProgress.setVisibility(View.VISIBLE);
                    searchClear.setVisibility(View.GONE);
                    complateSearch(false, false);

                    GalePressApplication.getInstance().getDataApi().fullTextSearch(GalePressApplication.getInstance().getSearchQuery(), MainActivity.this);
                }
                return false;
            }
        });

        searchEdittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && searchEdittext.getText().length() > 0) {

                    GalePressApplication.getInstance().setSearchQuery(searchEdittext.getText().toString());
                    GalePressApplication.getInstance().setMenuSearchResult(new ArrayList<MenuSearchResult>());
                    List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContentsWithSqlQuery(GalePressApplication.getInstance().getSearchQuery());
                    for (int i = 0; i < contents.size(); i++) {
                        MenuSearchResult temp = new MenuSearchResult();
                        temp.setContentId(((L_Content) contents.get(i)).getId().toString());
                        temp.setContentTitle(((L_Content) contents.get(i)).getName());
                        temp.setPage(-1);
                        GalePressApplication.getInstance().getMenuSearchResult().add(temp);

                    }
                    searchProgress.setVisibility(View.VISIBLE);
                    searchClear.setVisibility(View.GONE);
                    complateSearch(false, false);

                    GalePressApplication.getInstance().getDataApi().fullTextSearch(GalePressApplication.getInstance().getSearchQuery(), MainActivity.this);
                }
                return false;
            }
        });
        searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    searchClear.setVisibility(View.GONE);
                    findViewById(R.id.left_menu_search_icon).setVisibility(View.VISIBLE);
                } else {
                    searchClear.setVisibility(View.VISIBLE);
                    findViewById(R.id.left_menu_search_icon).setVisibility(View.GONE);
                }
                changeSearchViewColor(true);

            }
        });
        searchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });
        searchEdittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeSearchViewColor(true);
                return false;
            }
        });


        searchClear = (ImageView) findViewById(R.id.left_menu_search_clear);
        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchProgress.setVisibility(View.GONE);
                GalePressApplication.getInstance().setSearchQuery("");
                //GalePressApplication.getInstance().setMenuSearchResults(null);
                GalePressApplication.getInstance().setMenuSearchResult(null);
                complateSearch(false, false);
            }
        });

        searchProgress = (ProgressBar) findViewById(R.id.search_progress);
        searchProgress.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);

        menuButton = (ImageView) findViewById(R.id.menu_button);
        ((LinearLayout) findViewById(R.id.menu_button_layout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (leftMenu.isMenuShowing())
                    leftMenu.showContent(true);
                else {
                    leftMenu.showMenu(true);
                }
            }
        });

        searchMenuButton = (ImageView) findViewById(R.id.search_menu_button);
        ((LinearLayout) findViewById(R.id.search_menu_button_layout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (leftMenu.isMenuShowing())
                    leftMenu.showContent(true);
                else {
                    leftMenu.showSecondaryMenu(true);
                }
            }
        });


        findViewById(R.id.left_menu_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GalePressApplication.getInstance().getUserInformation() != null) {
                    logout();
                    ProgressDialog progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage(getResources().getString(R.string.logout) + "...");
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().restorePurchasedProductsFromMarket(true, MainActivity.this, progress);
                } else {
                    Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
                    intent.putExtra("action", UserLoginActivity.ACTION_MENU);
                    intent.putExtra("isLaunchOpen", false);
                    startActivityForResult(intent, 102);
                }
            }
        });

        TextView userInfo = ((TextView) findViewById(R.id.left_menu_login_text));
        userInfo.setTextColor(Color.WHITE);
        userInfo.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(this));
        if (GalePressApplication.getInstance().getUserInformation() != null) {
            userInfo.setText(GalePressApplication.getInstance().getUserInformation().getUserName());
        } else {
            userInfo.setText(getResources().getText(R.string.login_stand));
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
            Logout.e("Galepress", "Is Registered " + (GCMRegistrar.isRegisteredOnServer(GalePressApplication.getInstance().getApplicationContext()) ? "YES" : "NO"));
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
        actionbarTitle = (TextView) findViewById(R.id.action_bar_title_text_view);
        updateActivityViewAndAdapter(true);

        GalePressApplication.getInstance().setCurrentActivity(this);
        GalePressApplication.getInstance().setMainActivity(this);
        GalePressApplication.getInstance().getDataApi().getAppDetail(this);

    }



    public void choseCategory(int position) {
        GalePressApplication.getInstance().setSelectedCustomerApplication(null);
        GalePressApplication.getInstance().setLibraryFragment(null);
        actionbarTitle.setText(categoryListWithAll.get(position).getName().toUpperCase());
        categoriesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /*
        * Eger super cagrilirsa initCustomTabs() metodunda mTabHost.setCurrentTabByTag(LIBRARY_TAB_TAG); satirinda crash oluyor uygulama.
        * Bu durum her zaman olmuyor sadece eger o sirada baska bi activity aciksa internet state degisirse crash oluyor.
        * (MG)
        * */
        //super.onSaveInstanceState(outState);
    }

    /*
    * Renk degismedigi zaman servisten gelen tabbar ikonları invalidate edilmiyor.
    * Yoksa uygulama icinde her update oldugunda ikonlarda yeniden load edildigi icin kotu gorunuyor
    */
    public void updateActivityViewAndAdapter(boolean isColorChanged) {

        leftMenuBaseLayout.setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());

        //Kategori sekmesi
        categoriesTitleLayout.setBackgroundColor(Color.TRANSPARENT);
        ((TextView) (findViewById(R.id.left_menu_category_text))).setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        ((TextView) (findViewById(R.id.left_menu_category_text))).setTextColor(ApplicationThemeColor.getInstance().getThemeColorWithAlpha(50));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            categoriesListViewCloseIcon.setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));
        else
            categoriesListViewCloseIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_DOWN));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView) findViewById(R.id.left_menu_category_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_CATEGORY));
        else
            ((ImageView) findViewById(R.id.left_menu_category_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.LEFT_MENU_CATEGORY));

        categoryListWithAll = GalePressApplication.getInstance().getDatabaseApi().getAllCategories();
        categoryListWithAll.add(0, new L_Category(-1, getResources().getString(R.string.DOWNLOADED).toUpperCase()));

        categoriesAdapter.setmCategory(categoryListWithAll);
        categoriesAdapter.notifyDataSetChanged();
        categoriesListView.invalidate();


        //Kategori ve baglantilar listviewlerin height hesaplamasi. Scroll engelleyebilmek icin
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View listItemView = mInflater.inflate(R.layout.left_menu_category_item, categoriesListView, false);
        listItemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int listHeight = 0;
        for (int i = 0; i < categoryListWithAll.size(); i++) {
            listHeight += (int) getResources().getDimension(R.dimen.menu_category_item_size);
        }
        categoriesListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight));

        actionbarTitle.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        actionbarTitle.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(this));

        ((LinearLayout) findViewById(R.id.custom_actionbar_layout)).setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColor());

        searchEdittext.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(this));
        searchEdittext.setTextColor(Color.WHITE);
        searchEdittext.setHintTextColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout) searchEdittext.getParent()).setBackground(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(this));
        else
            ((RelativeLayout) searchEdittext.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
        else
            ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            searchClear.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
        else
            searchClear.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            menuButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MENU_ICON));
        else
            menuButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MENU_ICON));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            searchMenuButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_MENU_ICON));
        else
            searchMenuButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_MENU_ICON));

        /*
        * forceDelete ile silinen yada pasif hale getirilen icerikler oldugunda gridi update etmek icin yazdim(MG)
        * */
        if (applicationFragment != null && applicationFragment.gridview != null) {
            applicationFragment.gridview.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
            applicationFragment.getContentHolderAdapter().notifyDataSetChanged();
        }

    }

    public void logout() {
        //Facebook logout
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        LoginManager.getInstance().logOut();
        GalePressApplication.getInstance().createUser(false, null);
        ((TextView) findViewById(R.id.left_menu_login_text)).setText(getResources().getText(R.string.login_stand));
    }


    public void openSubscriptionChooser() {
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
        for (Subscription subs : GalePressApplication.getInstance().getSubscriptions()) {

            String price = "";
            if (subs.getMarketPrice() != null && subs.getMarketPrice().compareTo("") != 0) {
                price = subs.getMarketPrice();
            } else {
                price = subs.getPrice();
            }

            if (subs.getType() == Subscription.WEEK) {
                arrayAdapter.add(getResources().getString(R.string.WEEK) + " " + price);
            } else if (subs.getType() == Subscription.MONTH) {
                arrayAdapter.add(getResources().getString(R.string.MONTH) + " " + price);

            } else {
                arrayAdapter.add(getResources().getString(R.string.YEAR) + " " + price);
            }
        }
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*Subscription ownedSub = null;
                for (Subscription sub : GalePressApplication.getInstance().getSubscriptions())
                    if (sub.isOwned())
                        ownedSub = sub;
                selectedSubscription = GalePressApplication.getInstance().getSubscriptions().get(which);
                if (ownedSub != null) {
                    if (ownedSub.getType() == Subscription.WEEK)
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.subscription_type_owned, "" + getResources().getString(R.string.WEEK)), Toast.LENGTH_SHORT)
                                .show();
                    if (ownedSub.getType() == Subscription.MONTH)
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.subscription_type_owned, "" + getResources().getString(R.string.MONTH)), Toast.LENGTH_SHORT)
                                .show();
                    if (ownedSub.getType() == Subscription.YEAR)
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.subscription_type_owned, "" + getResources().getString(R.string.YEAR)), Toast.LENGTH_SHORT)
                                .show();
                } else {
                    subscribe();
                }*/

                selectedSubscription = GalePressApplication.getInstance().getSubscriptions().get(which);
                subscribe();
            }
        });
        builder.show();
    }

    private void subscribe() {

        if (GalePressApplication.getInstance().getmService() != null) {
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
                } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ITEM_ALREADY_OWNED) { // Urun daha once alinmis
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                            .show();
                    selectedSubscription.setOwned(true);
                    for (Subscription subs : GalePressApplication.getInstance().getSubscriptions())
                        if (subs.getIdentifier().compareTo(selectedSubscription.getIdentifier()) == 0)
                            subs.setOwned(true);
                    GalePressApplication.getInstance().prepareSubscriptions(null);
                } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_USER_CANCELED) { // Hata var
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                            .show();
                } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_BILLING_UNAVAILABLE) { // Hata var
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                            .show();
                } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ITEM_UNAVAILABLE) { // Hata var
                    Toast.makeText(this, this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                            .show();
                } else if (buyIntentBundle.getInt("RESPONSE_CODE") == GalePressApplication.RESULT_ERROR) { // Hata var
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                    .show();
        }

    }


    public void hideKeyboard(View view) {
        changeSearchViewColor(false);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void changeSearchViewColor(boolean hasFocus) {
        if (hasFocus) {
            searchEdittext.setTextColor(Color.WHITE);
            searchEdittext.setHintTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout) searchEdittext.getParent()).setBackground(ApplicationThemeColor.getInstance().getActiveSearchViewDrawable(MainActivity.this));
            else
                ((RelativeLayout) searchEdittext.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getActiveSearchViewDrawable(MainActivity.this));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.SEARCH_ICON));
            else
                ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.SEARCH_ICON));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                searchClear.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
            else
                searchClear.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.SEARCH_CLEAR));
        } else {
            searchEdittext.setTextColor(Color.WHITE);
            searchEdittext.setHintTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout) searchEdittext.getParent()).setBackground(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(MainActivity.this));
            else
                ((RelativeLayout) searchEdittext.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().getPassiveSearchViewDrawable(MainActivity.this));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackground(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
            else
                ((ImageView) findViewById(R.id.left_menu_search_icon)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(MainActivity.this, ApplicationThemeColor.PASSIVE_SEARCH_ICON));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                searchClear.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_CLEAR_ICON));
            else
                searchClear.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.PASSIVE_SEARCH_CLEAR_ICON));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GalePressApplication.getInstance().onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK && responseCode == GalePressApplication.BILLING_RESPONSE_RESULT_OK) {
                try {
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESPONSE_RESULT_OK), Toast.LENGTH_SHORT)
                            .show();

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");

                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, MainActivity.this);
                } catch (JSONException e) {
                    Toast.makeText(this, MainActivity.this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_OK && responseCode == GalePressApplication.RESULT_ITEM_ALREADY_OWNED) {

                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                            .show();
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, MainActivity.this);
                } catch (JSONException e) {
                    Toast.makeText(this, "act result json parse error - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            } else if (responseCode == GalePressApplication.RESULT_USER_CANCELED) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                        .show();
                GalePressApplication.getInstance().prepareSubscriptions(null);
            } else if (responseCode == GalePressApplication.RESULT_BILLING_UNAVAILABLE) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                GalePressApplication.getInstance().prepareSubscriptions(null);
            } else if (responseCode == GalePressApplication.RESULT_ITEM_UNAVAILABLE) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                GalePressApplication.getInstance().prepareSubscriptions(null);
            } else if (responseCode == GalePressApplication.RESULT_ERROR) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                        .show();
                GalePressApplication.getInstance().prepareSubscriptions(null);
            } else { //  Beklenmedik Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                        .show();
                GalePressApplication.getInstance().prepareSubscriptions(null);
            }

        }
        if (requestCode == 1002) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK && responseCode == LibraryFragment.BILLING_RESPONSE_RESULT_OK) {
                try {
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESPONSE_RESULT_OK), Toast.LENGTH_SHORT)
                            .show();

                    JSONObject jo = new JSONObject(purchaseData);
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, this);
                } catch (JSONException e) {
                    Toast.makeText(this, "act result json parse error - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_OK && responseCode == LibraryFragment.RESULT_ITEM_ALREADY_OWNED) {

                try {
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                            .show();

                    JSONObject jo = new JSONObject(purchaseData);
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, this);
                } catch (JSONException e) {
                    Toast.makeText(this, this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (responseCode == LibraryFragment.RESULT_USER_CANCELED) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                        .show();
                getLibraryFragment().getHeaderContentHolder().downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == LibraryFragment.RESULT_BILLING_UNAVAILABLE) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                getLibraryFragment().getHeaderContentHolder().downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == LibraryFragment.RESULT_ITEM_UNAVAILABLE) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                getLibraryFragment().getHeaderContentHolder().downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == LibraryFragment.RESULT_ERROR) { // Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                        .show();
                getLibraryFragment().getHeaderContentHolder().downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else { //  Beklenmedik Hata var
                Toast.makeText(this, this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                        .show();
                getLibraryFragment().getHeaderContentHolder().downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            }
        } else if (requestCode == 102) { //Login return
            if (resultCode == 102) {
                ((TextView) findViewById(R.id.left_menu_login_text)).setText(GalePressApplication.getInstance().getUserInformation().getUserName());
            }
        } else if (requestCode == 103) { //contentdetailactivity ekraninda kullanici login degilse kullaniciyi logine gönderiyoruz
            if (resultCode == 103) {
                Intent intent = new Intent(this, UserLoginActivity.class);
                intent.putExtra("action", UserLoginActivity.ACTION_MENU);
                intent.putExtra("isLaunchOpen", false);
                startActivityForResult(intent, 102);
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
    protected void onStop() {
        clearReferences();
        super.onStop();
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

    public void openLibraryFragment(){
        libraryFragment = new LibraryFragment();
        libraryFragment.isDownloaded = applicationFragment.isDownloaded;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, libraryFragment, "LIBRARY").addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            GalePressApplication.getInstance().destroyBillingServices();
            try {
                unregisterReceiver(mConnReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, applicationFragment).addToBackStack(null).commit();
            GalePressApplication.getInstance().setSelectedCustomerApplication(null);
            GalePressApplication.getInstance().setLibraryFragment(null);
            actionbarTitle.setText(applicationFragment.selectedCategory.getName().toUpperCase());
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if(fragmentList == null){
            fragmentList = new ArrayList<Fragment>();
            fragmentList.add(fragment);
        } else {
            if(fragmentList.get(fragmentList.size()-1).getTag() != null && fragment.getTag().compareTo(fragmentList.get(fragmentList.size()-1).getTag()) != 0)
                fragmentList.add(fragment);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Logout.e("Galepress", "OnPopUpMenuItem Clicked:" + item.getTitle().toString());
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

    public LibraryFragment getLibraryFragment() {
        return libraryFragment;
    }

    public ApplicationFragment getApplicationFragment() {
        return applicationFragment;
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

    private void clearReferences() {
        Activity currActivity = GalePressApplication.getInstance().getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            GalePressApplication.getInstance().setCurrentActivity(null);
    }


    public void completePurchase() {
        Toast.makeText(this, getResources().getString(R.string.purchase_validation_success), Toast.LENGTH_LONG).show();
        selectedSubscription.setOwned(true);
        for (Subscription subs : GalePressApplication.getInstance().getSubscriptions())
            if (subs.getIdentifier().compareTo(selectedSubscription.getIdentifier()) == 0)
                subs.setOwned(true);
        GalePressApplication.getInstance().setUserHaveActiveSubscription(true);
        GalePressApplication.getInstance().prepareSubscriptions(null);
    }

    public void complateSearch(boolean isServiceFinishCall, boolean showNotFoundMessage) {
        if (isServiceFinishCall) {
            searchProgress.setVisibility(View.GONE);
            searchClear.setVisibility(View.VISIBLE);
        }
        LinearLayout baseView = (LinearLayout) findViewById(R.id.search_result_layout);
        RecyclerView list = (RecyclerView) findViewById(R.id.search_recycler_view);
        if (GalePressApplication.getInstance().getMenuSearchResult() != null && GalePressApplication.getInstance().getMenuSearchResult().size() > 0) {
            baseView.setVisibility(View.VISIBLE);
            findViewById(R.id.search_result_layout_divider).setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            list.setLayoutManager(mLayoutManager);
            if (list.getAdapter() != null) {
                ((SearchAdapter) list.getAdapter()).searchList = GalePressApplication.getInstance().getMenuSearchResult();
                ((RecyclerView.Adapter) list.getAdapter()).notifyDataSetChanged();
            } else {
                SearchAdapter mAdapter = new SearchAdapter(GalePressApplication.getInstance().getMenuSearchResult());
                list.setAdapter(mAdapter);
            }
        } else {
            baseView.setVisibility(View.GONE);
            if (showNotFoundMessage)
                Toast.makeText(this, getResources().getString(R.string.text_not_found), Toast.LENGTH_SHORT).show();
        }

    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {
        private ArrayList<MenuSearchResult> searchList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView text, page;
            public MenuSearchResult result = new MenuSearchResult();

            public MyViewHolder(View view) {
                super(view);
                text = (TextView) view.findViewById(R.id.search_result_menu_title);
                page = (TextView) view.findViewById(R.id.search_result_menu_page);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (result.getPage() != -1) {
                            L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.valueOf(result.getContentId()));
                            if (content != null) {
                                if (content.isPdfDownloaded()) {
                                    openContentReader(content, result.getPage() - 1, GalePressApplication.getInstance().getSearchQuery());
                                    leftMenu.showContent(true);
                                } else {
                                    openContentDetail(content, result.getPage() - 1, GalePressApplication.getInstance().getSearchQuery());
                                    leftMenu.showContent(true);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.cannot_open_document), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            L_Content content = GalePressApplication.getInstance().getDatabaseApi().getContent(Integer.valueOf(result.getContentId()));
                            if (content != null) {
                                openContentDetail(content, -1, "");
                                leftMenu.showContent(true);
                            } else {
                                Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.cannot_open_document), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }

        public SearchAdapter(ArrayList<MenuSearchResult> searchList) {
            this.searchList = searchList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public SearchAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_item_menu, parent, false);

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.result = searchList.get(position);
            if (searchList.get(position).getPage() == -1) {
                holder.text.setText(searchList.get(position).getContentTitle());
                holder.text.setTextColor(Color.WHITE);
                holder.text.setTypeface(ApplicationThemeColor.getInstance().getGothamMedium(MainActivity.this));

                holder.page.setText("");
            } else {

                holder.text.setText(Html.fromHtml(searchList.get(position).getText()));
                holder.text.setTextColor(Color.WHITE);
                holder.text.setTypeface(ApplicationThemeColor.getInstance().getGothamBookItalic(MainActivity.this));

                holder.page.setText("" + searchList.get(position).getPage());
                holder.page.setTextColor(Color.WHITE);
                holder.page.setTypeface(ApplicationThemeColor.getInstance().getGothamBookItalic(MainActivity.this));
            }


        }

        @Override
        public int getItemCount() {
            return searchList.size();
        }
    }

    public void openContentDetail(L_Content content, int searchPage, String searchQuery) {
        Intent intent = new Intent(this, ContentDetailPopupActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("animationStartX", 0.5f);
        intent.putExtra("animationStartY", 0.5f);
        if (searchPage != -1) {
            intent.putExtra("searchPage", searchPage);
            intent.putExtra("searchQuery", searchQuery);
        }
        startActivityForResult(intent, 103);
    }

    public void openContentReader(L_Content content, int searchPage, String searchQuery) {
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
            Intent intent = new Intent(this, MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("searchPage", searchPage);
            intent.putExtra("searchQuery", searchQuery);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            this.startActivityForResult(intent, 101);
        }
    }


    public LeftMenuCategoryAdapter getCategoriesAdapter() {
        return categoriesAdapter;
    }

    public List<L_Category> getCategoryListWithAll() {
        return categoryListWithAll;
    }

    public void setCategoryListWithAll(List<L_Category> categoryListWithAll) {
        this.categoryListWithAll = categoryListWithAll;
    }

    public TextView getActionbarTitle() {
        return actionbarTitle;
    }

    public void setActionbarTitle(TextView actionbarTitle) {
        this.actionbarTitle = actionbarTitle;
    }
}
