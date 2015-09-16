package ak.detaysoft.galepress;

/**
 * Created by p1025 on 16.09.2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by p1025 on 14.09.2015.
 */
public class ForgotPasswordActivity extends Activity {

    private RelativeLayout popup;
    private LinearLayout baseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);
        //set background colors
        baseView = (LinearLayout) findViewById(R.id.forgot_password_base_view);
        baseView.setBackgroundColor(Color.TRANSPARENT);
        baseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                setResult(101, intent);
                finish();
            }
        });

        Button close = (Button)findViewById(R.id.activity_forgot_password_web_close);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            close.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE));
        else
            close.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                setResult(101, intent);
                finish();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((LinearLayout)close.getParent()).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));
        else
            ((LinearLayout)close.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));

        popup = ((RelativeLayout)findViewById(R.id.activity_forgot_password_popup));
        popup.setLayoutParams(new LinearLayout.LayoutParams(getIntent().getExtras().getInt("width")+ ((LinearLayout)close.getParent()).getLayoutParams().width - 8
                , getIntent().getExtras().getInt("height") + ((LinearLayout)close.getParent()).getLayoutParams().height - 8));
        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });


        CustomPulseProgress progress = (CustomPulseProgress)findViewById(R.id.activity_forgot_password_loading);
        progress.startAnim();

        WebView web = (WebView)findViewById(R.id.activity_forgot_password_web);
        web.setVisibility(View.INVISIBLE);
        prepareWebSetting(web);

        web.setWebViewClient(new MyWebViewClient(progress));
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("ConsoleMessage", consoleMessage.message());
                return true;
            }
        });

        RelativeLayout.LayoutParams webBaseParams = new RelativeLayout.LayoutParams(getIntent().getExtras().getInt("width"), getIntent().getExtras().getInt("height"));
        webBaseParams.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.forgot_password_base_view);
        ((RelativeLayout)web.getParent()).setLayoutParams(webBaseParams);

        GradientDrawable gradient =  new GradientDrawable();
        gradient.setCornerRadius(2);
        gradient.setColor(ApplicationThemeColor.getInstance().getThemeColor());
        gradient.setStroke(0, ApplicationThemeColor.getInstance().getThemeColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)web.getParent()).setBackground(gradient);
        else
            ((RelativeLayout)web.getParent()).setBackgroundDrawable(gradient);

        Integer applicationID = 0;
        try{
            applicationID = new Integer(GalePressApplication.getInstance().getApplicationId());
        } catch (Exception e){
            Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.cannot_load), Toast.LENGTH_SHORT).show();
            Intent intent = getIntent();
            setResult(101, intent);
            finish();
        }

        web.loadUrl("http://www.galepress.com/tr/mobil-kullanici/sifremi-unuttum/"+applicationID);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(101, intent);
        finish();
    }

    private void prepareWebSetting(WebView web){
        WebSettings s = web.getSettings();
        s.setBuiltInZoomControls(true);
        s.setPluginState(WebSettings.PluginState.ON);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);
    }

    private class MyWebViewClient extends WebViewClient {

        private CustomPulseProgress loading;

        public MyWebViewClient(CustomPulseProgress loading){
            this.loading = loading;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);

            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            view.setVisibility(View.VISIBLE);
            GradientDrawable gradient =  new GradientDrawable();
            gradient.setCornerRadius(2);
            gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
            gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)view.getParent()).setBackground(gradient);
            else
                ((RelativeLayout)view.getParent()).setBackgroundDrawable(gradient);
            loading.stopAnim();
            loading.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loading.startAnim();
            loading.setVisibility(View.VISIBLE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //view.loadUrl("file:///android_asset/annotation_not_loaded.html");

            view.loadUrl("about:blank");
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            view.setVisibility(View.VISIBLE);
            GradientDrawable gradient =  new GradientDrawable();
            gradient.setCornerRadius(2);
            gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
            gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)view.getParent()).setBackground(gradient);
            else
                ((RelativeLayout)view.getParent()).setBackgroundDrawable(gradient);
            loading.stopAnim();
            loading.setVisibility(View.GONE);
            Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.cannot_load), Toast.LENGTH_SHORT).show();
            Intent intent = getIntent();
            setResult(101, intent);
            finish();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}

