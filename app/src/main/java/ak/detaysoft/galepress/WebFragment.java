package ak.detaysoft.galepress;

/**
 * Created by p1025 on 21.05.2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

public class WebFragment extends Fragment {

    public CustomWebView customWebView;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        GalePressApplication.getInstance().setCurrentWebFragment(this);
        GalePressApplication.getInstance().setCurrentFragment(this);
        //GalePressApplication.getInstance().getDataApi().updateApplication();

        RelativeLayout v = (RelativeLayout)inflater.inflate(R.layout.custom_tab_web_layout, container, false);
        v.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());

        progressBar = (ProgressBar)v.findViewById(R.id.custom_web_view_progress_bar);
        ((LinearLayout)progressBar.getParent()).setBackgroundColor(ApplicationThemeColor.getInstance().getTransperentPopupColor());

        customWebView = new CustomWebView(this.getActivity(), progressBar,  true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        customWebView.setLayoutParams(params);
        if (savedInstanceState == null)
            customWebView.loadUrl(GalePressApplication.getInstance().getTabList().get(Integer.parseInt(getTag())).getWebUrl());
        else
            customWebView.restoreState(savedInstanceState);

        v.addView(customWebView);

        ((LinearLayout)progressBar.getParent()).bringToFront();

        ((MainActivity) this.getActivity()).prepareActionBarForCustomTab(customWebView, true, false);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(customWebView != null)
            customWebView.saveState(outState);
    }

    public CustomWebView getCustomWebView() {
        return customWebView;
    }



}
