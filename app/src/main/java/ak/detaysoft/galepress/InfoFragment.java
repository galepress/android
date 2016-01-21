package ak.detaysoft.galepress;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by adem on 02/04/14.
 */
public class InfoFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_page_layout, container, false);
        ((MainActivity)this.getActivity()).prepareActionBarForCustomTab(null, false, false);
        GalePressApplication.getInstance().setCurrentFragment(this);
        v.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        return v;
    }

}
