package ak.detaysoft.galepress.web_views;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.artifex.mupdfdemo.LinkInfoExternal;
import com.artifex.mupdfdemo.MuPDFReaderView;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by p1025 on 05.11.2015.
 */
public class WebViewAnnotationWithCrosswalk extends XWalkView {

    public float x1 , x2, y1 , y2;
    public float left, top ;
    public MuPDFReaderView readerView;
    public LinkInfoExternal linkInfoExternal;
    private CustomPulseProgress loading;
    private Context context;
    public boolean isHorizontalScrolling, isDummyAction;
    private MotionEvent previousMotionEvent;

    /*
    * Video ve ses iceriklerinde(ozellikle autoplay olanlarda) sayfa yuklenmesi bitmeden diger sayfaya gecilirse
    * javascript ile video ve ses durdurulamiyor.
    * Bu parametre ile MuPDFPageView classinda stopAllWebAnnotationsMedia metodunda kontrol edilerek devam eden loading iptal edilecek.
    */
    public boolean isLoadingFinished = false;

    class MyXWalkResourceClient extends XWalkResourceClient {


        public MyXWalkResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description,
                                        String failingUrl) {
            Log.d("MyXWalkResourceClient", "Load Failed:" + description);
            view.reload(XWalkView.RELOAD_NORMAL);
            if(loading != null) {
                loading.setVisibility(GONE);
                view.setVisibility(GONE);
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
            Log.e("deneme", ""+enterFullscreen);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, String url) {
            super.onPageLoadStarted(view, url);
            if(loading != null) {
                loading.setVisibility(VISIBLE);
            }
            isLoadingFinished = false;
            view.setVisibility(GONE);
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);
            if( loading != null) {
                loading.setVisibility(GONE);
            }
            isLoadingFinished = true;
            view.setVisibility(VISIBLE);
        }
    }

    public WebViewAnnotationWithCrosswalk(Context context, LinkInfoExternal lie, CustomPulseProgress loading) {
        super(context);

        this.loading = loading;
        this.linkInfoExternal = lie;
        this.context = context;

        setAlpha(0.9999f); // alpha set edilmedigi zaman view siyah gorunuyor. cozum ariyorum (MG).
        setBackgroundColor(Color.TRANSPARENT);
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
        settings.setGeolocationEnabled(true);

        MyXUIClient uiClient = new MyXUIClient(this);
        setUIClient(uiClient);

        if(linkInfoExternal.mustHorizontalScrollLock()){
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float dx,dy;
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        // Action DOWN
                        x1 = event.getX();
                        y1 = event.getY();
                        setPreviousMotionEvent(event);
                        isHorizontalScrolling = false;
                        if(isDummyAction){
                            return false;
                        }
                        else{
                            return true;
                        }

                    }
                    else if(event.getAction() == MotionEvent.ACTION_MOVE){
                        // Action MOVE
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2 - x1;
                        dy = y2 - y1;
                        if(Math.abs(dx) > 10 || Math.abs(dy) > 10){
                            if(!(Math.abs(dx) >  Math.abs(dy))) {
                                // vertical
                                isHorizontalScrolling = false;
                                return false;
                            }else {
                                // horizontal
                                isHorizontalScrolling = true;
                                if(getPreviousMotionEvent()!=null && getPreviousMotionEvent().getAction() != MotionEvent.ACTION_MOVE) {
                                    MotionEvent previousEvent = getPreviousMotionEvent();
                                    setPreviousMotionEvent(null);
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
                        if(isHorizontalScrolling){
                            isHorizontalScrolling = false;
                            event.setLocation(event.getX() + left, event.getY() + top); // Webview size is not equal to page size. Optimize the location for page.
                            readerView.onTouchEvent(event);
                            return true;
                        }
                        else{
                            if(getPreviousMotionEvent()!=null) {
                                MotionEvent previousEvent = getPreviousMotionEvent();
                                setPreviousMotionEvent(null);
                                isDummyAction = true;
                                onTouchEvent(previousEvent);
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
