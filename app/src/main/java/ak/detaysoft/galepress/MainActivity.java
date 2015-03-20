package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.Duration;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.view.TabBitmap;

/**
 * Created by adem on 31/03/14.
 */
public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener{

    public static final int GENEL_CATEGORY_ID = 0;
    public static final int SHOW_ALL_CATEGORY_ID = -1;
    public static final int CONTEXT_MENU_GROUP_ID = 1;
    public static final String LIBRARY_TAB_TAG = "LIBRARY_TAB";
    public static final String DOWNLOADED_LIBRARY_TAG = "DOWNLOADED_TAB";
    private static final String INFO_TAB_TAG = "INFO_TAB";
    public FragmentTabHost mTabHost;
    private android.support.v7.widget.SearchView searchView;
    private Button categoriesButton;
    AsyncTask<Void, Void, Void> mRegisterTask;
    public Integer content_id = null;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra("content_id")){
            this.content_id = Integer.valueOf(intent.getStringExtra("content_id"));
        }
        else{
            this.content_id = null;
        }


        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setTabs();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (android.support.v7.widget.SearchView)findViewById(R.id.search_view);
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
        }
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                LibraryFragment libraryFragment = getCurrentFragment();
                libraryFragment.searchQuery = newText;
                libraryFragment.updateGridView();
                return true;
            }
            public boolean onQueryTextSubmit(String query) {
                LibraryFragment libraryFragment = getCurrentFragment();
                libraryFragment.searchQuery = "";
                searchView.onActionViewCollapsed();
                searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        categoriesButton = (Button)findViewById(R.id.categories_button);
        categoriesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showCategoriesPopUp();
            }
        });

        LinkedHashMap extras = GalePressApplication.getInstance().extrasHashMap;

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
        GalePressApplication.getInstance().setCurrentActivity(this);
        GalePressApplication.getInstance().getDataApi().getAppDetail(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GalePressApplication.getInstance().onActivityResult(requestCode,resultCode,data);
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
        super.onDestroy();
    }

    private void setTabs() {
        mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this,getSupportFragmentManager(), android.R.id.tabcontent);

        addTab(getString(R.string.LIBRARY), LIBRARY_TAB_TAG,createTabDrawable(R.drawable.library_tabbar_icon),LibraryFragment.class);
        addTab(getString(R.string.DOWNLOADED), DOWNLOADED_LIBRARY_TAG,createTabDrawable(R.drawable.downloaded_tabbar_icon),LibraryFragment.class);
        addTab(getString(R.string.INFO), INFO_TAB_TAG,createTabDrawable(R.drawable.info_tabbar_icon),InfoPageFragment.class);
    }

    private Drawable createTabDrawable(int resId) {
        Resources res = getResources();
        StateListDrawable states = new StateListDrawable();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap icon = BitmapFactory.decodeResource(res, resId, options);

        Bitmap unselected = TabBitmap.createUnselectedBitmap(res, icon);
        Bitmap selected = TabBitmap.createSelectedBitmap(res, icon);

        icon.recycle();

        states.addState(new int[] { android.R.attr.state_selected }, new BitmapDrawable(res, selected));
        states.addState(new int[] { android.R.attr.state_enabled }, new BitmapDrawable(res, unselected));

        return states;
    }

    private View createTabIndicator(String label, Drawable drawable) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, mTabHost.getTabWidget(), false);

        TextView txtTitle = (TextView) tabIndicator.findViewById(R.id.text_view_tab_title);
        txtTitle.setText(label);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) txtTitle.getLayoutParams();
        txtTitle.setLayoutParams(params);

        ImageView imgIcon = (ImageView) tabIndicator.findViewById(R.id.image_view_tab_icon);
        imgIcon.setImageDrawable(drawable);

        return tabIndicator;
    }

    private void addTab(String label, String tag, Drawable drawable,Class classy) {
        TabHost.TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setIndicator(createTabIndicator(label, drawable));
        mTabHost.addTab(spec,classy,null);
    }

    private void showCategoriesPopUp(){
        PopupMenu popupMenu = new PopupMenu(this, categoriesButton);
        List categories = GalePressApplication.getInstance().getDatabaseApi().getAllCategories();
        popupMenu.getMenu().add(CONTEXT_MENU_GROUP_ID, Menu.FIRST+1, 0, R.string.show_all);

        for(int i =0; i<categories.size(); i++){
            L_Category category = (L_Category)categories.get(i);
            popupMenu.getMenu().add(CONTEXT_MENU_GROUP_ID, Menu.FIRST+i+2, 0, category.categoryName);
        }
        popupMenu.getMenu().setGroupCheckable(CONTEXT_MENU_GROUP_ID, false, false);
        popupMenu.getMenu().setGroupEnabled(CONTEXT_MENU_GROUP_ID, true);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
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
        LibraryFragment libraryFragment = getCurrentFragment();
        libraryFragment.selectedCategory = selectedCategory;
        libraryFragment.updateGridView();

        Logout.e("Adem", "OnPopUpMenuItem Clicked:"+item.getTitle().toString());
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        HashMap extrasHashMap = GalePressApplication.getInstance().extrasHashMap;
        int i = 0;
        Iterator it = extrasHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            menu.add(0, Menu.FIRST+i, Menu.FIRST+i,pairs.getKey().toString());
            i++;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.extras, menu);
        result = super.onCreateOptionsMenu(menu);
        return result;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        HashMap extrasHashMap = GalePressApplication.getInstance().extrasHashMap;
        Map.Entry selectedMenuItemMapEntry = null;
        int i = 1;
        Iterator it = extrasHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            if(item.getItemId() == i){
                selectedMenuItemMapEntry = pairs;
                break;
            }
            i++;
        }

        if(selectedMenuItemMapEntry.getKey().toString().toLowerCase().contains("mail")){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
//            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{selectedMenuItemMapEntry.getValue().toString()});
            intent.putExtra(Intent.EXTRA_SUBJECT, " ");
            intent.putExtra(Intent.EXTRA_TEXT   , " ");
            try {
                startActivity(Intent.createChooser(intent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Intent intent = new Intent(this, ExtraWebViewActivity.class);
            intent.putExtra("url",selectedMenuItemMapEntry.getValue().toString());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);

}

    public LibraryFragment getCurrentFragment(){
        int count = getSupportFragmentManager().getFragments().size();
        for(int i=0; i< count; i++){
            LibraryFragment fragment=(LibraryFragment) getSupportFragmentManager().getFragments().get(i);
            if(mTabHost.getCurrentTabTag().compareTo(fragment.getTag())==0){
                return fragment;
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