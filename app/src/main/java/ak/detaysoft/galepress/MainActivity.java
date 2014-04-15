package ak.detaysoft.galepress;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
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
import android.widget.ToggleButton;

import java.util.List;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.extras, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_extras_web:
                Logout.e("Adem", "web clicked");
                return true;
            case R.id.action_extras_facebook:
                Logout.e("Adem", "facebook clicked");
                return true;
            case R.id.action_extras_twitter:
                Logout.e("Adem", "twitter clicked");
                item.setChecked(true);
                openOptionsMenu();
                return true;
            case R.id.action_extras_email:
                Logout.e("Adem", "Email clicked");
                item.setChecked(true);
                return true;
            default:
                Logout.e("Adem","Default Runned");
                return super.onOptionsItemSelected(item);
        }
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
}