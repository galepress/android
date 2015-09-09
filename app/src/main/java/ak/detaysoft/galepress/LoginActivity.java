package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
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

import com.artifex.mupdfdemo.PageView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

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
    private String facebookEmail = "";
    private String facebookUserId = "";
    private String facebookToken;

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
                GalePressApplication.getInstance().prepareMemberShipList(true);
                finishActivityWithAnimation();
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
                openSignUpPopup();
            }
        });

        forgot_password = (TextView)findViewById(R.id.login_popup_forgot_password);
        forgot_password.setTextColor(createTextViewColorStateList());
        forgot_password.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(this));
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPasswordPopup();
            }
        });

        loginButton = (LoginButton) findViewById(R.id.facebook_popup_login_button);
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

                                    GalePressApplication.getInstance().prepareMemberShipList(true);
                                    finishActivityWithAnimation();

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

    private void openPasswordPopup(){
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.forgot_password_base_view);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.forgot_password_popup, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(this);
        popup.setContentView(layout);
        popup.setFocusable(true);

        Button close = (Button)layout.findViewById(R.id.forgot_web_close);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            close.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        else
            close.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((LinearLayout)close.getParent()).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));
        else
            ((LinearLayout)close.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));

        popup.setWidth(this.popup.getWidth()+ ((LinearLayout)close.getParent()).getLayoutParams().width - 8);
        popup.setHeight(this.popup.getHeight()+ ((LinearLayout)close.getParent()).getLayoutParams().height - 8);

        CustomPulseProgress progress = (CustomPulseProgress)layout.findViewById(R.id.forgot_loading);
        progress.startAnim();

        WebView web = (WebView)layout.findViewById(R.id.password_web);
        web.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams webBaseParams = new RelativeLayout.LayoutParams(this.popup.getWidth(), this.popup.getHeight());
        webBaseParams.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.forgot_popup_base);
        ((RelativeLayout)web.getParent()).setLayoutParams(webBaseParams);
        web.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        prepareWebSetting(web);

        GradientDrawable gradient =  new GradientDrawable();
        gradient.setCornerRadius(2);
        gradient.setColor(ApplicationThemeColor.getInstance().getThemeColor());
        gradient.setStroke(0, ApplicationThemeColor.getInstance().getThemeColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)web.getParent()).setBackground(gradient);
        else
            ((RelativeLayout)web.getParent()).setBackgroundDrawable(gradient);

        web.setWebViewClient(new MyWebViewClient(progress));

        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(false);

        web.loadUrl("http://m.facebook.com"); //Bu url suan dummy

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        int padding =  (this.popup.getHeight()+ (((LinearLayout)close.getParent()).getLayoutParams().width - 8)/2) ;
        popup.showAsDropDown(this.popup, -(((LinearLayout)close.getParent()).getLayoutParams().width - 8)/2, -padding);
    }

    private void openSignUpPopup(){
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.signup_base_view);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.signup_popup, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(this);
        popup.setContentView(layout);
        layout.setBackgroundColor(Color.TRANSPARENT);
        popup.setFocusable(true);

        Button close = (Button)layout.findViewById(R.id.signup_web_close);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            close.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        else
            close.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((LinearLayout)close.getParent()).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));
        else
            ((LinearLayout)close.getParent()).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.MEMBERSHIP_POPUP_CLOSE_BASE));

        popup.setWidth(this.popup.getWidth()+ ((LinearLayout)close.getParent()).getLayoutParams().width - 8);
        popup.setHeight(this.popup.getHeight()+ ((LinearLayout)close.getParent()).getLayoutParams().height - 8);

        CustomPulseProgress progress = (CustomPulseProgress)layout.findViewById(R.id.signup_loading);
        progress.startAnim();

        WebView web = (WebView)layout.findViewById(R.id.signup_web);
        web.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams webBaseParams = new RelativeLayout.LayoutParams(this.popup.getWidth(), this.popup.getHeight());
        webBaseParams.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.signup_popup_base);
        ((RelativeLayout)web.getParent()).setLayoutParams(webBaseParams);
        web.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        web.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        prepareWebSetting(web);

        GradientDrawable gradient =  new GradientDrawable();
        gradient.setCornerRadius(2);
        gradient.setColor(ApplicationThemeColor.getInstance().getThemeColor());
        gradient.setStroke(0, ApplicationThemeColor.getInstance().getThemeColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)web.getParent()).setBackground(gradient);
        else
            ((RelativeLayout)web.getParent()).setBackgroundDrawable(gradient);

        web.setWebViewClient(new MyWebViewClient(progress));
        web.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("deneme", consoleMessage.message());
                return true;
            }
        });

        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(false);

        web.loadUrl("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=NBA"); //Bu url suan dummy

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        int padding =  (this.popup.getHeight()+ (((LinearLayout)close.getParent()).getLayoutParams().width - 8)/2) ;
        popup.showAsDropDown(this.popup, -(((LinearLayout)close.getParent()).getLayoutParams().width - 8)/2, -padding);

    }

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

    private class MyWebViewClient extends WebViewClient {

        private CustomPulseProgress loading;

        public MyWebViewClient(CustomPulseProgress loading){
            this.loading = loading;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);

            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            view.setVisibility(View.VISIBLE);
            GradientDrawable gradient =  new GradientDrawable();
            gradient.setCornerRadius(2);
            gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
            gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)view.getParent()).setBackground(gradient);
            else
                ((RelativeLayout)view.getParent()).setBackgroundDrawable(gradient);
            loading.stopAnim();
            loading.setVisibility(View.GONE);

            /*AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try{
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = client.execute(request);

                        String html = "";
                        InputStream in = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder str = new StringBuilder();
                        String line = null;
                        while((line = reader.readLine()) != null)
                        {
                            str.append(line);
                        }
                        in.close();
                        html = str.toString();
                        Log.e("deneme", html);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    view.setVisibility(View.VISIBLE);
                    GradientDrawable gradient =  new GradientDrawable();
                    gradient.setCornerRadius(2);
                    gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
                    gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        ((RelativeLayout)view.getParent()).setBackground(gradient);
                    else
                        ((RelativeLayout)view.getParent()).setBackgroundDrawable(gradient);
                    loading.stopAnim();
                    loading.setVisibility(View.GONE);

                    super.onPostExecute(aVoid);
                }
            };
            asyncTask.execute();*/


        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loading.startAnim();
            loading.setVisibility(View.VISIBLE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //view.loadUrl("file:///android_asset/annotation_not_loaded.html");

            view.loadUrl("about:blank");
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            view.setVisibility(View.VISIBLE);
            GradientDrawable gradient =  new GradientDrawable();
            gradient.setCornerRadius(2);
            gradient.setColor(ApplicationThemeColor.getInstance().getForegroundColor());
            gradient.setStroke(0, ApplicationThemeColor.getInstance().getForegroundColor());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ((RelativeLayout)view.getParent()).setBackground(gradient);
            else
                ((RelativeLayout)view.getParent()).setBackgroundDrawable(gradient);
            loading.stopAnim();
            loading.setVisibility(View.GONE);
        }
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
