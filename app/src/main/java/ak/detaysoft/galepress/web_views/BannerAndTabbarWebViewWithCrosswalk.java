package ak.detaysoft.galepress.web_views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.MainActivity;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 07.01.2016.
 */
public class BannerAndTabbarWebViewWithCrosswalk extends XWalkView {

    private Context context;
    private boolean isBannerWebView = true;
    private ProgressBar progressBar;
    private boolean isBannerUrlUpdated = false;

    public BannerAndTabbarWebViewWithCrosswalk(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public BannerAndTabbarWebViewWithCrosswalk(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public BannerAndTabbarWebViewWithCrosswalk(Context context, Activity activity) {
        super(context, activity);
    }


    public BannerAndTabbarWebViewWithCrosswalk(Context context, ProgressBar progressBar, boolean isBannerWebView) {
        super(context);
        this.isBannerWebView = isBannerWebView;
        this.context = context;
        this.progressBar = progressBar;
        initView();
    }

    public void initView(){

        setOnKeyListener(null);

        MyXWalkResourceClient resourceClient = new MyXWalkResourceClient(this);
        setResourceClient(resourceClient);

        XWalkSettings settings = new XWalkSettings(getContext(), null , false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAppCacheEnabled(true);

        MyXUIClient uiClient = new MyXUIClient(this);
        setUIClient(uiClient);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
         /*
         * alpha set edilmedigi zaman view siyah gorunuyor.
         * crosswalk jira da cozum olarak bu sekilde yapilmasi onerilmis (MG).
         */
        if(isBannerWebView) {
            setAlpha(0.9999f);
            setBackgroundColor(Color.TRANSPARENT);
        }
    }


    class MyXWalkResourceClient extends XWalkResourceClient {


        public MyXWalkResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description,
                                        String failingUrl) {
            Log.d("MyXWalkResourceClient", "Load Failed:" + description);
            if(!isBannerWebView) {
                ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
            } else {
                view.load("file:///android_asset/banner_not_loaded.html", null);
            }
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {

            if(isBannerWebView){
                if(!isBannerUrlUpdated && (!url.contains("file:///") && url.compareTo(GalePressApplication.getInstance().getBannerLink()) != 0)){
                    /*
                    * isBannerUrlUpdated kontrolu yapilmazsa yeni banner url geldiginde onu ExtraWebviewActivity de aciyor. (MG)
                    */
                    isBannerUrlUpdated = false;
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

    class MyXUIClient extends XWalkUIClient {

        public MyXUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onFullscreenToggled(XWalkView view, boolean enterFullscreen) {
            //super.onFullscreenToggled(view, enterFullscreen);
            Log.e("deneme", ""+enterFullscreen);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, String url) {
            super.onPageLoadStarted(view, url);
            if(!isBannerWebView) {
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((LinearLayout)progressBar.getParent()).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);
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

    }

    public void loadBannerUrl(String url){
        isBannerUrlUpdated = true;
        load(url, null);
    }
}