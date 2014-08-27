package ak.detaysoft.galepress;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by adem on 02/04/14.
 */
public class InfoPageFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Button categoriesButton = (Button)getActivity().findViewById(R.id.categories_button);
        categoriesButton.setVisibility(View.INVISIBLE);
        SearchView searchView = (SearchView)getActivity().findViewById(R.id.search_view);
        searchView.setVisibility(View.INVISIBLE);
        return inflater.inflate(R.layout.info_page_layout, container, false);
    }
}
