package ak.detaysoft.galepress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by p1025 on 01.09.2015.
 */
public class LoginActivity extends Activity {

    private LinearLayout baseView;
    private LinearLayout popup;
    private float animationStartX, animationStartY;
    private boolean isFirstOpen = true;
    private EditText unameField;
    private EditText passwordField;
    private Button submit;
    private TextView forgot_password;
    private Button signup;

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ProgressDialog updateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            if(savedInstanceState != null){
                isFirstOpen = false;
            }

        } catch (Exception e){
            Log.e("Popup Content error", e.toString());
            finish();
        }


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

        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if(intent.hasExtra("animationStartX") && intent.hasExtra("animationStartY")){
            animationStartX = getIntent().getExtras().getFloat("animationStartX");
            animationStartY = getIntent().getExtras().getFloat("animationStartY");
        }
        else{
            animationStartX = 0.5f;
            animationStartY = 0.5f;
        }


        //set background colors
        baseView = (LinearLayout) findViewById(R.id.login_base_view);
        baseView.setBackgroundColor(ApplicationThemeColor.getInstance().getTransperentPopupColor());
        baseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityWithAnimation();
            }
        });

        popup = (LinearLayout) findViewById(R.id.login_custom_popup);
        GradientDrawable gradient =  new GradientDrawable();
        gradient.setCornerRadius(2);
        gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
        gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            popup.setBackground(gradient);
        else
            popup.setBackgroundDrawable(gradient);
        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });


        unameField = (EditText)findViewById(R.id.popup_login_username);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            unameField.setBackground(ApplicationThemeColor.getInstance().getPopupLoginInputDrawable(this));
        else
            unameField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupLoginInputDrawable(this));
        unameField.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ApplicationThemeColor.getInstance().paintIcons(this
                , ApplicationThemeColor.VIEWER_USERNAME_ACTIVE_INPUT_ICON), null);
        unameField.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        unameField.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        unameField.setHintTextColor(ApplicationThemeColor.getInstance().getThemeColor());

        passwordField = (EditText)findViewById(R.id.popup_login_password);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            passwordField.setBackground(ApplicationThemeColor.getInstance().getPopupLoginInputDrawable(this));
        else
            passwordField.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupLoginInputDrawable(this));
        passwordField.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ApplicationThemeColor.getInstance().paintIcons(this
                , ApplicationThemeColor.VIEWER_PASSWORD_ACTIVE_INPUT_ICON), null);
        passwordField.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        passwordField.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        passwordField.setHintTextColor(ApplicationThemeColor.getInstance().getThemeColor());

        submit = (Button)findViewById(R.id.popup_login_submit);
        submit.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        submit.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            submit.setBackground(ApplicationThemeColor.getInstance().getPopupLoginButtonDrawable(this));
        else
            submit.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupLoginButtonDrawable(this));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){


                    if (unameField.getText().length() == 0 || passwordField.getText().length() == 0) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                        alertDialog.setTitle(LoginActivity.this.getString(R.string.UYARI));
                        alertDialog.setMessage(LoginActivity.this.getString(R.string.user_information_missing));

                        alertDialog.setPositiveButton(LoginActivity.this.getString(R.string.TAMAM), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    } else {
                        updateDialog = ProgressDialog.show(LoginActivity.this, "",
                                LoginActivity.this.getString(R.string.user_information_check), true);
                        GalePressApplication.getInstance().getDataApi().login("","","","","",LoginActivity.this, false,
                                unameField.getText().toString(), GalePressApplication.getInstance().getMD5EncryptedValue(passwordField.getText().toString()));
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup = (Button)findViewById(R.id.login_popup_signup);
        signup.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        signup.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            signup.setBackground(ApplicationThemeColor.getInstance().getPopupLoginButtonDrawable(this));
        else
            signup.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupLoginButtonDrawable(this));
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
                    //openSignUpPopup();
                    openSigupActivity();
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }

            }
        });

        forgot_password = (TextView)findViewById(R.id.login_popup_forgot_password);
        forgot_password.setTextColor(createTextViewColorStateList());
        forgot_password.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GalePressApplication.getInstance().getDataApi().isConnectedToInternet()){
                    openPasswordActivity();
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton = (LoginButton) findViewById(R.id.facebook_popup_login_button);
        loginButton.setReadPermissions("user_friends", "email");
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {


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

                                    updateDialog = ProgressDialog.show(LoginActivity.this, "",
                                            LoginActivity.this.getString(R.string.user_information_check), true);
                                    GalePressApplication.getInstance().getDataApi().login(token, userId, email, name, lastName, LoginActivity.this, true,
                                            unameField.getText().toString(), GalePressApplication.getInstance().getMD5EncryptedValue(passwordField.getText().toString()));

                                } catch (JSONException e) {
                                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.WARNING_0), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.viewer_facebook_login_warning), Toast.LENGTH_SHORT).show();
            }
        });

        if(isFirstOpen){
            ScaleAnimation scale = new ScaleAnimation(0f, 1.05f, 0f, 1.05f, Animation.RELATIVE_TO_SELF, animationStartX, Animation.RELATIVE_TO_SELF, animationStartY);
            scale.setFillAfter(true);
            scale.setDuration(300);
            scale.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ScaleAnimation scaleLast = new ScaleAnimation(1.05f, 1f, 1.05f, 1f, Animation.RELATIVE_TO_SELF, animationStartX, Animation.RELATIVE_TO_SELF, animationStartY);
                    scaleLast.setFillAfter(true);
                    scaleLast.setDuration(100);
                    popup.startAnimation(scaleLast);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            popup.startAnimation(scale);
        }
    }

    public void customFailLoginWarning(String errorString){
        if(updateDialog != null)
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        if(errorString != null)
            Toast.makeText(LoginActivity.this, errorString, Toast.LENGTH_SHORT).show();
    }

    public void closeActivityAndUpdateApplication(){
        if(updateDialog != null){
            updateDialog.dismiss();
            finishActivityWithAnimation();
        }
    }

    public void internetConnectionWarning(){
        if(updateDialog != null)
            updateDialog.dismiss();
        LoginManager.getInstance().logOut();
        Toast.makeText(LoginActivity.this, getResources().getString(R.string.WARNING_1), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){ //signup
            if(resultCode == 101) //Kullanıcı oluşturma başarılı
                finishActivityWithAnimation();
        } else if(requestCode == 102) { //password

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishActivityWithAnimation();
    }

    private void finishActivityWithAnimation(){
        ScaleAnimation scale = new ScaleAnimation(1f, 1.05f, 1f, 1.05f, Animation.RELATIVE_TO_SELF, animationStartX, Animation.RELATIVE_TO_SELF, animationStartY);
        scale.setFillAfter(true);
        scale.setDuration(100);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {

                ScaleAnimation scaleLast = new ScaleAnimation(1.05f, 0f, 1.05f, 0f, Animation.RELATIVE_TO_SELF, animationStartX, Animation.RELATIVE_TO_SELF, animationStartY);
                scaleLast.setFillAfter(true);
                scaleLast.setDuration(300);
                scaleLast.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        baseView.setBackgroundColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        GalePressApplication.getInstance().setContentDetailPopupActivity(null);
                        Intent intent = getIntent();
                        setResult(102, intent);
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                AnimationSet animationSet = new AnimationSet(false);
                AlphaAnimation alpha = new AlphaAnimation(1,0);
                alpha.setDuration(300);
                alpha.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        popup.setAlpha(0);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                animationSet.addAnimation(scaleLast);
                animationSet.addAnimation(alpha);
                popup.startAnimation(animationSet);


            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        popup.startAnimation(scale);
    }

    private void openPasswordActivity(){
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        intent.putExtra("width", popup.getWidth());
        intent.putExtra("height", popup.getHeight());
        startActivityForResult(intent, 102);
    }

    private void openSigupActivity(){
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        intent.putExtra("width", popup.getWidth());
        intent.putExtra("height", popup.getHeight());
        startActivityForResult(intent, 101);
    }

    private ColorStateList createTextViewColorStateList(){
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {android.R.attr.state_selected},
                new int [] {}
        };

        int[] colors = new int[] {
                ApplicationThemeColor.getInstance().getReverseThemeColor(),
                ApplicationThemeColor.getInstance().getReverseThemeColor(),
                ApplicationThemeColor.getInstance().getReverseThemeColor(),
                ApplicationThemeColor.getInstance().getThemeColor()
        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

}
