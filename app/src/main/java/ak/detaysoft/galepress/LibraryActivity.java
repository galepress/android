package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;


import com.artifex.mupdfdemo.MuPDFActivity;

import java.io.File;
import java.util.List;

import ak.detaysoft.galepress.database_models.L_Content;

public class LibraryActivity extends Activity {
    static private File downloadsDirectory;
    static private File samplePdfFile;
    public ContentHolderAdapter contentHolderAdapter;
    public GridView gridview;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return (super.onCreateOptionsMenu(menu));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GalePressApplication.getInstance().getDataApi().updateApplication();
        GalePressApplication.getInstance().setLibraryActivity(this);
        setContentView(R.layout.activity_main);
        if (downloadsDirectory == null) {
            downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            samplePdfFile = new File(downloadsDirectory + "/20.pdf");
        }

        gridview = (GridView) findViewById(R.id.gridview);
        List contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents();
        this.contentHolderAdapter = new ContentHolderAdapter(this, contents);
        gridview.setAdapter(this.contentHolderAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (samplePdfFile.exists()) {
                    Log.v("Adem", "Opening pdf at location :" + samplePdfFile.getAbsolutePath());
                    Toast.makeText(LibraryActivity.this, "Button Clicked", Toast.LENGTH_LONG).show();
                    Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
                    Intent intent = new Intent(LibraryActivity.this, MuPDFActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(uri);

                    ContentHolderAdapter contentHolderAdapter =  (ContentHolderAdapter)parent.getAdapter();
                    Toast.makeText(LibraryActivity.this, "Content Name : "+((L_Content)contentHolderAdapter.contents.get(position)).getName(), Toast.LENGTH_SHORT).show();

                    GalePressApplication.getInstance().getDataApi().updateApplication();
//                    startActivity(intent);

                } else {
                    Log.e("Adem", "PDF doesn't exist in location :" + samplePdfFile.getAbsolutePath());
//                    Toast.makeText(LibraryActivity.this, "PDF Not Exist at Location:" + samplePdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
                ContentHolderAdapter contentHolderAdapter =  (ContentHolderAdapter)parent.getAdapter();
                Toast.makeText(LibraryActivity.this, "Content Name : "+((L_Content)contentHolderAdapter.contents.get(position)).getName(), Toast.LENGTH_SHORT).show();
                GalePressApplication.getInstance().getDataApi().updateApplication();
            }
        });

        Button syncButton = (Button) findViewById(R.id.sync_button);
        syncButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGridView();
            }
        });
    }

    public void updateGridView(){
        contentHolderAdapter.contents = GalePressApplication.getInstance().getDatabaseApi().getAllContents();
        gridview.invalidateViews();
    }

    public ContentHolderAdapter getContentHolderAdapter() {
        return contentHolderAdapter;
    }


}
