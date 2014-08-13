package ak.detaysoft.galepress;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.artifex.mupdfdemo.*;

/**
 * Created by adem on 08/08/14.
 */
public class WebViewAnnotation extends WebView {
    public float x1 , x2, y1 , y2, dx, dy;
    public float left, top ;
    public MuPDFReaderView readerView;


    public boolean isHorizontalScrolling, isDummyAction;
    private MotionEvent previousMotionEvent;
    public WebViewAnnotation(Context context) {
        super(context);
        this.setWebChromeClient(new WebChromeClient());
        this.setInitialScale(1);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // Android eski versiyonlarda da webviewer transparan yapar.
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.setVerticalScrollBarEnabled(false);     // Webviewer'da kontrol edilecek.
        this.setHorizontalScrollBarEnabled(false);   // Webviewer'da kontrol edilecek.
        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setDomStorageEnabled(true);
        this.setHorizontalScrollBarEnabled(false);
        this.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });
        this.setEnabled(true);
        final WebViewAnnotation web = this;
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
