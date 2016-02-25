package com.artifex.mupdfdemo;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ak.detaysoft.galepress.R;

/**
 * Created by adem on 21/05/14.
 */
/** TODO: Bunun yerine ExtraWebView'i kullandim. Enes le gorusup barsiz kullanim gerekmiyorsa bu activity'yi silecegim.*/
public class ModalWebViewActivity extends Activity {
    private WebView webView;
    public String url = "http://www.google.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modal_web_view);

        url = (String)this.getIntent().getExtras().get("url");

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(url);
    }

}