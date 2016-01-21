package ak.detaysoft.galepress.web_views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.MainActivity;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 30.04.2015.
 */
public class BannerAndTabbarWebView extends WebView {

    private Context context;
    private boolean isBannerWebView = true;
    private ProgressBar progressBar;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public BannerAndTabbarWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public BannerAndTabbarWebView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public BannerAndTabbarWebView(Context context, ProgressBar progressBar, boolean isBannerWebView) {
        super(context);
        this.isBannerWebView = isBannerWebView;
        this.context = context;
        this.progressBar = progressBar;
        initView();
    }

    public BannerAndTabbarWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initView(){
        this.setWebChromeClient(new MyChromeClient());
        this.setWebViewClient(new MyWebClient());

        WebSettings s = getSettings();
        s.setBuiltInZoomControls(true);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        if(Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            s.setMediaPlaybackRequiresUserGesture(false); //false olarak set edilmediği autoplay çalışmıyor.
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        if(isBannerWebView) {
            this.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        }
    }

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if(GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass() == MainActivity.class){
                Fragment fragment = GalePressApplication.getInstance().getCurrentFragment();
                if(fragment != null && fragment.getTag().compareTo(MainActivity.LIBRARY_TAB_TAG) != 0
                        && fragment.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) != 0 && fragment.getTag().compareTo(MainActivity.INFO_TAB_TAG) != 0 && !isBannerWebView){
                    ((MainActivity) context).prepareActionBarForCustomTab(view, true, true);
                    ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
                }
            }

            view.setVisibility(VISIBLE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(!isBannerWebView) {
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((LinearLayout)progressBar.getParent()).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(!isBannerWebView) {
                ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
            } else {
                view.loadUrl("file:///android_asset/banner_not_loaded.html");
            }
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(isBannerWebView){
                if(!url.contains("file:///") && url.compareTo(GalePressApplication.getInstance().getBannerLink()) != 0){
                    final int KITKAT = 19; // Android 4.4
                    if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
                        Intent intent = new Intent(context, ExtraWebViewActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("isMainActivitIntent", false);
                        context.startActivity(intent);
                        return true;
                    } else {
                        Intent intent = new Intent(context, ExtraWebViewWithCrosswalkActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("isMainActivitIntent", false);
                        context.startActivity(intent);
                        return true;
                    }

                }
                return false;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    public class MyChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    public void loadBannerUrl(String url){
        loadUrl(url);
    }
}
