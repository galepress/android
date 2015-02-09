package ak.detaysoft.galepress;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import com.artifex.mupdfdemo.MuPDFActivity;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    public ContentHolderAdapter contentHolderAdapter;
    public GridView gridview;
    private LayoutInflater layoutInflater;
    private boolean isOnlyDownloaded;
    private List contents;
    public String searchQuery = new String("");
    L_Category selectedCategory = null;

    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            isOnlyDownloaded = this.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG)==0;
        }catch (NullPointerException exception){
            isOnlyDownloaded = false;
        }
        super.onCreate(savedInstanceState);
        if(((MainActivity)this.getActivity()).content_id !=null){
            viewContent(GalePressApplication.getInstance().getDatabaseApi().getContent(((MainActivity)this.getActivity()).content_id));
            ((MainActivity)this.getActivity()).content_id = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        GalePressApplication.getInstance().setLibraryActivity(this);
        View v = inflater.inflate(R.layout.library_layout, container, false);

        Button categoriesButton = (Button)getActivity().findViewById(R.id.categories_button);
        categoriesButton.setVisibility(View.VISIBLE);
        SearchView searchView = (SearchView)getActivity().findViewById(R.id.search_view);
        searchView.setVisibility(View.VISIBLE);

        GalePressApplication.getInstance().getDataApi().updateApplication();
        gridview = (GridView) v.findViewById(R.id.gridview);
        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents(isOnlyDownloaded, searchQuery, selectedCategory);

        this.contentHolderAdapter = new ContentHolderAdapter(this, contents);
        gridview.setAdapter(this.contentHolderAdapter);

        updateGridView();

        return v;
    }
    public void updateGridView(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                contentHolderAdapter.contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents(isOnlyDownloaded,searchQuery,selectedCategory);
                contentHolderAdapter.notifyDataSetChanged();
                gridview.invalidateViews();
            }
        });

    }

    public ContentHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }

    public void viewContent(L_Content content){
        File samplePdfFile = new File(content.getPdfPath(),"file.pdf");
        if(content!=null && content.isPdfDownloaded() && samplePdfFile.exists()){

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, content.getId(), location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, null, dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_contentOpened, null,null,null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }


}
