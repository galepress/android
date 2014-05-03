package ak.detaysoft.galepress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import com.artifex.mupdfdemo.MuPDFActivity;
import java.io.File;
import java.util.List;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;

/**
 * Created by adem on 31/03/14.
 */
public class LibraryFragment extends Fragment {
    static private File downloadsDirectory;
    static private File samplePdfFile;
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
            MainActivity activity = (MainActivity)getActivity();
            isOnlyDownloaded = this.getTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG)==0;
        }catch (NullPointerException exception){
            isOnlyDownloaded = false;
        }
        GalePressApplication.getInstance().setLibraryActivity(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setLayoutInflater(inflater);
        View v = inflater.inflate(R.layout.library_layout, container, false);
        Button tv = (Button) v.findViewById(R.id.sync_button);
        tv.setText("Reload UI");

        GalePressApplication.getInstance().getDataApi().updateApplication();
        gridview = (GridView) v.findViewById(R.id.gridview);
        contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents(isOnlyDownloaded, searchQuery, selectedCategory);

        this.contentHolderAdapter = new ContentHolderAdapter(this, contents);
        gridview.setAdapter(this.contentHolderAdapter);

        Button syncButton = (Button) v.findViewById(R.id.sync_button);
        syncButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGridView();
            }
        });
        updateGridView();

        return v;
    }
    public void updateGridView(){
        contentHolderAdapter.contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents(isOnlyDownloaded,this.searchQuery,this.selectedCategory);
        gridview.invalidateViews();
        Logout.e("Adem","Gridview updated");
    }

    public ContentHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }

    public void viewContent(L_Content content){
        File samplePdfFile = new File(content.getPdfPath(),"file.pdf");
        if(content!=null && content.isPdfDownloaded() && samplePdfFile.exists()){
            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
//            intent.putExtra("Content", content);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }


}
