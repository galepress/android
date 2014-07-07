package ak.detaysoft.galepress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by adem on 15/04/14.
 */
public class ExtraWebViewActivity extends Activity {
    private WebView webView;
    public String url = "http://www.google.com";
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_web_view_layout);
        progressBar = (ProgressBar) findViewById(R.id.extra_web_view_close_progress_bar);

        ImageButton imageButton = (ImageButton) findViewById(R.id.extra_web_view_close_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        url = (String)this.getIntent().getExtras().get("url");

        webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                TextView titleTextView = (TextView) findViewById(R.id.extra_web_view_title);
                titleTextView.setText(title);
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
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(url);
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

