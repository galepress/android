package ak.detaysoft.galepress.web_views;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 04.01.2016.
 */
public class ExtraWebViewWithCrosswalkActivity extends Activity {
    private XWalkView webView;
    public String url = "http://www.google.com";
    public boolean isMainActivitIntent = false;
    private boolean isModal = false;
    ProgressBar progressBar;
    ImageButton ileriButton, geriButton, refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_crosswalk_webview_layout);
        progressBar = (ProgressBar) findViewById(R.id.crosswalk_extra_web_view_load_progress_bar);


        if (getIntent().getExtras().containsKey("isModal"))
            isModal = this.getIntent().getExtras().getBoolean("isModal");

        ileriButton = (ImageButton) findViewById(R.id.crosswalk_extra_web_view_ileri_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        else
            ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        ((RelativeLayout) findViewById(R.id.crosswalk_extra_web_view_actionbar)).setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        geriButton = (ImageButton) findViewById(R.id.crosswalk_extra_web_view_geri_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        else
            geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        refreshButton = (ImageButton) findViewById(R.id.crosswalk_extra_web_view_refresh_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
        else
            refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));

        ileriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.getNavigationHistory().canGoForward()) {
                    webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
                }
            }
        });
        geriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.getNavigationHistory().canGoBack()) {
                    webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                }
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload(XWalkView.RELOAD_NORMAL);
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.crosswalk_extra_web_view_title);
        titleTextView.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        titleTextView.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(ExtraWebViewWithCrosswalkActivity.this));


        if (isModal) {
            ileriButton.setVisibility(View.GONE);
            geriButton.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
            titleTextView.setVisibility(View.GONE);
        }

        ImageButton closeButton = (ImageButton) findViewById(R.id.crosswalk_extra_web_view_close_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            closeButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        else
            closeButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if (isMainActivitIntent)
                    overridePendingTransition(0, R.animator.right_to_left_translate);
            }
        });

        url = (String) this.getIntent().getExtras().get("url");
        if (getIntent().getExtras().containsKey("isMainActivitIntent"))
            isMainActivitIntent = this.getIntent().getExtras().getBoolean("isMainActivitIntent");


        webView = (XWalkView) findViewById(R.id.crosswalk_webview);
        MyXWalkResourceClient resourceClient = new MyXWalkResourceClient(webView);
        webView.setResourceClient(resourceClient);

        XWalkSettings settings = new XWalkSettings(webView.getContext(), null, false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAppCacheEnabled(true);

        MyXUIClient uiClient = new MyXUIClient(webView);
        webView.setUIClient(uiClient);

        webView.load(url, null);

        ((LinearLayout)progressBar.getParent()).bringToFront();
        ((LinearLayout)progressBar.getParent()).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    if (isMainActivitIntent)
                        overridePendingTransition(0, R.animator.right_to_left_translate);
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public void enableDisableNavigationButtons(XWalkView webView) {
        // if has previous page, enable the back button
        if (webView.getNavigationHistory().canGoBack()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
            else
                geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
            else
                geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        }
        // if has next page, enable the next button
        if (webView.getNavigationHistory().canGoForward()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
            else
                ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
            else
                ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));
        else
            refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));


    }

    class MyXWalkResourceClient extends XWalkResourceClient {


        public MyXWalkResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description,
                                        String failingUrl) {

            Log.d("MyXWalkResourceClient", "Load Failed:" + description);
            if (progressBar != null) {
                ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
                view.reload(XWalkView.RELOAD_NORMAL);
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
    }

    class MyXUIClient extends XWalkUIClient {

        public MyXUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onFullscreenToggled(XWalkView view, boolean enterFullscreen) {
            //super.onFullscreenToggled(view, enterFullscreen);
            Log.e("deneme", "" + enterFullscreen);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, String url) {
            super.onPageLoadStarted(view, url);
            if (progressBar != null) {
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(0xFF00D0FF, android.graphics.PorterDuff.Mode.MULTIPLY);
                ((LinearLayout)progressBar.getParent()).setVisibility(View.VISIBLE);
                ((LinearLayout)progressBar.getParent()).bringToFront();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(ExtraWebViewWithCrosswalkActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
                else
                    refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(ExtraWebViewWithCrosswalkActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
            }
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);
            ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
            enableDisableNavigationButtons(view);
        }

    }

}