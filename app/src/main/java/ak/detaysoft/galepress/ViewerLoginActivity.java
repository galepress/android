package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.view.ProgressWheel;

/**
 * Created by p1025 on 11.05.2015.
 */
public class ViewerLoginActivity extends Activity {

    private EditText unameField;
    private EditText passwordField;
    private Button submit;
    private ProgressDialog updateDialog;
    public L_Content masterContent;
    private ProgressWheel pw_two;
    private FrameLayout pw_two_layout;
    private LinearLayout baseLayout;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String facebookEmail = "";
    private String facebookUserId = "";
    private String facebookToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GalePressApplication.getInstance().setCurrentActivity(this);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                }
        );

        setContentView(R.layout.viewer_login);

        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("user_friends", "email");
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                facebookToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {

                                try {
                                    facebookUserId = object.getString("id");
                                    facebookEmail = object.getString("email");
                                    GalePressApplication.getInstance().setTestApplicationLoginInf("", "", "0"
                                            ,facebookEmail, facebookUserId, false);
                                    updateDialog = ProgressDialog.show(ViewerLoginActivity.this, "",
                                            ViewerLoginActivity.this.getString(R.string.user_information_check), true);
                                    GalePressApplication.getInstance().getDataApi().getCustomerApplications(ViewerLoginActivity.this, true);
                                } catch (JSONException e) {
                                    Toast.makeText(ViewerLoginActivity.this, getResources().getString(R.string.WARNING_0), Toast.LENGTH_SHORT).show();
                                    LoginManager.getInstance().logOut();
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(ViewerLoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(ViewerLoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }
        });

        baseLayout = (LinearLayout)findViewById(R.id.login_base_layout);
        baseLayout.setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());

        pw_two = (ProgressWheel) findViewById(R.id.login_progressBarTwo);
        pw_two_layout = (FrameLayout) findViewById(R.id.login_proggress_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView)findViewById(R.id.login_logo)).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.VIEWER_LOGIN_LOGO));
        else
            ((ImageView)findViewById(R.id.login_logo)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.VIEWER_LOGIN_LOGO));

        unameField = (EditText)findViewById(R.id.login_uname);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            unameField.setBackground(ApplicationThemeColor.getInstance().getLoginInputDrawable(this));
        else
            unameField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLoginInputDrawable(this));
        unameField.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ApplicationThemeColor.getInstance().paintIcons(this
                , ApplicationThemeColor.VIEWER_USERNAME_ACTIVE_INPUT_ICON), null);
        unameField.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        unameField.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        unameField.setHintTextColor(ApplicationThemeColor.getInstance().getThemeColor());

        passwordField = (EditText)findViewById(R.id.login_password);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            passwordField.setBackground(ApplicationThemeColor.getInstance().getLoginInputDrawable(this));
        else
            passwordField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLoginInputDrawable(this));
        passwordField.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ApplicationThemeColor.getInstance().paintIcons(this
                , ApplicationThemeColor.VIEWER_PASSWORD_ACTIVE_INPUT_ICON), null);
        passwordField.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        passwordField.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        passwordField.setHintTextColor(ApplicationThemeColor.getInstance().getThemeColor());

        submit = (Button)findViewById(R.id.login_submit);
        submit.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        submit.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            submit.setBackground(ApplicationThemeColor.getInstance().getLoginButtonDrawable(this));
        else
            submit.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLoginButtonDrawable(this));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (unameField.getText().length() == 0 || passwordField.getText().length() == 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewerLoginActivity.this);
                    alertDialog.setTitle(ViewerLoginActivity.this.getString(R.string.UYARI));
                    alertDialog.setMessage(ViewerLoginActivity.this.getString(R.string.WARNING_3));

                    alertDialog.setPositiveButton(ViewerLoginActivity.this.getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    GalePressApplication.getInstance().setTestApplicationLoginInf(unameField.getText().toString(), passwordField.getText().toString(), "0"
                            ,facebookEmail, facebookUserId, false);
                    updateDialog = ProgressDialog.show(ViewerLoginActivity.this, "",
                            ViewerLoginActivity.this.getString(R.string.user_information_check), true);
                    GalePressApplication.getInstance().getDataApi().getCustomerApplications(ViewerLoginActivity.this, false);
                }
            }
        });
        masterContent = GalePressApplication.getInstance().getDataApi().getMasterContent();

    }

    public void updateActivity(){
        if(updateDialog != null)
            updateDialog.dismiss();
    }

    public void internetConnectionWarning(){
        if(updateDialog != null)
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        //Giris basarisiz olursa lokalde tutulan degerler sifirlaniyor.
        GalePressApplication.getInstance().setTestApplicationLoginInf("", "", "0"
                ,"", "", false);
        Toast.makeText(ViewerLoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
    }

    public void customWarning(String errorString){
        if(updateDialog != null)
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        //Giris basarisiz olursa lokalde tutulan degerler sifirlaniyor.
        GalePressApplication.getInstance().setTestApplicationLoginInf("", "", "0"
                ,"", "", false);
        if(errorString != null)
            Toast.makeText(ViewerLoginActivity.this, errorString, Toast.LENGTH_SHORT).show();
    }

    public void openMasterContent(){
        submit.setClickable(true);
        unameField.setEnabled(true);
        passwordField.setEnabled(true);
        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("content_id",masterContent.getId().toString());
        startActivity(i);

        finish();
    }

    public void openLibraryFragment(){
        submit.setClickable(true);
        unameField.setEnabled(true);
        passwordField.setEnabled(true);
        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void getKeyForFacebookLogin(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "ak.detaysoft.galepress",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.w("-----------KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GalePressApplication.getInstance().setCurrentActivity(this);
        if(GalePressApplication.getInstance().getDataApi() != null){
            if(GalePressApplication.getInstance().getDataApi().downloadPdfTask != null
                    && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.FINISHED){
                openMasterContent();
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

    public void startMasterDownload(){
        DataApi.DownloadPdfTask downloadPdfTask = GalePressApplication.getInstance().getDataApi().downloadPdfTask;
        if(!(downloadPdfTask!= null && downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)){
            Logout.e("Adem", "Start Master Downloaded");
            GalePressApplication.getInstance().getDataApi().downloadPdf(this.masterContent);

            pw_two_layout.setVisibility(View.VISIBLE);
            submit.setClickable(false);
            unameField.setEnabled(false);
            passwordField.setEnabled(false);
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
            if(pw_two_layout.getVisibility() != View.VISIBLE){
                pw_two_layout.setVisibility(View.VISIBLE);
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



}
