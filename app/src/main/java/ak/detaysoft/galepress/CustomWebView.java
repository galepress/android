package ak.detaysoft.galepress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.mogoweb.chrome.WebChromeClient;
import com.mogoweb.chrome.WebSettings;
import com.mogoweb.chrome.WebView;
import com.mogoweb.chrome.WebViewClient;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 30.04.2015.
 */
public class CustomWebView extends WebView {

    private Context context;
    private boolean isWebFragment = false;
    private ProgressBar progressBar;

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if(GalePressApplication.getInstance().getCurrentActivity() != null && GalePressApplication.getInstance().getCurrentActivity().getClass() == MainActivity.class){
                Fragment fragment = GalePressApplication.getInstance().getCurrentFragment();
                if(fragment != null && fragment.getTag().compareTo(MainActivity.LIBRARY_TAB_TAG) != 0
                        && fragment.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) != 0 && fragment.getTag().compareTo(MainActivity.INFO_TAB_TAG) != 0 && isWebFragment){
                    ((MainActivity) context).prepareActionBarForCustomTab(view, true, true);
                    ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
                }
            }

            view.setVisibility(VISIBLE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(isWebFragment) {
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((LinearLayout)progressBar.getParent()).setVisibility(View.VISIBLE);
                ((LinearLayout)progressBar.getParent()).bringToFront();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(isWebFragment) {
                ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
            }
        }

    }

    public class MyChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public CustomWebView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public CustomWebView(Context context,ProgressBar progressBar, boolean isWebFragment) {
        super(context);
        this.isWebFragment = isWebFragment;
        this.context = context;
        this.progressBar = progressBar;
        initView();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

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
        s.setMediaPlaybackRequiresUserGesture(false); //false olarak set edilmediği autoplay çalışmıyor.
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        if(!isWebFragment)
            this.setBackgroundColor(Color.parseColor("#00FFFFFF"));
    }

}
