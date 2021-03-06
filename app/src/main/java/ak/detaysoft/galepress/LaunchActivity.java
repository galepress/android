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

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.util.SystemUiHider;
import ak.detaysoft.galepress.util.ProgressWheel;

import android.util.Log;
import android.view.View;

import com.facebook.FacebookSdk;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LaunchActivity extends Activity {
    ProgressWheel pw_two;
    public L_Content masterContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);
        pw_two = findViewById(R.id.progressBarTwo);
        pw_two.setVisibility(View.INVISIBLE);
        GalePressApplication.getInstance().setCurrentActivity(this);
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

        boolean openLogin = GalePressApplication.getInstance().isAgeVerificationActive()
                && !GalePressApplication.getInstance().isAgeVerificationSubmit()
                && !GalePressApplication.getInstance().isTestApplication();
        if(!openLogin) {
            Intent i = new Intent(this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("content_id", masterContent.getId().toString());
            startActivity(i);
            finish();
        } else {
            GalePressApplication.getInstance().setAgeVerificationSubmit(false);
            Intent intent = new Intent(LaunchActivity.this, UserLoginActivity.class);
            intent.putExtra("content_id", masterContent.getId().toString());
            intent.putExtra("action", UserLoginActivity.ACTION_OPEN_MASTER);
            intent.putExtra("isLaunchOpen", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void openLibraryFragment(){

        boolean openLogin = GalePressApplication.getInstance().isAgeVerificationActive()
                && !GalePressApplication.getInstance().isAgeVerificationSubmit()
                && !GalePressApplication.getInstance().isTestApplication();
        if(!openLogin) {
            Intent i = new Intent(this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else {
            Intent intent = new Intent(this, UserLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("action", UserLoginActivity.ACTION_OPEN_LIBRARY);
            intent.putExtra("isLaunchOpen", true);
            startActivity(intent);
            finish();
        }
    }

    public void startMasterDownload(){

        boolean openLogin = GalePressApplication.getInstance().isAgeVerificationActive()
                && !GalePressApplication.getInstance().isAgeVerificationSubmit()
                && !GalePressApplication.getInstance().isTestApplication();
        if(!openLogin) {
            DataApi.DownloadPdfTask downloadPdfTask = GalePressApplication.getInstance().getDataApi().downloadPdfTask;
            if(!(downloadPdfTask!= null && downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)){
                Logout.e("Galepress", "Start Master Downloaded");
                GalePressApplication.getInstance().getDataApi().downloadPdf(this.masterContent);

                pw_two.setVisibility(View.VISIBLE);
                ShapeDrawable bg = new ShapeDrawable(new RectShape());
                int[] pixels = new int[] { 0xFF2E9121, 0xFF2E9121, 0xFF2E9121,
                        0xFF2E9121, 0xFF2E9121, 0xFF2E9121, 0xFFFFFFFF, 0xFFFFFFFF};
                Bitmap bm = Bitmap.createBitmap(pixels, 8, 1, Bitmap.Config.ARGB_8888);
                Shader shader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                pw_two.resetCount();
            }
        } else {
            GalePressApplication.getInstance().setAgeVerificationSubmit(false);
            Intent intent = new Intent(LaunchActivity.this, UserLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("content_id", masterContent.getId().toString());
            intent.putExtra("action", UserLoginActivity.ACTION_DOWNLOAD_MASTER);
            intent.putExtra("isLaunchOpen", true);
            startActivity(intent);
            finish();

        }
    }

    public void progressUpdate(long total, long fileLength){
        Logout.e("Galepress", "Total : " + total + " file lenght: " + fileLength);
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
        Logout.e("Galepress", "Launch Activity onPostCreate calistirildi.");
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
