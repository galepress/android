package ak.detaysoft.galepress;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by adem on 15/04/14.
 */
public class AK_WebViewClient extends WebViewClient {
    @Override
    // show the web page in webview but not in web browser
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
