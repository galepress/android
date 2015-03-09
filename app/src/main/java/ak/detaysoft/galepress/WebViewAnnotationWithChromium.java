package ak.detaysoft.galepress;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.artifex.mupdfdemo.*;
import com.mogoweb.chrome.WebSettings;
import com.mogoweb.chrome.WebView;

/**
 * Created by adem on 08/08/14.
 */
public class WebViewAnnotationWithChromium extends WebView {
    public float x1 , x2, y1 , y2;
    public float left, top ;
    public MuPDFReaderView readerView;
    public LinkInfoExternal linkInfoExternal;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public boolean isHorizontalScrolling, isDummyAction;
    private MotionEvent previousMotionEvent;
    public WebViewAnnotationWithChromium(Context context, LinkInfoExternal lie) {
        super(context);

        this.linkInfoExternal = lie;
        this.setWebChromeClient(new com.mogoweb.chrome.WebChromeClient());
        this.setWebViewClient(new com.mogoweb.chrome.WebViewClient());

        /*if(lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_VIDEO || lie.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_WEB)
        {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else{
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }*/

        WebSettings s = getSettings();
        s.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT < 8) {
            s.setPluginsEnabled(true);
        }
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);s.setDefaultTextEncodingName("utf-8");
        s.setSupportZoom(false);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.setBackgroundColor(Color.YELLOW);
        final WebViewAnnotationWithChromium web = this;

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
