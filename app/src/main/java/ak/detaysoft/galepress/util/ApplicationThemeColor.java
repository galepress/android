package ak.detaysoft.galepress.util;

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
import ak.detaysoft.galepress.custom_models.TabbarItem;

/**
 * Created by p1025 on 01.04.2015.
 */
public class ApplicationThemeColor {

    private static ApplicationThemeColor instance;

    public static int themeType = 1;
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
    public static final int READER_MENU_OPEN1 = 51;
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
    public static final int BEHANCE_ICON = 73;
    public static final int FLICKER_ICON = 74;
    public static final int FOURSQUARE_ICON = 75;
    public static final int SWARM_ICON = 76;
    public static final int SEARCH_MENU_ICON = 77;
    public static final int POPUP_DESCRIPTION_OPEN = 78;
    public static final int POPUP_DESCRIPTION_CLOSE = 79;
    public static final int MENU_INFO = 80;

    public ApplicationThemeColor() {

    }

    public void setParameters(JSONObject response) {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
        editor = preferences.edit();
        String foreGround;
        String backGround;
        try {
            foreGround = response.getString("ThemeForeground");
            backGround = response.getString("ThemeBackground");
        } catch (Exception e) {
            backGround = preferences.getString("ThemeBackground", "1");
            foreGround = preferences.getString("ThemeForeground", "#2980B9");
        }
        editor.putString("ThemeBackground", backGround);
        editor.putString("ThemeForeground", foreGround);
        editor.commit();

        boolean isColorChanged = false;
        if (getInstance().themeType != Integer.parseInt(backGround) || getInstance().foregroundColor.compareTo(foreGround) != 0)
            isColorChanged = true;

        getInstance().themeType = Integer.parseInt(backGround);
        getInstance().foregroundColor = foreGround;

        if (isColorChanged && GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass() == MainActivity.class) {
            ((MainActivity) GalePressApplication.getInstance().getCurrentActivity()).updateActivityViewAndAdapter(true);
            if (GalePressApplication.getInstance().getCurrentFragment() != null)
                ((LibraryFragment) GalePressApplication.getInstance().getCurrentFragment()).gridview.setBackgroundColor(getThemeColor());
        }
    }

    public static ApplicationThemeColor getInstance() {
        if (instance == null) {
            instance = new ApplicationThemeColor();
        }
        return instance;
    }

    public int getThemeColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#333333"); //Dark Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Light Theme
        }
    }


    public int getLibraryGridViewColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#313131"); //Dark Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Light Theme
        }
    }


    public int getLightThemeColor() {
        return Color.parseColor("#E8E8E8"); //Light Theme
    }

    public int getDarkThemeColor() {
        return Color.parseColor("#333333"); //Dark Theme
    }


    public int getThemeColorWithAlpha(int alpha) {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "333333"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "E8E8E8"); //Light Theme
        }
    }

    public int getReverseThemeColor() {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor("#333333"); //Light Theme
        } else {
            return Color.parseColor("#E8E8E8"); //Dark Theme
        }
    }

    public int getLibraryItemTextColor() {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor("#404040"); //Light Theme
        } else {
            return Color.parseColor("#EAEAEA"); //Dark Theme
        }
    }

    public int getLibraryItemTextColorWithAlpha(int alpha) {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "404040"); //Light Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "EAEAEA"); //Dark Theme
        }
    }

    //alpha degeri 0-100 arası olmali convertIntAlphaToHex metodunda hex hesaplamasi yapiliyor
    public int getReverseThemeColorWithAlpha(int alpha) {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "333333"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "E8E8E8"); //Light Theme
        }
    }

    public String convertIntAlphaToHex(int alpha) {

        String hex = Integer.toHexString((alpha * 255) / 100);
        if (hex.length() == 1)
            hex = "0" + hex;
        if (hex.length() == 0)
            hex = "FF";

        return "#" + hex;
    }

    public int getTransperentThemeColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#AA333333"); //Dark Theme
        } else {
            return Color.parseColor("#DDE8E8E8"); //Light Theme
        }

    }

    public int getTransperentPopupColor() {
        return Color.parseColor("#AA000000");

    }

    public int getMenuShadowColor() {
        return Color.parseColor("#AA000000");

    }

    public int getActionAndTabBarColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#282828"); //Dark Theme
        } else {
            return Color.parseColor("#F7F7F7"); //Light Theme
        }
    }

    public int getActionAndTabBarColorWithAlpha(int alpha) {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "282828"); //Light Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "F7F7F7"); //Dark Theme
        }
    }

    public int getCoverImageBackgroundColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#7c7c7c"); //Dark Theme
        } else {
            return Color.parseColor("#FFFFFF"); //Light Theme
        }
    }

    public int getLightCoverImageBackgroundColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#787878"); //Light Theme
        } else {
            return Color.parseColor("#FFFFFF"); //Dark Theme
        }
    }

    public int getPopupTextColor() {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor("#333333"); //Light Theme
        } else {
            return Color.parseColor("#E9E9E9"); //Dark Theme
        }
    }

    public int getPopupTextColorWithAlpha(int alpha) {
        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "333333"); //Light Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "E9E9E9"); //Dark Theme
        }
    }

    public ColorStateList downloadButtonPriceColorStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors;

        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            colors = new int[]{
                    Color.parseColor("#E9E9E9"),
                    Color.parseColor("#E9E9E9"),
                    Color.parseColor("#E9E9E9"),
                    Color.parseColor("#333333")
            };
        } else {
            colors = new int[]{
                    Color.parseColor("#333333"),
                    Color.parseColor("#333333"),
                    Color.parseColor("#333333"),
                    Color.parseColor("#E9E9E9")
            };
        }

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    public ColorStateList defaultLightPressedDarkStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors;

        colors = new int[]{
                Color.parseColor("#333333"),
                Color.parseColor("#333333"),
                Color.parseColor("#333333"),
                Color.parseColor("#E9E9E9")

        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }


    public ColorStateList defaultLightAlphaPressedDarkStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors;

        colors = new int[]{
                Color.parseColor("#AA333333"),
                Color.parseColor("#AA333333"),
                Color.parseColor("#AA333333"),
                Color.parseColor("#AAE9E9E9")

        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    public ColorStateList leftmenuListViewColorStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors;

        if (getInstance().themeType == LIGHT_THEME_TYPE) {
            colors = new int[]{
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#AAE9E9E9"),
                    Color.parseColor("#E9E9E9")
            };
        } else {
            colors = new int[]{
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
    public int getProgressbarBackgroundColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#272A33"); //Dark Theme
        } else {
            return Color.parseColor("#CACACA"); //Light Theme
        }
    }

    public int getDisableButtonColor() {
        return Color.parseColor("#939393");
    }

    public int getForegroundColor() {
        return Color.parseColor(foregroundColor);
    }

    public int getForegroundColorWithAlpha(int alpha) {
        return Color.parseColor(convertIntAlphaToHex(alpha) + foregroundColor.substring(1));
    }

    public int getVerficationLoginColor() {
        return Color.parseColor("#7E7E7E");
    }

    public ColorFilter getForegroundColorFilter() {
        int color = getForegroundColor();
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getThemeColorFilter() {
        int color = getThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = {0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getThemeColorFilterWithAlpha(float alpha) {
        int color = getThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = {0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, alpha, 0};
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getForeGroundColorFilterWithAlpha(float alpha) {
        int color = getForegroundColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = {0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, alpha, 0};
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getReverseThemeColorFilterWithAlpha(float alpha) {
        int color = getReverseThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = {0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, alpha, 0};
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getReverseThemeColorFilter() {
        int color = getReverseThemeColor();
        int redTheme = (color & 0xFF0000) / 0xFFFF;
        int greenTheme = (color & 0xFF00) / 0xFF;
        int blueTheme = color & 0xFF;
        float[] matrixTheme = {0, 0, 0, 0, redTheme
                , 0, 0, 0, 0, greenTheme
                , 0, 0, 0, 0, blueTheme
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrixTheme);
    }

    public ColorFilter getUnselectedColorFilter() {
        int color = getDisableButtonColor();
        int redUnselected = (color & 0xFF0000) / 0xFFFF;
        int greenUnselected = (color & 0xFF00) / 0xFF;
        int blueUnselected = color & 0xFF;
        float[] matrixUnselected = {0, 0, 0, 0, redUnselected
                , 0, 0, 0, 0, greenUnselected
                , 0, 0, 0, 0, blueUnselected
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrixUnselected);
    }

    public ColorFilter getLightThemeColorFilter() {
        int color = Color.parseColor("#E9E9E9");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrix);
    }

    public ColorFilter getDarkThemeColorFilter() {
        int color = Color.parseColor("#333333");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrix);
    }


    public ColorFilter getReaderSearchColorFilter() {
        int color;
        if (getInstance().themeType == DARK_THEME_TYPE) {
            color = Color.parseColor("#444444");
        } else {
            color = Color.parseColor("#C2C2C2");
        }
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(matrix);
    }


    public ColorFilter getDarkThemeColorFilterWithAlpha(float alpha) {
        int color = Color.parseColor("#333333");
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;
        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, alpha, 0};
        return new ColorMatrixColorFilter(matrix);
    }

    public Drawable paintIcons(Context context, int resourceType) {
        Drawable myIcon;

        switch (resourceType) {
            case WEBVIEW_BACK:
                myIcon = context.getResources().getDrawable(R.drawable.extra_web_back);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case WEBVIEW_BACK_DISABLE:
                myIcon = context.getResources().getDrawable(R.drawable.extra_web_back);
                myIcon.setColorFilter(getUnselectedColorFilter());
                break;

            case WEBVIEW_NEXT:
                myIcon = context.getResources().getDrawable(R.drawable.extra_web_next);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case WEBVIEW_NEXT_DISABLE:
                myIcon = context.getResources().getDrawable(R.drawable.extra_web_next);
                myIcon.setColorFilter(getUnselectedColorFilter());
                break;

            case WEBVIEW_REFRESH:
                myIcon = context.getResources().getDrawable(R.drawable.web_refresh);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case WEBVIEW_REFRESH_DISABLE:
                myIcon = context.getResources().getDrawable(R.drawable.web_refresh);
                myIcon.setColorFilter(getUnselectedColorFilter());
                break;

            case WEBVIEW_CLOSE:
                myIcon = context.getResources().getDrawable(R.drawable.extra_web_close);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case MENU_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.menu);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case LIBRARY_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.tab_library);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case DOWNLOAD_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.tab_download);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case INFO_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.tab_info);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case LEFT_MENU_DOWN:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_down);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case LEFT_MENU_UP:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_up);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case LEFT_MENU_CATEGORY:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_category_icon1);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case LEFT_MENU_LINK:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_link);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case CATEGORY_SELECT:
                myIcon = context.getResources().getDrawable(R.drawable.category_select);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case SEARCH_CLEAR:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case SEARCH_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case LIBRARY_ICON_SELECTED:
                myIcon = context.getResources().getDrawable(R.drawable.tab_library);
                myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
                break;

            case DOWNLOAD_ICON_SELECTED:
                myIcon = context.getResources().getDrawable(R.drawable.tab_download);
                myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
                break;

            case INFO_ICON_SELECTED:
                myIcon = context.getResources().getDrawable(R.drawable.tab_info);
                myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
                break;

            case FACEBOOK_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.facebook);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case TWITTER_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.twitter);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case INSTAGRAM_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.instagram);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case LINKEDIN_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.linkedin);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case WEB_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.web);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MAIL_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.mail);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case READER_MENU:
                myIcon = context.getResources().getDrawable(R.drawable.table_of_contents);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case READER_MAIL:
                myIcon = context.getResources().getDrawable(R.drawable.reader_share);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case GOOGLE_PLUS_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.google_plus);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case PINTEREST_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.pinterest);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case TUMBLR_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.tumblr);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case YOUTUBE_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.youtube);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MENU_SELECTED:
                myIcon = context.getResources().getDrawable(R.drawable.menu);
                myIcon.setColorFilter(getUnselectedColorFilter());
                break;

            case READER_UCGEN:
                myIcon = context.getResources().getDrawable(R.drawable.reader_ucgen);
                myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.9));
                break;

            case SETTING_POPUP_ARROW:
                myIcon = context.getResources().getDrawable(R.drawable.setting_popup_arrowup);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case VIEWER_LOGIN_LOGO:
                myIcon = context.getResources().getDrawable(R.drawable.viewer_logo);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case INTERNET_CONNECTION_ERROR:
                myIcon = context.getResources().getDrawable(R.drawable.no_connection);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case VIEWER_USERNAME_ACTIVE_INPUT_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.login_username);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case VIEWER_USERNAME_PASSIVE_INPUT_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.login_username);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case VIEWER_PASSWORD_ACTIVE_INPUT_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.login_password);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            case VIEWER_PASSWORD_PASSIVE_INPUT_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.login_password);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case CATEGORY_UNSELECT:
                myIcon = context.getResources().getDrawable(R.drawable.category_unselect);
                myIcon.setColorFilter(getThemeColorFilterWithAlpha((float) 0.5));
                break;

            case PASSIVE_SEARCH_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_search_icon);
                myIcon.setColorFilter(getThemeColorFilterWithAlpha((float) 0.5));
                break;

            case PASSIVE_SEARCH_CLEAR_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.left_menu_clear_icon_light);
                myIcon.setColorFilter(getThemeColorFilterWithAlpha((float) 0.5));
                break;

            case HOME_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.tab_home);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case HOME_ICON_SELECTED:
                myIcon = context.getResources().getDrawable(R.drawable.tab_home);
                myIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
                break;

            case READER_SEARCH_OPEN:
                myIcon = context.getResources().getDrawable(R.drawable.library_menu_search);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case READER_SEARCH_CLEAR:
                myIcon = context.getResources().getDrawable(R.drawable.reader_search_clear);
                myIcon.setColorFilter(getReaderSearchColorFilter());
                break;

            case READER_MENU_OPEN1:
                if (themeType == DARK_THEME_TYPE) {
                    myIcon = context.getResources().getDrawable(R.drawable.reader_thumb_menu_dark);
                } else {
                    myIcon = context.getResources().getDrawable(R.drawable.reader_thumb_menu_light);
                }
                break;

            case READER_MENU_OPEN2:
                myIcon = context.getResources().getDrawable(R.drawable.reader_thumb_menu_arrow);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case READER_MENU_OPEN_OK:
                myIcon = context.getResources().getDrawable(R.drawable.reader_bottom_ok);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case CUSTOM_PROGRESS_PULSE:
                myIcon = context.getResources().getDrawable(R.drawable.progress_icon);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case MEMBERSHIP_LOGIN:
                myIcon = context.getResources().getDrawable(R.drawable.membership_login);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MEMBERSHIP_LOGOUT:
                myIcon = context.getResources().getDrawable(R.drawable.membership_logout);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MEMBERSHIP_RESTORE:
                myIcon = context.getResources().getDrawable(R.drawable.membership_restore);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MEMBERSHIP_SUBSCRIPTION:
                myIcon = context.getResources().getDrawable(R.drawable.membership_subscription);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;

            case MEMBERSHIP_POPUP_CLOSE_BASE:
                myIcon = context.getResources().getDrawable(R.drawable.popup_close_base_circle);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case VERIFICATION_POPUP_CLOSE_BASE:
                myIcon = context.getResources().getDrawable(R.drawable.popup_close_base_circle);
                myIcon.setColorFilter(getDarkThemeColorFilterWithAlpha((float) 0.5));
                break;

            case SEARCH_MENU_ICON:
                myIcon = context.getResources().getDrawable(R.drawable.library_menu_search);
                myIcon.setColorFilter(getForegroundColorFilter());
                break;

            case MENU_INFO:
                myIcon = context.getResources().getDrawable(R.drawable.menu_info);
                myIcon.setColorFilter(getThemeColorFilter());
                break;

            default:
                myIcon = context.getResources().getDrawable(R.drawable.library_menu_search);
                myIcon.setColorFilter(getReverseThemeColorFilter());
                break;
        }
        return myIcon;
    }

    //Custom tabbarlarda url seklinde gelen ikonlarin foreground rengine gore boyandigi metod
    public void paintRemoteIcon(final Context context, final TabbarItem item, final ImageView image) {

        DisplayImageOptions displayConfig = new DisplayImageOptions.Builder()
                .cacheInMemory(true).build();
        ImageLoader.getInstance().displayImage(item.getIconUrl(), image, displayConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                Drawable myIcon = context.getResources().getDrawable(R.drawable.download_error_icon);
                myIcon.setColorFilter(getForegroundColorFilter());
                Drawable mySelectedIcon = context.getResources().getDrawable(R.drawable.download_error_icon);
                mySelectedIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));
                ((ImageView) view).setImageDrawable(new TabbarStateList(true, myIcon, mySelectedIcon));
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Drawable myIcon = new BitmapDrawable(context.getResources(), bitmap);
                Drawable mySelectedIcon = new BitmapDrawable(context.getResources(), bitmap);

                myIcon.setColorFilter(getForegroundColorFilter());
                mySelectedIcon.setColorFilter(getForeGroundColorFilterWithAlpha((float) 0.5));

                ((ImageView) view).setImageDrawable(new TabbarStateList(true, myIcon, mySelectedIcon));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public int getThemeTranperentForegroundColor() {
        return Color.parseColor("#CC" + foregroundColor.substring(1));
    }

    public String getForegroundHexColor() {
        return foregroundColor;
    }

    //Popup ekraninda cikan indir sil guncelle oku butonlarinin drawable
    public Drawable getContentActionButtonsDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(7);
        normal.setColor(Color.TRANSPARENT);
        if (context.getResources().getDisplayMetrics().density >= (float) 2.0)
            normal.setStroke(3, getInstance().getForegroundColor());
        else
            normal.setStroke(2, getInstance().getForegroundColor());

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(7);
        pressed.setColor(Color.TRANSPARENT);
        if (context.getResources().getDisplayMetrics().density >= (float) 2.0)
            pressed.setStroke(3, getInstance().getThemeTranperentForegroundColor());
        else
            pressed.setStroke(2, getInstance().getThemeTranperentForegroundColor());

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Viewer Login ekrani login butonu drawable
    public Drawable getLoginButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        pressed.setColor(getThemeColorWithAlpha(30));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Login ekrani login butonu drawable
    public Drawable getPopupLoginButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        normal.setColor(getThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        pressed.setColor(getThemeColorWithAlpha(30));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Viewer Logout butonu background
    public Drawable getLogoutButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        pressed.setColor(getTransperentThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Verification giris butonu background
    public Drawable getVerificationLoginButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadii(new float[]{
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                0, 0,
                0, 0,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2});
        normal.setColor(getDarkThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadii(new float[]{
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                0, 0,
                0, 0,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2});
        pressed.setColor(getLightThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Verification signup butonu background
    public Drawable getVerificationSubmitButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(getDarkThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(getLightThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }


    //Verification login butonu background
    public Drawable getVerificationSignupButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadii(new float[]{
                0, 0,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                0, 0
        });
        normal.setColor(getDarkThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadii(new float[]{
                0, 0,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                context.getResources().getDimension(R.dimen.verification_input_height) / 2, context.getResources().getDimension(R.dimen.verification_input_height) / 2,
                0, 0});
        pressed.setColor(getLightThemeColor());
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Verification login-signup-facebook butonu background
    public Drawable getVerificationFacebookButtonDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(Color.parseColor("#2c467b"));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        pressed.setColor(Color.parseColor("#AA2c467b"));
        pressed.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getVerificationLoginInputDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        normal.setColor(getDarkThemeColor());
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable focused = new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.verification_input_height));
        focused.setColor(getLightThemeColor());
        focused.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_focused},
                focused);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }


    public Drawable getVerificationClose(Context context) {

        Drawable normal;
        Drawable pressed;

        normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
        pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_focused},
                pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    public Drawable getCropPageButtonDrawable(Context context, int resourceType) {
        Drawable myIcon;
        if (resourceType == CROP_PAGE_SUBMIT) {
            myIcon = context.getResources().getDrawable(R.drawable.crop_submit);
            myIcon.setColorFilter(getForegroundColorFilter());
        } else {
            myIcon = context.getResources().getDrawable(R.drawable.crop_cancel);
            myIcon.setColorFilter(getForegroundColorFilter());
        }

        return myIcon;
    }

    public Drawable getPopupButtonDrawable(Context context, int resourceType) {

        Drawable normal;
        Drawable pressed;

        switch (resourceType) {
            case MEMBERSHIP_POPUP_CLOSE:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);
                    pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.login_popup_cancel_light);
                    pressed = context.getResources().getDrawable(R.drawable.login_popup_cancel_dark);
                }
                break;

            case READ_CONTENT:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_read_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_read_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_read_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_read_dark);
                }
                break;

            case DELETE_CONTENT:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_delete_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_delete_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_delete_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_delete_dark);
                }
                break;

            case UPDATE_CONTENT:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_update_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_update_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_update_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_update_dark);
                }
                break;

            case CANCEL_CONTENT_DOWNLOAD:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_cancel_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_cancel_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_cancel_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_cancel_dark);
                }
                break;

            case DOWNLOAD_CONTENT_FREE_ARROW:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_free_arrow_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_free_arrow_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_free_arrow_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_free_arrow_dark);
                }
                break;

            case DOWNLOAD_CONTENT_FREE:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_free_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_free_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_free_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_free_dark);
                }
                break;

            case DOWNLOAD_CONTENT_CLOUD:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_dark);
                }
                break;

            case DOWNLOAD_CONTENT_CLOUD_ARROW:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_cloud_arrow_dark);
                }
                break;

            case DOWNLOAD_CONTENT_PURCHASE_ARROW:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_dark_arrow);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_light_arrow);

                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_light_arrow);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_dark_arrow);
                }
                break;

            case DOWNLOAD_CONTENT_PURCHASE_BOTTOM:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_dark_bottom);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_light_bottom);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_purchase_light_bottom);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_purchase_dark_bottom);

                }
                break;

            case DOWNLOAD_CONTENT_BUTTON_BACKGROUND:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_bg_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_bg_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_download_bg_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_download_bg_dark);
                }
                break;

            case POPUP_DESCRIPTION_OPEN:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.swipe_open_dark);
                    pressed = context.getResources().getDrawable(R.drawable.swipe_open_dark);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.swipe_open_light);
                    pressed = context.getResources().getDrawable(R.drawable.swipe_open_light);
                }
                break;

            case POPUP_DESCRIPTION_CLOSE:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.swipe_close_dark);
                    pressed = context.getResources().getDrawable(R.drawable.swipe_close_dark);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.swipe_close_light);
                    pressed = context.getResources().getDrawable(R.drawable.swipe_close_light);
                }
                break;

            default:
                if (getInstance().themeType == DARK_THEME_TYPE) {
                    normal = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg_dark);
                    pressed = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg_light);
                } else {
                    normal = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg_light);
                    pressed = context.getResources().getDrawable(R.drawable.popup_purchase_download_bg_dark);
                }
                break;
        }
        return new PopupButtonStateList(normal, pressed);
    }


    public Drawable getLeftMenuIconDrawable(Context context, int resourceType) {

        Drawable normal;
        Drawable pressed;

        switch (resourceType) {
            case FACEBOOK_ICON:
                normal = context.getResources().getDrawable(R.drawable.facebook);
                pressed = context.getResources().getDrawable(R.drawable.facebook);
                break;

            case TWITTER_ICON:
                normal = context.getResources().getDrawable(R.drawable.twitter);
                pressed = context.getResources().getDrawable(R.drawable.twitter);
                break;

            case INSTAGRAM_ICON:
                normal = context.getResources().getDrawable(R.drawable.instagram);
                pressed = context.getResources().getDrawable(R.drawable.instagram);
                break;

            case LINKEDIN_ICON:
                normal = context.getResources().getDrawable(R.drawable.linkedin);
                pressed = context.getResources().getDrawable(R.drawable.linkedin);
                break;

            case WEB_ICON:
                normal = context.getResources().getDrawable(R.drawable.web);
                pressed = context.getResources().getDrawable(R.drawable.web);
                break;

            case MAIL_ICON:
                normal = context.getResources().getDrawable(R.drawable.mail);
                pressed = context.getResources().getDrawable(R.drawable.mail);
                break;

            case BEHANCE_ICON:
                normal = context.getResources().getDrawable(R.drawable.behance);
                pressed = context.getResources().getDrawable(R.drawable.behance);
                break;

            case FLICKER_ICON:
                normal = context.getResources().getDrawable(R.drawable.flickr);
                pressed = context.getResources().getDrawable(R.drawable.flickr);
                break;

            case SWARM_ICON:
                normal = context.getResources().getDrawable(R.drawable.swarm);
                pressed = context.getResources().getDrawable(R.drawable.swarm);
                break;

            case FOURSQUARE_ICON:
                normal = context.getResources().getDrawable(R.drawable.foursquare);
                pressed = context.getResources().getDrawable(R.drawable.foursquare);
                break;

            case GOOGLE_PLUS_ICON:
                normal = context.getResources().getDrawable(R.drawable.google_plus);
                pressed = context.getResources().getDrawable(R.drawable.google_plus);
                break;

            case PINTEREST_ICON:
                normal = context.getResources().getDrawable(R.drawable.pinterest);
                pressed = context.getResources().getDrawable(R.drawable.pinterest);
                break;

            case TUMBLR_ICON:
                normal = context.getResources().getDrawable(R.drawable.tumblr);
                pressed = context.getResources().getDrawable(R.drawable.tumblr);
                break;

            case YOUTUBE_ICON:
                normal = context.getResources().getDrawable(R.drawable.youtube);
                pressed = context.getResources().getDrawable(R.drawable.youtube);
                break;

            case MEMBERSHIP_LOGIN:
                normal = context.getResources().getDrawable(R.drawable.membership_login);
                pressed = context.getResources().getDrawable(R.drawable.membership_login);
                break;

            case MEMBERSHIP_RESTORE:
                normal = context.getResources().getDrawable(R.drawable.membership_restore);
                pressed = context.getResources().getDrawable(R.drawable.membership_restore);
                break;

            case MEMBERSHIP_SUBSCRIPTION:
                normal = context.getResources().getDrawable(R.drawable.membership_subscription);
                pressed = context.getResources().getDrawable(R.drawable.membership_subscription);
                break;

            default:
                normal = context.getResources().getDrawable(R.drawable.membership_logout);
                pressed = context.getResources().getDrawable(R.drawable.membership_logout);
                break;
        }

        normal.setColorFilter(getThemeColorFilter());
        pressed.setColorFilter(getThemeColorFilterWithAlpha((float) 0.5));

        return new LeftMenuStateList(normal, pressed);
    }

    //Viewer Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getLoginInputDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        normal.setColor(getThemeColorWithAlpha(10));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable focused = new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        focused.setColor(getThemeColorWithAlpha(30));
        focused.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_focused},
                focused);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Login ekraninda kullanici adi sifre girilen edittextlerin background
    public Drawable getPopupLoginInputDrawable(Context context) {
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        normal.setColor(getThemeColorWithAlpha(10));
        normal.setStroke(0, Color.TRANSPARENT);

        GradientDrawable focused = new GradientDrawable();
        focused.setCornerRadius(context.getResources().getDimension(R.dimen.popup_login_input_height));
        focused.setColor(getThemeColorWithAlpha(30));
        focused.setStroke(0, Color.TRANSPARENT);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_focused},
                focused);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                normal);

        return drawable;
    }

    //Sol menu search tiklandiginda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getActiveSearchViewDrawable(Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(2, getThemeColor());
        return drawable;
    }

    //Sol menu search passive oldugunda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getPassiveSearchViewDrawable(Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.login_input_height));
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(2, getThemeColorWithAlpha(50));
        return drawable;
    }


    public int getReaderSearchResultTextColor() {
        if (themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#E9E9E9");
        } else {
            return Color.parseColor("#333333");
        }
    }


    public int getReaderSearchTextColor() {
        if (themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#444444");
        } else {
            return Color.parseColor("#C2C2C2");
        }
    }


    public int getReaderPopupColor() {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor("#333333"); //Dark Theme
        } else {
            return Color.parseColor("#E9E9E9"); //Light Theme
        }
    }

    //reader menu search tiklandiginda search view in arka planini degistirmek icin bu metod kullaniliyor
    public Drawable getReaderSearchViewDrawable(Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(20);
        if (themeType == DARK_THEME_TYPE) {
            drawable.setColor(Color.parseColor("#707271"));
        } else {
            drawable.setColor(Color.parseColor("#E9E9E9"));
        }
        drawable.setStroke(0, Color.parseColor("#E9E9E9"));
        return drawable;
    }


    public int getDownloadProgressColorWithAlpha(int alpha) {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "000000"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "FFFFFF"); //Light Theme
        }
    }

    //alpha degeri 0-255 arası olmali convertIntAlphaToHex metodunda hex hesaplamasi yapiliyor
    public int getPoupDescritionColorWithAlpha(int alpha) {
        if (getInstance().themeType == DARK_THEME_TYPE) {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "333333"); //Dark Theme
        } else {
            return Color.parseColor(convertIntAlphaToHex(alpha) + "FFFFFF"); //Light Theme
        }
    }


    /*
    * (MG)
    * Try catch koymamım sebebi lollipop versiyonunda font bulunamadı hatası almamız
    * https://fabric.io/galepress/android/apps/ak.detaysoft.ekinyayincilikdis/issues/56e18957ffcdc04250b80d10
    * */

    public Typeface getRubikLight(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Rubik-Light.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getRubikRegular(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Rubik-Regular.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getRubikMedium(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Rubik-Medium.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public Typeface getRubikBold(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Rubik-Bold.ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }
}
