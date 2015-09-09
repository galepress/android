package ak.detaysoft.galepress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by adem on 15/04/14.
 */
public class ExtraWebViewActivity extends Activity {
    private WebView webView;
    public String url = "http://www.google.com";
    public boolean isMainActivitIntent = false;
    private boolean isModal = false;
    ProgressBar progressBar;
    ImageButton ileriButton,geriButton,refreshButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_web_view_layout);
        progressBar = (ProgressBar) findViewById(R.id.extra_web_view_close_progress_bar);


        if(getIntent().getExtras().containsKey("isModal"))
            isModal = this.getIntent().getExtras().getBoolean("isModal");

        ileriButton = (ImageButton) findViewById(R.id.extra_web_view_ileri_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        else
            ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        ((RelativeLayout)findViewById(R.id.extra_web_view_actionbar)).setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        geriButton = (ImageButton) findViewById(R.id.extra_web_view_geri_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        else
            geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        refreshButton = (ImageButton) findViewById(R.id.extra_web_view_refresh_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
        else
            refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));

        ileriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        geriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webView.canGoBack()){
                    webView.goBack();
                }
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.extra_web_view_title);
        titleTextView.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        titleTextView.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(ExtraWebViewActivity.this));


        if(isModal){
            ileriButton.setVisibility(View.GONE);
            geriButton.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
            titleTextView.setVisibility(View.GONE);
        }

        ImageButton closeButton = (ImageButton) findViewById(R.id.extra_web_view_close_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            closeButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        else
            closeButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if(isMainActivitIntent)
                    overridePendingTransition(0, R.animator.right_to_left_translate);
            }
        });

        url = (String)this.getIntent().getExtras().get("url");
        if(getIntent().getExtras().containsKey("isMainActivitIntent"))
            isMainActivitIntent = this.getIntent().getExtras().getBoolean("isMainActivitIntent");


        webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        final ExtraWebViewActivity activity = ExtraWebViewActivity.this;
        webView.setWebViewClient(new AK_WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(0xFF00D0FF, android.graphics.PorterDuff.Mode.MULTIPLY);
                progressBar.setVisibility(View.VISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(ExtraWebViewActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
                else
                    refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(ExtraWebViewActivity.this, ApplicationThemeColor.WEBVIEW_REFRESH_DISABLE));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                enableDisableNavigationButtons(view);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(url);
    }
    public void enableDisableNavigationButtons(WebView webView) {
        // if has previous page, enable the back button
        if(webView.canGoBack()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
            else
                geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK));
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                geriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
            else
                geriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_BACK_DISABLE));
        }
        // if has next page, enable the next button
        if(webView.canGoForward()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
            else
                ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT));
        } else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ileriButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
            else
                ileriButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_NEXT_DISABLE));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            refreshButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));
        else
            refreshButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_REFRESH));


    }



    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
    */
}

