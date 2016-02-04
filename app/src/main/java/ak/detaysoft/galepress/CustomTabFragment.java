package ak.detaysoft.galepress;

/**
 * Created by p1025 on 21.05.2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import ak.detaysoft.galepress.web_views.BannerAndTabbarWebView;
import ak.detaysoft.galepress.web_views.BannerAndTabbarWebViewWithCrosswalk;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

public class CustomTabFragment extends Fragment {

    public BannerAndTabbarWebView tabbarWebView;
    public BannerAndTabbarWebViewWithCrosswalk tabbarWebViewWithCrosswalk;
    private ProgressBar progressBar;
    final int KITKAT = 19; // Android 4.4

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        GalePressApplication.getInstance().setCurrentWebFragment(this);
        GalePressApplication.getInstance().setCurrentFragment(this);
        //GalePressApplication.getInstance().getDataApi().updateApplication();

        RelativeLayout v = (RelativeLayout)inflater.inflate(R.layout.banner_and_tab_web_layout, container, false);
        v.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());

        progressBar = (ProgressBar)v.findViewById(R.id.custom_web_view_progress_bar);
        ((LinearLayout)progressBar.getParent()).setBackgroundColor(ApplicationThemeColor.getInstance().getTransperentPopupColor());

        if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
            tabbarWebView = new BannerAndTabbarWebView(this.getActivity(), progressBar,  false);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            tabbarWebView.setLayoutParams(params);
            if (savedInstanceState == null)
                tabbarWebView.loadUrl(GalePressApplication.getInstance().getTabList().get(Integer.parseInt(getTag())).getWebUrl());
            else
                tabbarWebView.restoreState(savedInstanceState);

            v.addView(tabbarWebView);

            ((LinearLayout)progressBar.getParent()).bringToFront();
            ((LinearLayout)progressBar.getParent()).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            ((MainActivity) this.getActivity()).prepareActionBarForCustomTab(tabbarWebView, true, false);
        } else {
            tabbarWebViewWithCrosswalk = new BannerAndTabbarWebViewWithCrosswalk(this.getActivity(), progressBar,  false);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            tabbarWebViewWithCrosswalk.setLayoutParams(params);
            if (savedInstanceState == null)
                tabbarWebViewWithCrosswalk.load(GalePressApplication.getInstance().getTabList().get(Integer.parseInt(getTag())).getWebUrl(), null);
            else
                tabbarWebView.restoreState(savedInstanceState);

            v.addView(tabbarWebViewWithCrosswalk);

            ((LinearLayout)progressBar.getParent()).bringToFront();
            ((LinearLayout)progressBar.getParent()).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            ((MainActivity) this.getActivity()).prepareActionBarForCustomTab(tabbarWebViewWithCrosswalk, true, false);
        }


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(tabbarWebView != null)
            tabbarWebView.saveState(outState);
    }

    public View getWebview() {

        if (android.os.Build.VERSION.SDK_INT >= KITKAT) {
            return tabbarWebView;
        } else {
            return tabbarWebViewWithCrosswalk;
        }

    }



}
