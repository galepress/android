package ak.detaysoft.galepress.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONObject;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.LibraryFragment;
import ak.detaysoft.galepress.MainActivity;
import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.StateListDrawableForPopupButtons;
import ak.detaysoft.galepress.StateListDrawableWithColorFilter;
import ak.detaysoft.galepress.custom_models.TabbarItem;

/**
 * Created by p1025 on 01.04.2015.
 */
public class ApplicationThemeColor {

    private static ApplicationThemeColor instance;

    private static int themeType = 1;
    private static String foregroundColor = "#2980B9";

    public static final int DARK_THEME_TYPE = 1;
    public static final int LIGHT_THEME_TYPE = 2;
    public static final int LIBRARY_ICON = 0;
    public static final int DOWNLOAD_ICON = 1;
    public static final int INFO_ICON = 2;
    public static final int MENU_ICON = 3;
    public static final int LEFT_MENU_DOWN = 4;
    public static final int LEFT_MENU_UP = 5;
    public static final int LEFT_MENU_CATEGORY = 6;
    public static final int LEFT_MENU_LINK = 7;
    public static final int CATEGORY_SELECT = 8;
    public static final int SEARCH_ICON = 9;
    public static final int SEARCH_CLEAR = 10;
    public static final int DOWNLOAD_ICON_SELECTED = 11;
    public static final int INFO_ICON_SELECTED = 12;
    public static final int LIBRARY_ICON_SELECTED = 13;
    public static final int FACEBOOK_ICON = 14;
    public static final int TWITTER_ICON = 15;
    public static final int INSTAGRAM_ICON = 16;
    public static final int WEB_ICON = 17;
    public static final int LINKEDIN_ICON = 18;
    public static final int MAIL_ICON = 19;
    public static final int READER_MENU = 20;
    public static final int READER_MAIL = 21;
    public static final int WEBVIEW_BACK = 22;
    public static final int WEBVIEW_NEXT = 23;
    public static final int WEBVIEW_CLOSE = 24;
    public static final int WEBVIEW_REFRESH = 25;
    public static final int WEBVIEW_BACK_DISABLE = 26;
    public static final int WEBVIEW_NEXT_DISABLE = 27;
    public static final int WEBVIEW_REFRESH_DISABLE = 28;
    public static final int GOOGLE_PLUS_ICON = 29;
    public static final int PINTEREST_ICON = 30;
    public static final int TUMBLR_ICON = 31;
    public static final int YOUTUBE_ICON = 32;
    public static final int MENU_SELECTED = 33;
    public static final int READER_UCGEN = 34;
    public static final int SETTING_POPUP_ARROW = 35;
    public static final int VIEWER_LOGIN_LOGO = 36;
    public static final int INTERNET_CONNECTION_ERROR = 37;
    public static final int VIEWER_USERNAME_ACTIVE_INPUT_ICON = 38;
    public static final int VIEWER_PASSWORD_ACTIVE_INPUT_ICON = 39;
    public static final int VIEWER_USERNAME_PASSIVE_INPUT_ICON = 40;
    public static final int VIEWER_PASSWORD_PASSIVE_INPUT_ICON = 41;
    public static final int CATEGORY_UNSELECT = 42;
    public static final int PASSIVE_SEARCH_ICON = 43;
    public static final int PASSIVE_SEARCH_CLEAR_ICON = 44;
    public static final int READ_CONTENT = 45;
    public static final int DELETE_CONTENT = 46;
    public static final int UPDATE_CONTENT = 47;
    public static final int DOWNLOAD_CONTENT = 48;
    public static final int HOME_ICON = 49;
    public static final int HOME_ICON_SELECTED = 50;

    //Servisten gelen iconlar icin
    public static final int CUSTOM_TAB_ICON = 101;
    public static final int CUSTOM_TAB_ICON_SELECTED = 102;

    public ApplicationThemeColor(){

    }

    public void setParameters(JSONObject response) {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
        editor = preferences.edit();
        String foreGround;
        String backGround;
        try{
            foreGround =  response.getString("ThemeForeground");
            backGround =  response.getString("ThemeBackground");
        } catch (Exception e){
            backGround = preferences.getString("ThemeBackground","1");
            foreGround = preferences.getString("ThemeForeground","#2980B9");
        }
        editor.putString("ThemeBackground", backGround);
        editor.putString("ThemeForeground", foreGround);
        editor.commit();

        boolean isColorChanged = false;
        if(getInstance().themeType != Integer.parseInt(backGround) || getInstance().foregroundColor.compareTo(foreGround) != 0)
            isColorChanged = true;

        getInstance().themeType = Integer.parseInt(backGround);
        getInstance().foregroundColor = foreGround;

        if(isColorChanged && GalePressApplication.getInstance().getCurrentActivity()!= null && GalePressApplication.getInstance().getCurrentActivity().getClass() == MainActivity.class) {
            ((MainActivity) GalePressApplication.getInstance().getCurrentActivity()).invalidateActivityViewAndAdapter(true);
            if(GalePressApplication.getInstance().getCurrentFragment() != null)
                ((LibraryFragment)GalePressApplication.getInstance().getCurrentFragment()).gridview.setBackgroundColor(getThemeColor());
        }
    }

    public static ApplicationThemeColor getInstance(){
        if(instance == null){
            instance = new ApplicationThemeColor();
        }
        return instance;
    }

    public int getThemeType(){
        return themeType;
    }


    public int getThemeColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#333333"); //Dark Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Light Theme
        }
    }

    //alpha degeri 0-255 arası olmali convertIntAlphaToHex metodunda hex hesaplamasi yapiliyor
    public int getThemeColorWithAlpha(int alpha){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor(convertIntAlphaToHex(alpha)+"333333"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha)+"E8E8E8"); //Light Theme
        }
    }

    public int getReverseThemeColor(){
        if(getInstance().themeType == LIGHT_THEME_TYPE){
            return Color.parseColor("#333333"); //Light Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Dark Theme
        }
    }

    public int getLibraryItemTextColor(){
        if(getInstance().themeType == LIGHT_THEME_TYPE){
            return Color.parseColor("#404040"); //Light Theme
        } else {
            return Color.parseColor("#DBDBDB"); //Dark Theme
        }
    }

    //alpha degeri 0-100 arası olmali convertIntAlphaToHex metodunda hex hesaplamasi yapiliyor
    public int getReverseThemeColorWithAlpha(int alpha){
        if(getInstance().themeType == LIGHT_THEME_TYPE){
            return Color.parseColor(convertIntAlphaToHex(alpha)+"333333"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha)+"E8E8E8"); //Light Theme
        }
    }

    public String convertIntAlphaToHex(int alpha){

        String hex = Integer.toHexString((alpha*255)/100);
        if(hex.length() == 1)
            hex = "0"+hex;
        if(hex.length() == 0)
            hex = "FF";

        return "#"+hex;
    }

    public int getDarkestThemeColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#2B2B2B"); //Dark Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Light Theme
        }
    }

    public int getTransperentThemeColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#AA333333"); //Dark Theme
        } else {
            return Color.parseColor("#DDE8E8E8"); //Light Theme
        }

    }

    public int getTransperentPopupColor(){
        return Color.parseColor("#AA000000");

    }

    public int getMenuShadowColor(){
        return Color.parseColor("#AA000000");

    }

    public int getActionAndTabBarColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#282828"); //Light Theme
        } else {
            return Color.parseColor("#F7F7F7"); //Dark Theme
        }
    }

    public int getActionAndTabBarColorWithAlpha(int alpha){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor(convertIntAlphaToHex(alpha)+"282828"); //Light Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha)+"F7F7F7"); //Dark Theme
        }
    }

    public int getCoverImageBackgroundColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#575757"); //Light Theme
        } else {
            return Color.parseColor("#FFFFFF"); //Dark Theme
        }
    }

    public int getLightCoverImageBackgroundColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#787878"); //Light Theme
        } else {
            return Color.parseColor("#FFFFFF"); //Dark Theme
        }
    }

    public int getPopupTextColor(){
        if(getInstance().themeType == LIGHT_THEME_TYPE){
            return Color.parseColor("#333333"); //Light Theme
        } else {
            return Color.parseColor("#E9E9E9"); //Dark Theme
        }
    }

    //icerikler indirilirken cikan progresslerin background color
    public int getProgressbarBackgroundColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#272A33"); //Dark Theme
        } else {
            return Color.parseColor("#CACACA"); //Light Theme
        }
    }

    public int getDisableButtonColor(){
        return Color.parseColor("#939393");
    }

    public int getForegroundColor(){
        return Color.parseColor(foregroundColor);
    }

    public ColorFilter getTransparentForegroundColorFilter(){
        int color = getThemeTranperentForegroundColor();
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                         , 0, 0, 0, 0, green
                         , 0, 0, 0, 0, blue
                         , 0, 0, 0, (float)0.9, 0 };
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getForegroundColorFilter(){
        int color = getForegroundColor();
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                         , 0, 0, 0, 0, green
                         , 0, 0, 0, 0, blue
                         , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getThemeColorFilter(){
        int color = getThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = { 0, 0, 0, 0, redTheme
                              , 0, 0, 0, 0, greenTheme
                              , 0, 0, 0, 0, blueTheme
                              , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getForeGroundColorFilterWithAlpha(float alpha){
        int color = getForegroundColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = { 0, 0, 0, 0, redTheme
                              , 0, 0, 0, 0, greenTheme
                              , 0, 0, 0, 0, blueTheme
                              , 0, 0, 0, alpha, 0 };
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getReverseThemeColorFilterWithAlpha(float alpha){
        int color = getReverseThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = { 0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, alpha, 0 };
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getReverseThemeColorFilter(){
        int color = getReverseThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = { 0, 0, 0, 0, redTheme
                              , 0, 0, 0, 0, greenTheme
                              , 0, 0, 0, 0, blueTheme
                              , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getUnselectedColorFilter(){
        int color = getDisableButtonColor();
        int redUnselected = (color & 0xFF0000) / 0xFFFF;
        int greenUnselected = (color & 0xFF00) / 0xFF;
        int blueUnselected = color & 0xFF;
        float[] matrixUnselected = { 0, 0, 0, 0, redUnselected
                , 0, 0, 0, 0, greenUnselected
                , 0, 0, 0, 0, blueUnselected
                , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrixUnselected);
    }

    public Drawable paintIcons(Context context, int resourceType){
        Drawable myIcon;
        if(resourceType == WEBVIEW_BACK){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_back);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == WEBVIEW_BACK_DISABLE){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_back);
            myIcon.setColorFilter(getUnselectedColorFilter());
        } else if(resourceType == WEBVIEW_NEXT){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_next);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == WEBVIEW_NEXT_DISABLE) {
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_next);
            myIcon.setColorFilter(getUnselectedColorFilter());
        } else  if( resourceType == WEBVIEW_REFRESH) {
            myIcon = context.getResources().getDrawable(R.drawable.web_refresh);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == WEBVIEW_REFRESH_DISABLE){
            myIcon = context.getResources().getDrawable(R.drawable.web_refresh);
            myIcon.setColorFilter(getUnselectedColorFilter());
        } else if(resourceType == WEBVIEW_CLOSE) {
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_close);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == MENU_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.menu);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == LIBRARY_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.tab_library);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == DOWNLOAD_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.tab_download);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == INFO_ICON) {
            myIcon = context.getResources().getDrawable(R.drawable.tab_info);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == LEFT_MENU_DOWN){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_down);
            myIcon.setColorFilter(getReverseThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == LEFT_MENU_UP){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_up);
            myIcon.setColorFilter(getReverseThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == LEFT_MENU_CATEGORY){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_category_icon1);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == LEFT_MENU_LINK) {
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_link);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if (resourceType == CATEGORY_SELECT){
            myIcon = context.getResources().getDrawable(R.drawable.category_select);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == SEARCH_CLEAR){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == SEARCH_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == LIBRARY_ICON_SELECTED) {
            myIcon = context.getResources().getDrawable(R.drawable.tab_library);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
        } else if(resourceType == DOWNLOAD_ICON_SELECTED) {
            myIcon = context.getResources().getDrawable(R.drawable.tab_download);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.5));
        } else if(resourceType == INFO_ICON_SELECTED){
            myIcon = context.getResources().getDrawable(R.drawable.tab_info);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.5));
        } else if(resourceType == FACEBOOK_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.facebook);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == TWITTER_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.twitter);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == INSTAGRAM_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.instagram);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == LINKEDIN_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.linkedin);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == WEB_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.web);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MAIL_ICON) {
            myIcon = context.getResources().getDrawable(R.drawable.mail);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == READER_MENU) {
            myIcon = context.getResources().getDrawable(R.drawable.table_of_contents);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == READER_MAIL){
            myIcon = context.getResources().getDrawable(R.drawable.reader_share);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == GOOGLE_PLUS_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.google_plus);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == PINTEREST_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.pinterest);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == TUMBLR_ICON) {
            myIcon = context.getResources().getDrawable(R.drawable.tumblr);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == YOUTUBE_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.youtube);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MENU_SELECTED){
            myIcon = context.getResources().getDrawable(R.drawable.menu);
            myIcon.setColorFilter(getUnselectedColorFilter());
        } else if(resourceType == READER_UCGEN){
            myIcon = context.getResources().getDrawable(R.drawable.reader_ucgen);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.9));
        } else if(resourceType == SETTING_POPUP_ARROW){
            myIcon = context.getResources().getDrawable(R.drawable.setting_popup_arrowup);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == VIEWER_LOGIN_LOGO){
            myIcon = context.getResources().getDrawable(R.drawable.viewer_logo);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == INTERNET_CONNECTION_ERROR){
            myIcon = context.getResources().getDrawable(R.drawable.no_connection);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == VIEWER_USERNAME_ACTIVE_INPUT_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.login_username);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == VIEWER_USERNAME_PASSIVE_INPUT_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.login_username);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == VIEWER_PASSWORD_ACTIVE_INPUT_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.login_password);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == VIEWER_PASSWORD_PASSIVE_INPUT_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.login_password);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == CATEGORY_UNSELECT){
            myIcon = context.getResources().getDrawable(R.drawable.category_unselect);
            myIcon.setColorFilter(getReverseThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == PASSIVE_SEARCH_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
            myIcon.setColorFilter(getReverseThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == PASSIVE_SEARCH_CLEAR_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
            myIcon.setColorFilter(getReverseThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == READ_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_read);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == DELETE_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_delete);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == UPDATE_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_update);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == DOWNLOAD_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_download);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == HOME_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.tab_home);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else {
            myIcon = context.getResources().getDrawable(R.drawable.tab_home);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.5));
        }
        return myIcon;
    }

    //Custom tabbarlarda url seklinde gelen ikonlarin foreground rengine gore boyandigi metod
    public void paintRemoteIcon(final Context context, final TabbarItem item, final ImageView image){

        DisplayImageOptions displayConfig = new DisplayImageOptions.Builder()
                .cacheInMemory(true).build();
        ImageLoader.getInstance().displayImage(item.getIconUrl(), image, displayConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                Drawable myIcon = context.getResources().getDrawable(R.drawable.download_error_icon);
                myIcon.setColorFilter(getForegroundColorFilter());
                Drawable mySelectedIcon = context.getResources().getDrawable(R.drawable.download_error_icon);
                mySelectedIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.5));
                ((ImageView)view).setImageDrawable(new StateListDrawableWithColorFilter(true, myIcon, mySelectedIcon));
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {}

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Drawable myIcon = new BitmapDrawable(context.getResources(),bitmap);
                Drawable mySelectedIcon = new BitmapDrawable(context.getResources(),bitmap);

                myIcon.setColorFilter(getForegroundColorFilter());
                mySelectedIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));

                ((ImageView)view).setImageDrawable(new StateListDrawableWithColorFilter(true, myIcon, mySelectedIcon));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public int getThemeTranperentForegroundColor(){
        return Color.parseColor("#CC" + foregroundColor.substring(1));
    }

    public String getForegroundHexColor(){
        return foregroundColor;
    }

    //Popup ekraninda cikan indir sil guncelle oku butonlarinin drawable
    public Drawable getContentActionButtonsDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(7);
        normal.setColor(Color.TRANSPARENT);
        if(context.getResources().getDisplayMetrics().density >= (float)2.0)
            normal.setStroke(3, getInstance().getForegroundColor());
        else
            normal.setStroke(2, getInstance().getForegroundColor());

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(7);
        pressed.setColor(Color.TRANSPARENT);
        if(context.getResources().getDisplayMetrics().density >= (float)2.0)
            pressed.setStroke(3, getInstance().getThemeTranperentForegroundColor());
        else
            pressed.setStroke(2, getInstance().getThemeTranperentForegroundColor());

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Login ekrani login butonu drawable
    public Drawable getLoginButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        pressed.setColor(getThemeColorWithAlpha(30));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Logout butonu background
    public Drawable getLogoutButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getReverseThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        pressed.setColor(getTransperentThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    public Drawable getPopupButtonDrawable(Context context, int resourceType) {

        Drawable normal;
        Drawable pressed;

        if(resourceType == READ_CONTENT){
            normal = context.getResources().getDrawable(R.drawable.popup_read);
            normal.setColorFilter(getReverseThemeColorFilter());

            pressed = context.getResources().getDrawable(R.drawable.popup_read);
            pressed.setColorFilter(getThemeColorFilter());
        } else if(resourceType == DELETE_CONTENT){
            normal = context.getResources().getDrawable(R.drawable.popup_delete);
            normal.setColorFilter(getReverseThemeColorFilter());

            pressed = context.getResources().getDrawable(R.drawable.popup_delete);
            pressed.setColorFilter(getThemeColorFilter());
        } else if(resourceType == UPDATE_CONTENT){
            normal = context.getResources().getDrawable(R.drawable.popup_update);
            normal.setColorFilter(getReverseThemeColorFilter());

            pressed = context.getResources().getDrawable(R.drawable.popup_update);
            pressed.setColorFilter(getThemeColorFilter());
        } else {
            normal = context.getResources().getDrawable(R.drawable.popup_download);
            normal.setColorFilter(getReverseThemeColorFilter());

            pressed = context.getResources().getDrawable(R.drawable.popup_download);
            pressed.setColorFilter(getThemeColorFilter());
        }

        return new StateListDrawableForPopupButtons(normal, pressed);
    }

    //Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getLoginInputDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getThemeColorWithAlpha(10));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable focused =  new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        focused.setColor(getThemeColorWithAlpha(30));
        focused.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_focused },
                focused);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Sol menu search tiklandiginda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getActiveSearchViewDrawable(Context context) {
        GradientDrawable drawable =  new GradientDrawable();
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(1, getReverseThemeColor());
        return drawable;
    }

    //Sol menu search passive oldugunda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getPassiveSearchViewDrawable(Context context) {
        GradientDrawable drawable =  new GradientDrawable();
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(1, getReverseThemeColorWithAlpha(50));
        return drawable;
    }

    public Typeface getFont(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Avenir-Light.otf");
    }

    public Typeface getMediumFont(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Avenir-Medium.otf");
    }

    public Typeface getOpenSansLight(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");
    }

    public Typeface getOpenSansRegular(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
    }
}
