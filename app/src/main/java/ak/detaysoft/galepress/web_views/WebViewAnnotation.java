package ak.detaysoft.galepress.web_views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.artifex.mupdfdemo.*;

import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by adem on 08/08/14.
 */
public class WebViewAnnotation extends WebView {
    public float x1 , x2, y1 , y2;
    public float left, top ;
    public MuPDFReaderView readerView;
    public LinkInfoExternal linkInfoExternal;
    private CustomPulseProgress loading;
    private Context context;

    /*
    * Video ve ses iceriklerinde(ozellikle autoplay olanlarda) sayfa yuklenmesi bitmeden diger sayfaya gecilirse
    * javascript ile video ve ses durdurulamiyor.
    * Bu parametre ile MuPDFPageView classinda stopAllWebAnnotationsMedia metodunda kontrol edilerek devam eden loading iptal edilecek.
    */
    public boolean isLoadingFinished = false;

    private class MyWebChromeClient extends WebChromeClient {
        /*
        * Bu iki metod override edildigi zaman videolar gorunmuyor
        * */
        /*@Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }*/
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ak.detaysoft.galepress.Logout.e("Adem WebView", "shouldOverrideUrlLoading: " + url);
            // don't override URL so that stuff within iframe can work properly
            // view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isLoadingFinished = true;
            if(loading != null) {
                loading.setVisibility(GONE);
            }
            Logout.e("xxxxxx",""+url);
            view.setVisibility(VISIBLE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isLoadingFinished = false;
            if(loading != null) {
                loading.setVisibility(VISIBLE);
            }
            view.setVisibility(GONE);
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

    public boolean isHorizontalScrolling, isDummyAction;
    private MotionEvent previousMotionEvent;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public WebViewAnnotation(Context context, LinkInfoExternal lie, CustomPulseProgress loading) {
        super(context);
        this.loading = loading;
        this.linkInfoExternal = lie;
        this.context = context;
        this.setWebChromeClient(new MyWebChromeClient());
        this.setWebViewClient(new MyWebViewClient());

        if(lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_VIDEO){
            this.setLayerType(WebView.LAYER_TYPE_HARDWARE,null);
        }
        else if(lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_WEB){
            this.setLayerType(WebView.LAYER_TYPE_HARDWARE,null);
        }
        else{
            this.setLayerType(WebView.LAYER_TYPE_SOFTWARE,null);
        }

        WebSettings s = getSettings();
        s.setBuiltInZoomControls(true);
        s.setPluginState(WebSettings.PluginState.ON);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); //false olarak set edilmedigi autoplay calismiyor.
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.setBackgroundColor(Color.TRANSPARENT);
        setInitialScale(1);
        final WebViewAnnotation web = this;

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
                                    previousEvent.setLocation(previousEvent.getX() + left, previousEvent.getY() + top);
                                    readerView.onTouchEvent(previousEvent);
                                }
                                event.setLocation(event.getX() + left, event.getY() + top); // Webview size is not equal to page size. Optimize the location for page.
                                readerView.onTouchEvent(event);
                                return true;
                            }
                        }
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        // Action UP
                        if(web.isHorizontalScrolling){
                            web.isHorizontalScrolling = false;
                            event.setLocation(event.getX() + left, event.getY() + top); // Webview size is not equal to page size. Optimize the location for page.
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

    public MotionEvent getPreviousMotionEvent() {
        return previousMotionEvent;
    }

    public void setPreviousMotionEvent(MotionEvent event) {
        if(event == null)
            this.previousMotionEvent = null;
        else
            this.previousMotionEvent = MotionEvent.obtain(event);
    }



}