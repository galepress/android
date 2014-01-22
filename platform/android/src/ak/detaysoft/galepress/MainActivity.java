package ak.detaysoft.galepress;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.artifex.mupdfdemo.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {
    static private File downloadsDirectory;
    static private File samplePdfFile;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return(super.onCreateOptionsMenu(menu));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(downloadsDirectory == null){
            downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            samplePdfFile = new File(downloadsDirectory+"/20.pdf");
        }

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ContentHolderAdapter(this,this));


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(samplePdfFile.exists()){
                    Log.v("PDF File","Openin pdf at location :"+samplePdfFile.getAbsolutePath());
                    Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_LONG).show();
                    Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
                    Intent intent = new Intent(MainActivity.this, MuPDFActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    startActivity(intent);
                }
                else{
                    Log.e("PDF FILE","PDF doesn't exist in location :"+samplePdfFile.getAbsolutePath());
                    Toast.makeText(MainActivity.this, "PDF Not Exist at Location:"+samplePdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
