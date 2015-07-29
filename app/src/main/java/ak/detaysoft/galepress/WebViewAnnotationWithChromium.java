package ak.detaysoft.galepress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.Toast;

import com.artifex.mupdfdemo.*;
import com.mogoweb.chrome.JavascriptInterface;
import com.mogoweb.chrome.WebChromeClient;
import com.mogoweb.chrome.WebSettings;
import com.mogoweb.chrome.WebView;

import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by adem on 08/08/14.
 */
public class WebViewAnnotationWithChromium extends WebView {
    public float x1 , x2, y1 , y2;
    public float left, top ;
    public MuPDFReaderView readerView;
    public LinkInfoExternal linkInfoExternal;
    private Context mContext;
    private CustomPulseProgress loading;

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeViewVisible();
        }
    };

    private class MyWebClient extends com.mogoweb.chrome.WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundColor(Color.argb(1, 0, 0, 0));
            } else {
                view.setBackgroundColor(0x01000000);
            }
            view.setVisibility(View.VISIBLE);

            if(loading != null) {
                loading.setVisibility(GONE);

            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.setVisibility(View.INVISIBLE);
            if(loading != null) {
                loading.setVisibility(VISIBLE);
            }
            Log.e("loadedurl", ""+url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //view.loadUrl("file:///android_asset/annotation_not_loaded.html");
            view.loadUrl("about:blank");
            view.setVisibility(GONE);
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(loading != null) {
                loading.setVisibility(GONE);
            }

            ((PageView)view.getParent()).removeView(view);
        }

    }

    private class MyChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, android.webkit.WebChromeClient.CustomViewCallback callback) {
            Logout.e("Adem","onShowCustomView");
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            Logout.e("Adem","onHideCustomView");
            super.onHideCustomView();
        }
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public class StopVideoAndAudioInterface{
        Context mContext;
        public StopVideoAndAudioInterface(Context c){
            mContext = c;
        }
        @JavascriptInterface
        public void stopMedia(String script){
            Toast.makeText(mContext, script, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isHorizontalScrolling, isDummyAction;
    private MotionEvent previousMotionEvent;
    public WebViewAnnotationWithChromium(Context context, LinkInfoExternal lie, CustomPulseProgress loading) {
        super(context);

        mContext = context;
        this.loading = loading;
        //mContext.registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.linkInfoExternal = lie;
        this.setWebChromeClient(new com.mogoweb.chrome.WebChromeClient());
        this.setWebChromeClient(new MyChromeClient());
        this.setWebViewClient(new MyWebClient());
        /*if(lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_VIDEO){
            this.setLayerType(WebView.LAYER_TYPE_HARDWARE,null);
        }
        else if(lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_WEB){
            this.setLayerType(WebView.LAYER_TYPE_HARDWARE,null);
        }
        else{
            this.setLayerType(WebView.LAYER_TYPE_SOFTWARE,null);
        }*/
        //addJavascriptInterface(new StopVideoAndAudioInterface(context), "Script");
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
        s.setAppCacheEnabled(false);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        //s.setDefaultTextEncodingName("utf-8");
        s.setSupportZoom(false);

        setVisibility(View.INVISIBLE);
        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        this.setBackgroundColor(0x01000000);
        final WebViewAnnotationWithChromium web = this;

        if(linkInfoExternal != null){
            if(linkInfoExternal.mustHorizontalScrollLock()){
                this.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float dx,dy;
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            // Action DOWN
                            web.x1 = event.getX();
                            web.y1 = event.getY();
                            web.setPreviousMotionEvent(event);
                            web.isHorizontalScrolling = false;
                            if(web.isDummyAction){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                        else if(event.getAction() == MotionEvent.ACTION_MOVE){
                            // Action MOVE
                            web.x2 = event.getX();
                            web.y2 = event.getY();
                            dx = web.x2 - web.x1;
                            dy = web.y2 - web.y1;
                            if(Math.abs(dx) > 10 || Math.abs(dy) > 10){
                                if(!(Math.abs(dx) >  Math.abs(dy))) {
                                    // vertical
                                    web.isHorizontalScrolling = false;
                                    return false;
                                }else {
                                    // horizontal
                                    web.isHorizontalScrolling = true;
                                    if(web.getPreviousMotionEvent()!=null && web.getPreviousMotionEvent().getAction() != MotionEvent.ACTION_MOVE) {
                                        MotionEvent previousEvent = web.getPreviousMotionEvent();
                                        web.setPreviousMotionEvent(null);
                                        previousEvent.setLocation(previousEvent.getX() + WebViewAnnotationWithChromium.this.left, previousEvent.getY() + WebViewAnnotationWithChromium.this.top);
                                        readerView.onTouchEvent(previousEvent);
                                    }
                                    event.setLocation(event.getX() + WebViewAnnotationWithChromium.this.left, event.getY() + WebViewAnnotationWithChromium.this.top); // Webview size is not equal to page size. Optimize the location for page.
                                    readerView.onTouchEvent(event);
                                    return true;
                                }
                            }
                        }
                        else if(event.getAction() == MotionEvent.ACTION_UP){
                            // Action UP
                            if(web.isHorizontalScrolling){
                                web.isHorizontalScrolling = false;
                                event.setLocation(event.getX() + WebViewAnnotationWithChromium.this.left, event.getY() + WebViewAnnotationWithChromium.this.top); // Webview size is not equal to page size. Optimize the location for page.
                                readerView.onTouchEvent(event);
                                return true;
                            }
                            else{
                                if(web.getPreviousMotionEvent()!=null) {
                                    MotionEvent previousEvent = web.getPreviousMotionEvent();
                                    web.setPreviousMotionEvent(null);
                                    web.isDummyAction = true;
                                    web.onTouchEvent(previousEvent);
                                }
                                return false;
                            }
                        }
                        return false;
                    }
                });
            }
        }
    }

    public MotionEvent getPreviousMotionEvent() {
        return previousMotionEvent;
    }

    public void setPreviousMotionEvent(MotionEvent event) {
        if(event == null)
            this.previousMotionEvent = null;
        else
            this.previousMotionEvent = MotionEvent.obtain(event);
    }

    private void changeViewVisible(){
        setVisibility(View.INVISIBLE);
        if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
            setBackgroundColor(Color.parseColor("#00FFFFFF"));
            loadUrl(getUrl());
        }
        else
            setVisibility(View.INVISIBLE);
    }
}
