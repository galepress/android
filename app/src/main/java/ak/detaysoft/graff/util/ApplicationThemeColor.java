package ak.detaysoft.graff.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONObject;

import ak.detaysoft.graff.GalePressApplication;
import ak.detaysoft.graff.LibraryFragment;
import ak.detaysoft.graff.MainActivity;
import ak.detaysoft.graff.R;
import ak.detaysoft.graff.custom_models.TabbarItem;

/**
 * Created by p1025 on 01.04.2015.
 */
public class ApplicationThemeColor {

    private static ApplicationThemeColor instance;

    public static int themeType = 1;
    private static String foregroundColor = "#2ca0dc";
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
    public static final int HOME_ICON = 49;
    public static final int HOME_ICON_SELECTED = 50;
    public static final int READER_MENU_OPEN = 51;
    public static final int READER_MENU_OPEN2 = 52;
    public static final int READER_MENU_OPEN_OK = 53;
    public static final int CUSTOM_PROGRESS_PULSE = 54;
    public static final int DOWNLOAD_CONTENT_FREE_ARROW = 55;
    public static final int DOWNLOAD_CONTENT_FREE = 56;
    public static final int CANCEL_CONTENT_DOWNLOAD = 57;
    public static final int DOWNLOAD_CONTENT_CLOUD = 58;
    public static final int DOWNLOAD_CONTENT_CLOUD_ARROW = 59;
    public static final int DOWNLOAD_CONTENT_PURCHASE_ARROW = 60;
    public static final int DOWNLOAD_CONTENT_BUTTON_BACKGROUND = 61;
    public static final int CROP_PAGE_SUBMIT = 62;
    public static final int CROP_PAGE_CANCEL = 63;
    public static final int MEMBERSHIP_LOGIN = 62;
    public static final int MEMBERSHIP_SUBSCRIPTION = 63;
    public static final int MEMBERSHIP_RESTORE = 64;
    public static final int MEMBERSHIP_LOGOUT = 65;
    public static final int MEMBERSHIP_POPUP_CLOSE = 66;
    public static final int MEMBERSHIP_POPUP_CLOSE_BASE = 67;
    public static final int DOWNLOAD_CONTENT_PURCHASE_BUTTON_BACKGROUND = 68;
    public static final int DOWNLOAD_CONTENT_PURCHASE_BOTTOM = 69;
    public static final int VERIFICATION_POPUP_CLOSE_BASE = 70;
    public static final int READER_SEARCH_OPEN = 71;
    public static final int READER_SEARCH_CLEAR = 72;
    public static final int SEARCH_MENU_ICON = 73;
    public static final int MENU_ICON_BACK = 74;

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
        getInstance().foregroundColor = "#2ca0dc";

        if(isColorChanged && GalePressApplication.getInstance().getCurrentActivity()!= null && GalePressApplication.getInstance().getCurrentActivity().getClass() == MainActivity.class) {
            ((MainActivity) GalePressApplication.getInstance().getCurrentActivity()).updateActivityViewAndAdapter();
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

    public int getThemeColor(){
        return Color.parseColor("#353535");
    }

    public int getLightThemeColor(){
        return Color.parseColor("#E8E8E8"); //Light Theme
    }

    public int getDarkThemeColor(){
        return Color.parseColor("#333333"); //Dark Theme
    }

    //alpha degeri 0-255 arası olmali convertIntAlphaToHex metodunda hex hesaplamasi yapiliyor
    public int getThemeColorWithAlpha(int alpha){
        return Color.parseColor(convertIntAlphaToHex(alpha)+"313131");
    }

    public int getReaderBackground(){
        return Color.parseColor("#1e1e1e");
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
        return Color.parseColor("#191919");
    }

    public int getActionAndTabBarColorWithAlpha(int alpha){
        return Color.parseColor(convertIntAlphaToHex(alpha)+"191919");
    }

    public int getCoverImageBackgroundColor(){
        return Color.parseColor("#191919");
    }

    public int getHolderDetailBackround(){
        return Color.parseColor(convertIntAlphaToHex(90)+"219ED8");
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

    public ColorStateList downloadButtonPriceColorStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors;

        if(getInstance().themeType == LIGHT_THEME_TYPE){
            colors = new int[] {
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColor()
            };
        } else {
            colors = new int[] {
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColorWithAlpha(50),
                    getGridItemNameLabelColor()
            };
        }

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    public ColorStateList defaultLightPressedDarkStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors;

        colors = new int[] {
                Color.parseColor("#333333"),
                Color.parseColor("#333333"),
                Color.parseColor("#333333"),
                Color.parseColor("#E9E9E9")

        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }


    public ColorStateList defaultLightAlphaPressedDarkStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors;

        colors = new int[] {
                Color.parseColor("#AA333333"),
                Color.parseColor("#AA333333"),
                Color.parseColor("#AA333333"),
                Color.parseColor("#AAE9E9E9")

        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    public ColorStateList defaultWhiteAlphaPressedWhiteStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors;

        colors = new int[] {
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#80FFFFFF")

        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    public ColorStateList leftmenuListViewColorStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors;

        if(getInstance().themeType == LIGHT_THEME_TYPE){
            colors = new int[] {
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#E9E9E9")
            };
        } else {
            colors = new int[] {
                    Color.parseColor("#AA333333"),
                    Color.parseColor("#AA333333"),
                    Color.parseColor("#AA333333"),
                    Color.parseColor("#333333")

            };
        }

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
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

    public int getForegroundColorWithAlpha(int alpha){
        return Color.parseColor(convertIntAlphaToHex(alpha)+foregroundColor.substring(1));
    }

    public int getWhiteColorWithAlpha(int alpha){
        return Color.parseColor(convertIntAlphaToHex(alpha)+"FFFFFF");
    }

    public int getVerficationLoginColor(){
        return Color.parseColor("#7E7E7E");
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

    public ColorFilter getThemeColorFilterWithAlpha(float alpha){
        int color = getThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = { 0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, alpha, 0 };
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

    public ColorFilter getLightThemeColorFilter(){
        int color = Color.parseColor("#E9E9E9");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getDarkThemeColorFilter(){
        int color = Color.parseColor("#333333");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrix);
    }


    public ColorFilter getDarkThemeColorFilterWithAlpha(float alpha){
        int color = Color.parseColor("#333333");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, alpha, 0 };
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getWhiteColorFilter(){
        int color = Color.WHITE;
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };
        return new ColorMatrixColorFilter(matrix);
    }


    public ColorFilter getCustomColorFilterWithAlpha(String colorHex, float alpha){
        int color = Color.parseColor(colorHex);
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, alpha, 0 };
        return new ColorMatrixColorFilter(matrix);
    }

    public Drawable paintIcons(Context context, int resourceType){
        Drawable myIcon;
        if(resourceType == WEBVIEW_BACK){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_back);
        } else if(resourceType == WEBVIEW_BACK_DISABLE){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_back_disable);
        } else if(resourceType == WEBVIEW_NEXT){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_next);
        } else if(resourceType == WEBVIEW_NEXT_DISABLE) {
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_next_disable);
        } else  if( resourceType == WEBVIEW_REFRESH) {
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_refresh);
        } else if(resourceType == WEBVIEW_REFRESH_DISABLE){
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_refresh_disable);
        } else if(resourceType == WEBVIEW_CLOSE) {
            myIcon = context.getResources().getDrawable(R.drawable.extra_web_close);
        } else if(resourceType == MENU_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.menu);
        } else if(resourceType == MENU_ICON_BACK){
            myIcon = context.getResources().getDrawable(R.drawable.menu_back);
        }  else if(resourceType == SEARCH_MENU_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.search_menu);
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
            myIcon.setColorFilter(getThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == LEFT_MENU_UP){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_up);
            myIcon.setColorFilter(getThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == LEFT_MENU_CATEGORY){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_category_icon1);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == LEFT_MENU_LINK) {
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_link);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if (resourceType == CATEGORY_SELECT){
            myIcon = context.getResources().getDrawable(R.drawable.category_select);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == SEARCH_CLEAR){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
            myIcon.setColorFilter(getWhiteColorFilter());
        } else if(resourceType == SEARCH_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
            myIcon.setColorFilter(getWhiteColorFilter());
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
        } else if(resourceType == READER_MAIL){
            myIcon = context.getResources().getDrawable(R.drawable.reader_share);
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
            myIcon.setColorFilter(getThemeColorFilterWithAlpha((float)0.5));
        } else if(resourceType == PASSIVE_SEARCH_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
            myIcon.setColorFilter(getWhiteColorFilter());
        } else if(resourceType == PASSIVE_SEARCH_CLEAR_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
            myIcon.setColorFilter(getWhiteColorFilter());
        }  else if(resourceType == DELETE_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_delete);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == UPDATE_CONTENT){
            myIcon = context.getResources().getDrawable(R.drawable.popup_update);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == HOME_ICON){
            myIcon = context.getResources().getDrawable(R.drawable.tab_home);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == HOME_ICON_SELECTED){
            myIcon = context.getResources().getDrawable(R.drawable.tab_home);
            myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float)0.5));
        } else if(resourceType == READER_SEARCH_OPEN) {
            myIcon = context.getResources().getDrawable(R.drawable.reader_search);
        } else if(resourceType == READER_SEARCH_CLEAR) {
            myIcon = context.getResources().getDrawable(R.drawable.reader_search_clear);
            myIcon.setColorFilter(getThemeColorFilter());
        } else if(resourceType == READER_MENU_OPEN){
            myIcon = context.getResources().getDrawable(R.drawable.reader_bottom_menu);
        } else if(resourceType == READER_MENU_OPEN2){
            if(themeType == DARK_THEME_TYPE) {
                myIcon = context.getResources().getDrawable(R.drawable.ks);
            } else {
                myIcon = context.getResources().getDrawable(R.drawable.kb);
            }
        } else if(resourceType == READER_MENU_OPEN_OK){
            myIcon = context.getResources().getDrawable(R.drawable.reader_bottom_ok);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == CUSTOM_PROGRESS_PULSE){
            myIcon = context.getResources().getDrawable(R.drawable.progress_icon);
            myIcon.setColorFilter(getForegroundColorFilter());
        }  else if(resourceType == CANCEL_CONTENT_DOWNLOAD){
            myIcon = context.getResources().getDrawable(R.drawable.popup_cancel);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MEMBERSHIP_LOGIN){
            myIcon = context.getResources().getDrawable(R.drawable.membership_login);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MEMBERSHIP_LOGOUT){
            myIcon = context.getResources().getDrawable(R.drawable.membership_logout);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MEMBERSHIP_RESTORE){
            myIcon = context.getResources().getDrawable(R.drawable.membership_restore);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MEMBERSHIP_SUBSCRIPTION){
            myIcon = context.getResources().getDrawable(R.drawable.membership_subscription);
            myIcon.setColorFilter(getReverseThemeColorFilter());
        } else if(resourceType == MEMBERSHIP_POPUP_CLOSE){
            myIcon = context.getResources().getDrawable(R.drawable.popup_cancel);
        } else if(resourceType == MEMBERSHIP_POPUP_CLOSE_BASE){
            myIcon = context.getResources().getDrawable(R.drawable.popup_close_base_circle);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else if(resourceType == VERIFICATION_POPUP_CLOSE_BASE){
            myIcon = context.getResources().getDrawable(R.drawable.popup_close_base_circle);
            myIcon.setColorFilter(getDarkThemeColorFilterWithAlpha((float)0.5));
        } else {
            myIcon = context.getResources().getDrawable(R.drawable.popup_cancel);
            myIcon.setColorFilter(getReverseThemeColorFilter());
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
                ((ImageView)view).setImageDrawable(new TabbarStateList(true, myIcon, mySelectedIcon));
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {}

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Drawable myIcon = new BitmapDrawable(context.getResources(),bitmap);
                Drawable mySelectedIcon = new BitmapDrawable(context.getResources(),bitmap);

                myIcon.setColorFilter(getForegroundColorFilter());
                mySelectedIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));

                ((ImageView)view).setImageDrawable(new TabbarStateList(true, myIcon, mySelectedIcon));
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

    //Viewer Login ekrani login butonu drawable
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

    //Login ekrani login butonu drawable
    public Drawable getPopupLoginButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        normal.setColor(getThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        pressed.setColor(getThemeColorWithAlpha(30));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Viewer Logout butonu background
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

    //Verification giris butonu background
    public Drawable getVerificationLoginButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(Color.parseColor("#2ca0dc"));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(Color.parseColor("#CC2ca0dc"));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Verification signup butonu background
    public Drawable getVerificationSignupButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(Color.parseColor("#39b54a"));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(Color.parseColor("#CC39b54a"));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Verification signup butonu background
    public Drawable getVerificationSubmitButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(getDarkThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(getLightThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }




    //Verification login-signup-facebook butonu background
    public Drawable getVerificationFacebookButtonDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(Color.parseColor("#2c467b"));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed =  new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(Color.parseColor("#AA2c467b"));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    //Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getVerificationLoginInputDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(Color.TRANSPARENT);
        normal.setStroke(1, getWhiteColorWithAlpha(50));

        GradientDrawable focused =  new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        focused.setColor(Color.TRANSPARENT);
        focused.setStroke(1, Color.WHITE);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_focused },
                focused);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }


    public Drawable getVerificationClose(Context context){

        Drawable normal;
        Drawable pressed;

        normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
        pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_focused },
                pressed);
        drawable.addState(new int[] { android.R.attr.state_enabled },
                normal);

        return drawable;
    }

    public Drawable getCropPageButtonDrawable(Context context, int resourceType) {
        Drawable myIcon;
        if(resourceType == CROP_PAGE_SUBMIT){
            myIcon = context.getResources().getDrawable(R.drawable.crop_submit);
        } else {
            myIcon = context.getResources().getDrawable(R.drawable.crop_cancel);
        }

        return myIcon;
    }

    public Drawable getPopupButtonDrawable(Context context, int resourceType) {

        Drawable normal;
        Drawable pressed;

        if(resourceType == MEMBERSHIP_POPUP_CLOSE){
            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);
                pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
            } else {
                normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
                pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);
            }
        } else if(resourceType == READ_CONTENT){

            normal = context.getResources().getDrawable(R.drawable.popup_read_default);
            pressed = context.getResources().getDrawable(R.drawable.popup_read_pressed);
        } else if(resourceType == DELETE_CONTENT){

            normal = context.getResources().getDrawable(R.drawable.popup_delete_default);
            pressed = context.getResources().getDrawable(R.drawable.popup_delete_pressed);
        } else if(resourceType == UPDATE_CONTENT){

            normal = context.getResources().getDrawable(R.drawable.popup_update_default);
            pressed = context.getResources().getDrawable(R.drawable.popup_update_pressed);
        } else if(resourceType == CANCEL_CONTENT_DOWNLOAD){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_cancel_dark);
                pressed = context.getResources().getDrawable(R.drawable.popup_cancel_light);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_cancel_light);
                pressed = context.getResources().getDrawable(R.drawable.popup_cancel_dark);
            }

        } else if(resourceType == DOWNLOAD_CONTENT_FREE_ARROW){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_free_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_free_arrow);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_free_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_free_arrow);
            }
        } else if(resourceType == DOWNLOAD_CONTENT_FREE){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_free);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_free);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_free);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_free);
            }

        } else if(resourceType == DOWNLOAD_CONTENT_CLOUD){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_cloud);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_cloud);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud);
            }
        } else if(resourceType == DOWNLOAD_CONTENT_CLOUD_ARROW){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow);
            }
        } else if(resourceType == DOWNLOAD_CONTENT_PURCHASE_ARROW){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_arrow);

            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_arrow);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_arrow);
            }
        } else if(resourceType == DOWNLOAD_CONTENT_PURCHASE_BOTTOM){

            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_bottom);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_bottom);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_bottom);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_bottom);

            }
        } else if(resourceType == DOWNLOAD_CONTENT_BUTTON_BACKGROUND){
            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_download_bg);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_bg);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_download_bg);
                pressed = context.getResources().getDrawable(R.drawable.popup_download_bg);
            }
        } else {
            if(getInstance().themeType == DARK_THEME_TYPE){
                normal = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg);
                pressed = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg);
            } else {
                normal = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg);
                pressed = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg);
            }
        }

        return new PopupButtonStateList(normal, pressed);
    }

    public Drawable getLeftMenuIconDrawable(Context context, int resourceType) {

        Drawable normal;
        Drawable pressed;

        if(resourceType == FACEBOOK_ICON){
            normal = context.getResources().getDrawable(R.drawable.facebook);
            pressed = context.getResources().getDrawable(R.drawable.facebook);
        }else if(resourceType == TWITTER_ICON){
            normal = context.getResources().getDrawable(R.drawable.twitter);
            pressed = context.getResources().getDrawable(R.drawable.twitter);
        } else if(resourceType == INSTAGRAM_ICON){

            normal = context.getResources().getDrawable(R.drawable.instagram);
            pressed = context.getResources().getDrawable(R.drawable.instagram);
        } else if(resourceType == LINKEDIN_ICON){

            normal = context.getResources().getDrawable(R.drawable.linkedin);
            pressed = context.getResources().getDrawable(R.drawable.linkedin);
        } else if(resourceType == WEB_ICON){

            normal = context.getResources().getDrawable(R.drawable.web);
            pressed = context.getResources().getDrawable(R.drawable.web);
        } else if(resourceType == MAIL_ICON){

            normal = context.getResources().getDrawable(R.drawable.mail);
            pressed = context.getResources().getDrawable(R.drawable.mail);

        } else if(resourceType == GOOGLE_PLUS_ICON){

            normal = context.getResources().getDrawable(R.drawable.google_plus);
            pressed = context.getResources().getDrawable(R.drawable.google_plus);
        } else if(resourceType == PINTEREST_ICON){

            normal = context.getResources().getDrawable(R.drawable.pinterest);
            pressed = context.getResources().getDrawable(R.drawable.pinterest);

        } else if(resourceType == TUMBLR_ICON){

            normal = context.getResources().getDrawable(R.drawable.tumblr);
            pressed = context.getResources().getDrawable(R.drawable.tumblr);
        } else if(resourceType == YOUTUBE_ICON){

            normal = context.getResources().getDrawable(R.drawable.youtube);
            pressed = context.getResources().getDrawable(R.drawable.youtube);
        } else if(resourceType == MEMBERSHIP_LOGIN){

            normal = context.getResources().getDrawable(R.drawable.membership_login);
            pressed = context.getResources().getDrawable(R.drawable.membership_login);
        }else if(resourceType == MEMBERSHIP_LOGIN){

            normal = context.getResources().getDrawable(R.drawable.membership_restore);
            pressed = context.getResources().getDrawable(R.drawable.membership_restore);
        }else if(resourceType == MEMBERSHIP_LOGIN){

            normal = context.getResources().getDrawable(R.drawable.membership_subscription);
            pressed = context.getResources().getDrawable(R.drawable.membership_subscription);
        } else { //membership logout
            normal = context.getResources().getDrawable(R.drawable.membership_logout);
            pressed = context.getResources().getDrawable(R.drawable.membership_logout);
        }

        normal.setColorFilter(getThemeColorFilter());
        pressed.setColorFilter(getThemeColorFilterWithAlpha((float) 0.5));

        return new LeftMenuStateList(normal, pressed);
    }

    //Viewer Login ekraninda kullanici adi sifre girilen edittextlerin background
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

    //Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getPopupLoginInputDrawable(Context context) {
        GradientDrawable normal =  new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        normal.setColor(getThemeColorWithAlpha(10));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable focused =  new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
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
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.search_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(1, Color.WHITE);
        return drawable;
    }

    //Sol menu search passive oldugunda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getPassiveSearchViewDrawable(Context context) {
        GradientDrawable drawable =  new GradientDrawable();
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.search_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(1, Color.WHITE);
        return drawable;
    }


    public int getReaderSearchResultTextColor(){
        if(themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#E9E9E9");
        } else {
            return Color.parseColor("#333333");
        }
    }


    public int getReaderPopupColor(){
        if(getInstance().themeType == DARK_THEME_TYPE){
            return Color.parseColor("#333333"); //Dark Theme
        } else {
            return Color.parseColor("#E9E9E9"); //Light Theme
        }
    }

    //reader menu search tiklandiginda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getReaderSearchViewDrawable(Context context) {
        GradientDrawable drawable =  new GradientDrawable();
        drawable.setCornerRadius(10);
        if(themeType == DARK_THEME_TYPE) {
            drawable.setColor(Color.parseColor("#424242"));
        } else {
            drawable.setColor(Color.parseColor("#767676"));
        }
        drawable.setStroke(0, getThemeColor());
        return drawable;
    }

    public int getGridItemNameLabelColor() {
        return Color.parseColor("#FFFFFF");
    }

    public int getGridItemNameLabelColorWithAlpha(int alpha) {
        return Color.parseColor(convertIntAlphaToHex(alpha)+"219ed8");
    }

    public int getGridItemDetailLabelColor() {
        return Color.WHITE;
    }


    public DisplayMetrics getScreenSizes(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /*
    * (MG)
    * Try catch koymamım sebebi lollipop versiyonunda font bulunamadı hatası almamız
    * https://fabric.io/galepress/android/apps/ak.detaysoft.ekinyayincilikdis/issues/56e18957ffcdc04250b80d10
    * */
    public Typeface getFont(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/Avenir-Light.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getMediumFont(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/Avenir-Medium.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getOpenSansLight(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getOpenSansRegular(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getOpenSansBold(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Bold.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamBook(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-Book.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamLight(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-Light.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamMedium(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-Medium.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamBookItalic(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-BookItalic.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamLightItalic(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-LightItalic.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getGothamMediumItalic(Context context){
        try{
            return Typeface.createFromAsset(context.getAssets(), "fonts/GothamRounded-MediumItalic.otf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }
}
