package ak.detaysoft.galepress;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
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

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;
import ak.detaysoft.galepress.util.ProgressWheel;

/**
 * Created by p1025 on 07.04.2016.
 */
public class UserLoginActivity extends Activity {


    public static int ACTION_OPEN_LIBRARY = 0;
    public static int ACTION_DOWNLOAD_MASTER = 1;
    public static int ACTION_OPEN_MASTER = 2;
    public static int ACTION_MENU = 3;
    private Integer content_id = null;
    private int action = 0;
    private boolean isLaunchOpen = true;
    private ProgressWheel pw_two;
    private FrameLayout pw_two_layout;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private boolean isLoginViewActive = true;
    private String facebookEmail = "";
    private String facebookUserId = "";
    private String facebookToken;
    private boolean isFirstInit = true;


    private EditText passwordField;
    private EditText unameField;
    private Button submit;
    private ProgressDialog updateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GalePressApplication.getInstance().setCurrentActivity(this);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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

        Intent intent = getIntent();
        if(intent.hasExtra("content_id")){
            this.content_id = Integer.valueOf(intent.getStringExtra("content_id"));
        }

        if(intent.hasExtra("action")){
            this.action = intent.getIntExtra("action", 0);
        }

        if(intent.hasExtra("isLaunchOpen")){
            this.isLaunchOpen = intent.getBooleanExtra("isLaunchOpen", true);
        }

        openLoginView();

    }

    public void openLoginView(){
        setContentView(R.layout.activity_verification_login);

        loginButton = (LoginButton) findViewById(R.id.verification_facebook_login);
        loginButton.setReadPermissions("user_friends", "email");
        // Callback registration

        //loginButton.performClick();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                updateDialog = ProgressDialog.show(UserLoginActivity.this, "",
                        UserLoginActivity.this.getString(R.string.user_information_check), true);

                facebookToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {

                                try {
                                    String userId = object.getString("id");
                                    String email = object.getString("email");
                                    String name = object.getString("first_name");
                                    String lastName = object.getString("last_name");
                                    String token = loginResult.getAccessToken().getToken();


                                    GalePressApplication.getInstance().getDataApi().login(token, userId, email, name, lastName, UserLoginActivity.this, true,
                                            unameField.getText().toString(), GalePressApplication.getInstance().getMD5EncryptedValue(passwordField.getText().toString()));

                                } catch (JSONException e) {
                                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.WARNING_0), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }
        });

        pw_two = (ProgressWheel) findViewById(R.id.verification_login_progressBarTwo);
        pw_two_layout = (FrameLayout) findViewById(R.id.verification_login_proggress_layout);

        unameField = (EditText)findViewById(R.id.verification_login_uname);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            unameField.setBackground(ApplicationThemeColor.getInstance().getVerificationLoginInputDrawable(this));
        else
            unameField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getVerificationLoginInputDrawable(this));
        unameField.setTextColor(ApplicationThemeColor.getInstance().defaultLightAlphaPressedDarkStateList());
        unameField.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        unameField.setHintTextColor(ApplicationThemeColor.getInstance().defaultLightAlphaPressedDarkStateList());


        passwordField = (EditText)findViewById(R.id.verification_login_password);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            passwordField.setBackground(ApplicationThemeColor.getInstance().getVerificationLoginInputDrawable(this));
        else
            passwordField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getVerificationLoginInputDrawable(this));
        passwordField.setTextColor(ApplicationThemeColor.getInstance().defaultLightAlphaPressedDarkStateList());
        passwordField.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        passwordField.setHintTextColor(ApplicationThemeColor.getInstance().defaultLightAlphaPressedDarkStateList());



        TextView forgot = ((TextView)findViewById(R.id.verification_forgot_password));
        forgot.setTextColor(ApplicationThemeColor.getInstance().defaultLightPressedDarkStateList());
        forgot.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
                    if(isLaunchOpen && GalePressApplication.getInstance().getAgeVerificationQuestion() != null && GalePressApplication.getInstance().getAgeVerificationQuestion().length() > 0) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                        alertDialog.setTitle(UserLoginActivity.this.getResources().getString(R.string.UYARI));
                        alertDialog.setMessage(GalePressApplication.getInstance().getAgeVerificationQuestion());

                        alertDialog.setPositiveButton(getString(R.string.EVET), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GalePressApplication.getInstance().setAgeVerificationSubmit(true);
                                openForgotView();


                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();
                    } else {
                        openForgotView();
                    }
                } else {
                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }


            }
        });

        final Button login = ((Button)findViewById(R.id.verification_login));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            login.setBackground(ApplicationThemeColor.getInstance().getVerificationLoginButtonDrawable(this));
        else
            login.setBackgroundDrawable(ApplicationThemeColor.getInstance().getVerificationLoginButtonDrawable(this));
        login.setTextColor(ApplicationThemeColor.getInstance().defaultLightPressedDarkStateList());
        login.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){

                    if (unameField.getText().length() == 0 || passwordField.getText().length() == 0) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                        alertDialog.setTitle(UserLoginActivity.this.getString(R.string.UYARI));
                        alertDialog.setMessage(UserLoginActivity.this.getString(R.string.user_information_missing));

                        alertDialog.setPositiveButton(UserLoginActivity.this.getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    } else {
                        if(isLaunchOpen && GalePressApplication.getInstance().getAgeVerificationQuestion() != null && GalePressApplication.getInstance().getAgeVerificationQuestion().length() > 0) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                            alertDialog.setTitle(UserLoginActivity.this.getResources().getString(R.string.UYARI));
                            alertDialog.setMessage(GalePressApplication.getInstance().getAgeVerificationQuestion());

                            alertDialog.setPositiveButton(getString(R.string.EVET), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    GalePressApplication.getInstance().setAgeVerificationSubmit(true);
                                    updateDialog = ProgressDialog.show(UserLoginActivity.this, "",
                                            UserLoginActivity.this.getString(R.string.user_information_check), true);
                                    GalePressApplication.getInstance().getDataApi().login("","","","","",UserLoginActivity.this, false,
                                            unameField.getText().toString(), GalePressApplication.getInstance().getMD5EncryptedValue(passwordField.getText().toString()));

                                }
                            });
                            alertDialog.setNegativeButton(getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        } else {
                            updateDialog = ProgressDialog.show(UserLoginActivity.this, "",
                                    UserLoginActivity.this.getString(R.string.user_information_check), true);
                            GalePressApplication.getInstance().getDataApi().login("","","","","",UserLoginActivity.this, false,
                                    unameField.getText().toString(), GalePressApplication.getInstance().getMD5EncryptedValue(passwordField.getText().toString()));
                        }
                    }
                } else {
                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }



            }
        });

        Button signup = ((Button)findViewById(R.id.verification_signup));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            signup.setBackground(ApplicationThemeColor.getInstance().getVerificationSignupButtonDrawable(this));
        else
            signup.setBackgroundDrawable(ApplicationThemeColor.getInstance().getVerificationSignupButtonDrawable(this));
        signup.setTextColor(ApplicationThemeColor.getInstance().defaultLightPressedDarkStateList());
        signup.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
                    if(isLaunchOpen && GalePressApplication.getInstance().getAgeVerificationQuestion() != null && GalePressApplication.getInstance().getAgeVerificationQuestion().length() > 0) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                        alertDialog.setTitle(UserLoginActivity.this.getResources().getString(R.string.UYARI));
                        alertDialog.setMessage(GalePressApplication.getInstance().getAgeVerificationQuestion());

                        alertDialog.setPositiveButton(getString(R.string.EVET), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GalePressApplication.getInstance().setAgeVerificationSubmit(true);
                                openSignupView();

                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();
                    } else {

                    }
                } else {
                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }

            }
        });

        TextView connect_with_txt = ((TextView)findViewById(R.id.verification_connect_with_txt));
        connect_with_txt.setTextColor(ApplicationThemeColor.getInstance().getLightThemeColor());
        connect_with_txt.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));

        TextView facebook_txt = ((TextView)findViewById(R.id.verification_facebook_txt));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            facebook_txt.setBackground(ApplicationThemeColor.getInstance().getVerificationFacebookButtonDrawable(this));
        else
            facebook_txt.setBackgroundDrawable(ApplicationThemeColor.getInstance().getVerificationFacebookButtonDrawable(this));
        facebook_txt.setTextColor(ApplicationThemeColor.getInstance().getLightThemeColor());
        facebook_txt.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));

        FrameLayout facebook = ((FrameLayout)findViewById(R.id.verification_facebook_button_base));
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isLaunchOpen && GalePressApplication.getInstance().getAgeVerificationQuestion() != null && GalePressApplication.getInstance().getAgeVerificationQuestion().length() > 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                    alertDialog.setTitle(UserLoginActivity.this.getResources().getString(R.string.UYARI));
                    alertDialog.setMessage(GalePressApplication.getInstance().getAgeVerificationQuestion());

                    alertDialog.setPositiveButton(getString(R.string.EVET), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            GalePressApplication.getInstance().setAgeVerificationSubmit(true);
                            loginButton.performClick();

                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                        }
                    });
                    alertDialog.show();
                } else {
                    loginButton.performClick();
                }

            }
        });

        TextView skip = ((TextView)findViewById(R.id.verification_skip));
        skip.setTextColor(ApplicationThemeColor.getInstance().defaultLightPressedDarkStateList());
        skip.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isLaunchOpen && GalePressApplication.getInstance().getAgeVerificationQuestion() != null && GalePressApplication.getInstance().getAgeVerificationQuestion().length() > 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserLoginActivity.this);
                    alertDialog.setTitle(UserLoginActivity.this.getResources().getString(R.string.UYARI));
                    alertDialog.setMessage(GalePressApplication.getInstance().getAgeVerificationQuestion());

                    alertDialog.setPositiveButton(getString(R.string.EVET), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            GalePressApplication.getInstance().setAgeVerificationSubmit(true);
                            runAction();

                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.HAYIR), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            GalePressApplication.getInstance().setAgeVerificationSubmit(false);
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                } else {
                    runAction();
                }

            }
        });

        if(!isLaunchOpen) {
            skip.setVisibility(View.GONE);
        }

        Button close = (Button)findViewById(R.id.verification_login_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLoginActivity.this.finish();
            }
        });

        if(isLaunchOpen) {
            close.setVisibility(View.GONE);
        }
    }

    public void openSignupView(){
        setContentView(R.layout.activity_verification_signup);
        isLoginViewActive = false;


        pw_two = (ProgressWheel) findViewById(R.id.verification_signup_progressBarTwo);
        pw_two_layout = (FrameLayout) findViewById(R.id.verification_signup_proggress_layout);

        Button close = (Button)findViewById(R.id.verification_signup_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginView();
            }
        });

        CustomPulseProgress progress = (CustomPulseProgress)findViewById(R.id.verification_signup_loading);
        progress.startAnim();

        WebView web = ((WebView)findViewById(R.id.verification_signup_webview));
        web.setVisibility(View.INVISIBLE);
        prepareWebSetting(web);

        web.setWebViewClient(new SignupWebViewClient(progress));
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }
        });

        Integer applicationID = 0;
        try{
            applicationID = new Integer(GalePressApplication.getInstance().getApplicationId());
        } catch (Exception e){
            Toast.makeText(this, getResources().getString(R.string.cannot_load), Toast.LENGTH_SHORT).show();
            openLoginView();
        }

        web.loadUrl("http://www.galepress.com/"+getResources().getString(R.string.language_code)+"/mobile-user/register/"+applicationID);
    }

    public void openForgotView(){
        setContentView(R.layout.activity_verification_forgot);
        isLoginViewActive = false;


        pw_two = (ProgressWheel) findViewById(R.id.verification_forgot_progressBarTwo);
        pw_two_layout = (FrameLayout) findViewById(R.id.verification_forgot_proggress_layout);

        Button close = (Button)findViewById(R.id.verification_forgot_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginView();
            }
        });

        CustomPulseProgress progress = (CustomPulseProgress)findViewById(R.id.verification_forgot_loading);
        progress.startAnim();

        WebView web = ((WebView)findViewById(R.id.verification_forgot_webview));
        web.setVisibility(View.INVISIBLE);
        prepareWebSetting(web);

        web.setWebViewClient(new ForgotWebViewClient(progress));
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }
        });

        Integer applicationID = 0;
        try{
            applicationID = new Integer(GalePressApplication.getInstance().getApplicationId());
        } catch (Exception e){
            Toast.makeText(this, getResources().getString(R.string.cannot_load), Toast.LENGTH_SHORT).show();
            openLoginView();
        }

        web.loadUrl("http://www.galepress.com/"+getResources().getString(R.string.language_code)+"/mobile-user/forgot-password/"+applicationID);
    }

    private void runAction(){
        if(action == ACTION_OPEN_LIBRARY) {
            openLibraryActivity();
        } else if(action == ACTION_OPEN_MASTER) {
            openMasterContent();
        } else if(action == ACTION_DOWNLOAD_MASTER) {
            startMasterDownload();
        } else if(action == ACTION_MENU) {
            leftMenuLogin();
        }
    }

    public void leftMenuLogin(){
        Intent intent = getIntent();
        setResult(102, intent);
        finish();
    }

    public void openMasterContent(){
        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("content_id", content_id.toString());
        startActivity(i);
        finish();
    }

    public void openLibraryActivity(){
        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void startMasterDownload(){

        DataApi.DownloadPdfTask downloadPdfTask = GalePressApplication.getInstance().getDataApi().downloadPdfTask;
        if(!(downloadPdfTask!= null && downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)){
            Logout.e("Galepress", "Start Master Downloaded");
            L_Content masterContent = GalePressApplication.getInstance().getDataApi().getMasterContent();
            GalePressApplication.getInstance().getDataApi().downloadPdf(masterContent);

            pw_two_layout.setVisibility(View.VISIBLE);
            ShapeDrawable bg = new ShapeDrawable(new RectShape());
            int[] pixels = new int[] { 0xFF2E9121, 0xFF2E9121, 0xFF2E9121,
                    0xFF2E9121, 0xFF2E9121, 0xFF2E9121, 0xFFFFFFFF, 0xFFFFFFFF};
            Bitmap bm = Bitmap.createBitmap(pixels, 8, 1, Bitmap.Config.ARGB_8888);
            Shader shader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            pw_two.resetCount();
        }
    }

    public void progressUpdate(long total, long fileLength){
        Logout.e("Galepress", "Total : "+total+" file lenght: "+fileLength);
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


    public void customFailLoginWarning(String errorString){
        if(updateDialog != null && updateDialog.isShowing())
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        if(errorString != null)
            Toast.makeText(UserLoginActivity.this, errorString, Toast.LENGTH_SHORT).show();
    }

    public void closeActivityAndUpdateApplication(){
        if(updateDialog != null && updateDialog.isShowing()){
            updateDialog.dismiss();
            runAction();
        }
    }

    public void internetConnectionWarning(){
        if(updateDialog != null && updateDialog.isShowing())
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void prepareWebSetting(WebView web){
        WebSettings s = web.getSettings();
        s.setBuiltInZoomControls(true);
        s.setPluginState(WebSettings.PluginState.ON);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);
    }

    private class SignupWebViewClient extends WebViewClient {

        private CustomPulseProgress loading;

        public SignupWebViewClient(CustomPulseProgress loading){
            this.loading = loading;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            view.setVisibility(View.VISIBLE);
            loading.stopAnim();
            loading.setVisibility(View.GONE);

            if(url.contains("usertoken")){
                JSONObject obj = new JSONObject();
                try {
                    if(url.indexOf("usertoken=") != -1){ // kayit-basarili?usertoken= üzerindede yapılabilir
                        String accessToken = url.substring(url.indexOf("usertoken=")+"usertoken=".length());
                        obj.put("accessToken", accessToken);
                        GalePressApplication.getInstance().editMemberShipList(true, obj);
                        updateDialog = ProgressDialog.show(UserLoginActivity.this, "",
                                UserLoginActivity.this.getString(R.string.user_information_check), true);
                        GalePressApplication.getInstance().restorePurchasedProductsFromMarket(true, UserLoginActivity.this, updateDialog);

                    } else {
                        Intent intent = getIntent();
                        setResult(102, intent);
                        finish();
                    }
                } catch (JSONException e) {
                    GalePressApplication.getInstance().editMemberShipList(false, null);
                    customFailLoginWarning(getResources().getString(R.string.WARNING_0));
                    openLoginView();
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.setVisibility(View.GONE);
            loading.startAnim();
            loading.setVisibility(View.VISIBLE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            loading.stopAnim();
            loading.setVisibility(View.GONE);
            openLoginView();

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }


    private class ForgotWebViewClient extends WebViewClient {

        private CustomPulseProgress loading;

        public ForgotWebViewClient(CustomPulseProgress loading){
            this.loading = loading;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            view.setVisibility(View.VISIBLE);
            loading.stopAnim();
            loading.setVisibility(View.GONE);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.setVisibility(View.GONE);
            loading.startAnim();
            loading.setVisibility(View.VISIBLE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            loading.stopAnim();
            loading.setVisibility(View.GONE);
            openLoginView();

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    protected void onResume() {
        GalePressApplication.getInstance().setCurrentActivity(this);
        super.onResume();
    }

    @Override
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if(isLoginViewActive) {
                        finish();
                        return true;
                    } else {
                        openLoginView();
                        return false;
                    }

            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public ProgressDialog getUpdateDialog() {
        return updateDialog;
    }

}
