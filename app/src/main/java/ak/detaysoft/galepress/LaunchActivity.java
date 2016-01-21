package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.service_models.R_AppDetail;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.SystemUiHider;
import ak.detaysoft.galepress.view.ProgressWheel;

import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.facebook.FacebookSdk;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LaunchActivity extends ActionBarActivity {
    private SystemUiHider mSystemUiHider;
    boolean running;
    ProgressWheel pw_two;
    int progress = 0;
    public L_Content masterContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_launch);
        pw_two = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pw_two.setVisibility(View.INVISIBLE);
        GalePressApplication.getInstance().setCurrentActivity(this);

        if(GalePressApplication.getInstance().isTestApplication())
            FacebookSdk.sdkInitialize(this.getApplicationContext());

        if(GalePressApplication.getInstance().isTestApplication()){
            if(GalePressApplication.getInstance().getTestApplicationLoginInf().getUsername().isEmpty())
                openLoginActivity();
            else
                GalePressApplication.getInstance().getDataApi().updateApplication();
        } else {
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }

        masterContent = GalePressApplication.getInstance().getDataApi().getMasterContent();
    }

    public void openLoginActivity(){
        Intent i = new Intent(this,ViewerLoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void openMasterContent(){

        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("content_id",masterContent.getId().toString());
        startActivity(i);

        finish();
    }

    public void openLibraryFragment(){
        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void startMasterDownload(){
        DataApi.DownloadPdfTask downloadPdfTask = GalePressApplication.getInstance().getDataApi().downloadPdfTask;
        if(!(downloadPdfTask!= null && downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)){
            Logout.e("Adem", "Start Master Downloaded");
            GalePressApplication.getInstance().getDataApi().downloadPdf(this.masterContent);

            pw_two.setVisibility(View.VISIBLE);
            ShapeDrawable bg = new ShapeDrawable(new RectShape());
            int[] pixels = new int[] { 0xFF2E9121, 0xFF2E9121, 0xFF2E9121,
                    0xFF2E9121, 0xFF2E9121, 0xFF2E9121, 0xFFFFFFFF, 0xFFFFFFFF};
            Bitmap bm = Bitmap.createBitmap(pixels, 8, 1, Bitmap.Config.ARGB_8888);
            Shader shader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            pw_two.resetCount();
        }
    }

    public void progressUpdate(long total, long fileLength){
        Logout.e("Adem", "Total : "+total+" file lenght: "+fileLength);
        if(total == fileLength){
//            openMasterContent();
        }
        else{
            if(pw_two.getVisibility() != View.VISIBLE){
                pw_two.setVisibility(View.VISIBLE);
            }
            pw_two.setProgress((int)((total*360)/fileLength));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Logout.e("Adem","Launch Activity onPostCreate calistirildi.");
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }
    @Override
    protected void onResume() {
        super.onResume();
        GalePressApplication.getInstance().setCurrentActivity(this);
        if(GalePressApplication.getInstance().getDataApi() != null){
            if(masterContent != null){
                if(GalePressApplication.getInstance().getDataApi().downloadPdfTask != null
                        && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.FINISHED){
                    openMasterContent();
                }
            } else {
                if(GalePressApplication.getInstance().isTestApplication()){
                    if(GalePressApplication.getInstance().getTestApplicationLoginInf().getUsername().isEmpty())
                        openLoginActivity();
                    else
                        GalePressApplication.getInstance().getDataApi().updateApplication();
                } else {
                    GalePressApplication.getInstance().getDataApi().updateApplication();
                }
            }
        }
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }
    private void clearReferences(){
        Activity currActivity = GalePressApplication.getInstance().getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            GalePressApplication.getInstance().setCurrentActivity(null);
    }
}
