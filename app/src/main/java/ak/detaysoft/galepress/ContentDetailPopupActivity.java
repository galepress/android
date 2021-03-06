package ak.detaysoft.galepress;

import android.animation.Animator;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomDownloadButton;
import ak.detaysoft.galepress.util.CustomPulseProgress;
import ak.detaysoft.galepress.util.ProgressWheel;

/**
 * Created by p1025 on 27.03.2015.
 */
public class ContentDetailPopupActivity extends Activity{


    public LinearLayout baseView;
    public PopupFixedAspectLayout popup;
    public TextView nameLabel;
    public TextView descriptionLabel;
    public ImageView descriptionIcon;
    public TextView monthLabel;
    public L_Content content;
    public ImageView image;
    public Button updateButton;
    public Button viewButton;
    public Button deleteButton;
    public CustomDownloadButton downloadButton;
    public ProgressWheel progressBar;
    public ImageView overlay;
    public CustomPulseProgress loading;
    public Button cancelButton;
    public ContentHolder contentHolder;
    public boolean isFirstOpen;
    private float animationStartX, animationStartY;

    private final static int BILLING_RESPONSE_RESULT_OK = 0;
    private final static int RESULT_USER_CANCELED = 1;
    private final static int RESULT_BILLING_UNAVAILABLE = 3;
    private final static int RESULT_ITEM_UNAVAILABLE = 4;
    private final static int RESULT_DEVELOPER_ERROR = 5;
    private final static int RESULT_ERROR = 6;
    private final static int RESULT_ITEM_ALREADY_OWNED = 7;
    private final static int RESULT_ITEM_NOT_OWNED = 8; //For consumable product
    private boolean isFinishActionStart = false;

    private int searchPage = -1;
    private String searchQuery;

    private boolean isDescriptionShowing = true;
    private LinearLayout descriptionBase;
    private float descriptionTopYClose;
    private float descriptionTopYOpen;
    float touchDownY = 0;
    float touchUpY = 0;

    public class ContentHolder{
        Button updateButton;
        Button viewButton;
        Button deleteButton;
        CustomDownloadButton downloadButton;
        ImageView overlay;
        ProgressWheel progressBar;
        Button cancelButton;
        public ContentHolder(Button updateButton,
                             Button viewButton,
                             Button deleteButton,
                             CustomDownloadButton downloadButton,
                             ProgressWheel progressBar,
                             ImageView overlay,
                             Button cancelButton){
            this.updateButton = updateButton;
            this.viewButton = viewButton;
            this.deleteButton = deleteButton;
            this.downloadButton = downloadButton;
            this.progressBar = progressBar;
            this.overlay = overlay;
            this.cancelButton = cancelButton;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if(savedInstanceState != null){
                this.content = (L_Content)savedInstanceState.getSerializable("savedContent");
                isFirstOpen = false;
            } else {
                if(this.content == null){
                    this.content = (L_Content) getIntent().getSerializableExtra("content");
                    isFirstOpen = true;
                } else {
                    isFirstOpen = false;
                }
            }

        } catch (Exception e){
            finish();
        }

        Intent intent = getIntent();
        if(intent.hasExtra("animationStartX") && intent.hasExtra("animationStartY")){
            animationStartX = getIntent().getExtras().getFloat("animationStartX");
            animationStartY = getIntent().getExtras().getFloat("animationStartY");
        }
        else{
            animationStartX = 0.5f;
            animationStartY = 0.5f;
        }

        if(intent.hasExtra("searchPage")) {
            searchPage = intent.getExtras().getInt("searchPage", -1);
            searchQuery = intent.getExtras().getString("searchQuery", "");
        }

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        GalePressApplication.getInstance().setContentDetailPopupActivity(this);
        setContentView(R.layout.fixed_content_popup_layout);

        //set background colors
        baseView = (LinearLayout) findViewById(R.id.content_detail_baseview);
        baseView.setBackgroundColor(ApplicationThemeColor.getInstance().getTransperentPopupColor());
        baseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFinishActionStart)
                    finishActivityWithAnimation();

            }
        });

        popup = (PopupFixedAspectLayout) findViewById(R.id.content_detail_popup);
        popup.setBackgroundColor(Color.TRANSPARENT);
        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });

        ((LinearLayout)findViewById(R.id.content_popup_button_layer)).setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());

        //content name ve detail gibi text detaylarinin oldugu layer icin radius
        GradientDrawable gradient =  new GradientDrawable();
        gradient.setCornerRadii(new float[]{0,0,0,0, 2,2,2,2});
        gradient.setColor(ApplicationThemeColor.getInstance().getCoverImageBackgroundColor());
        gradient.setStroke(0, ApplicationThemeColor.getInstance().getCoverImageBackgroundColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)findViewById(R.id.content_detail_layer)).setBackground(gradient);
        else
            ((RelativeLayout)findViewById(R.id.content_detail_layer)).setBackgroundDrawable(gradient);

        //imagein oldugu layer icin radius
        GradientDrawable gradientForCoverImage =  new GradientDrawable();
        gradientForCoverImage.setCornerRadii(new float[]{2,2,2,2, 0,0,0,0});
        gradientForCoverImage.setColor(ApplicationThemeColor.getInstance().getLightCoverImageBackgroundColor());
        gradientForCoverImage.setStroke(0, ApplicationThemeColor.getInstance().getLightCoverImageBackgroundColor());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((RelativeLayout)findViewById(R.id.custom_cover_image_view)).setBackground(gradientForCoverImage);
        else
            ((RelativeLayout)findViewById(R.id.custom_cover_image_view)).setBackgroundDrawable(gradientForCoverImage);

        baseView.getRootView().setBackgroundColor(Color.TRANSPARENT);

        //setText
        nameLabel = (TextView)findViewById(R.id.content_detail_name_label);
        nameLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikMedium(this));
        nameLabel.setTextColor(ApplicationThemeColor.getInstance().getPopupTextColor());
        nameLabel.setText(content.getName());

        monthLabel = (TextView)findViewById(R.id.content_detail_month_label);
        monthLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikRegular(this));
        monthLabel.setTextColor(ApplicationThemeColor.getInstance().getPopupTextColorWithAlpha(50));
        monthLabel.setText(content.getMonthlyName());

        //setButtons
        viewButton = (Button)findViewById(R.id.content_detail_view);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            viewButton.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.READ_CONTENT));
        else
            viewButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.READ_CONTENT));
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(content != null && content.isPdfDownloaded())
                    viewContent();
            }
        });

        updateButton = (Button)findViewById(R.id.content_detail_update);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            updateButton.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.UPDATE_CONTENT));
        else
            updateButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.UPDATE_CONTENT));
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataApi.isConnectedToInternet()){
                    v.setEnabled(false);
                    v.setVisibility(View.GONE);
                    GalePressApplication.getInstance().getDataApi().getPdf(content, ContentDetailPopupActivity.this);
                }
            }
        });

        deleteButton = (Button)findViewById(R.id.content_detail_delete);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            deleteButton.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.DELETE_CONTENT));
        else
            deleteButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.DELETE_CONTENT));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                GalePressApplication.getInstance().getDataApi().deletePdf(content.getId(), ContentDetailPopupActivity.this);
            }
        });

        downloadButton = (CustomDownloadButton)findViewById(R.id.content_detail_download);

        initDownloadButton();
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataApi.isConnectedToInternet()){

                    if(content.isBuyable()) {
                        if(content.isContentBought() || GalePressApplication.getInstance().isUserHaveActiveSubscription()) {
                            if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                    || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)){
                                downloadButton.startAnim();
                            }
                            GalePressApplication.getInstance().getDataApi().getPdf(content, ContentDetailPopupActivity.this);
                        } else {
/*
                            * Login olmayan kullanici urun alamaz
                            * */
                            if(GalePressApplication.getInstance().getUserInformation() != null
                                    && GalePressApplication.getInstance().getUserInformation().getAccessToken() != null
                                    && GalePressApplication.getInstance().getUserInformation().getAccessToken().length() != 0){
                                if (!GalePressApplication.getInstance().isBlnBind() && GalePressApplication.getInstance().getmService() == null) {
                                    Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }

                                try {
                                    downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
                                    Bundle buyIntentBundle = GalePressApplication.getInstance().getmService().getBuyIntent(3, getPackageName(),
                                            content.getIdentifier(), "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                                    if (buyIntentBundle.getInt("RESPONSE_CODE") == BILLING_RESPONSE_RESULT_OK) { // Urun satin alinmamis
                                        // Start purchase flow (this brings up the Google Play UI).
                                        // Result will be delivered through onActivityResult().
                                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                                Integer.valueOf(0));
                                    } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ITEM_ALREADY_OWNED){ // Urun daha once alinmis
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                                                .show();
                                        if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                                || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)){
                                            downloadButton.startAnim();
                                        }
                                        GalePressApplication.getInstance().getDataApi().getPdf(content, ContentDetailPopupActivity.this);
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_USER_CANCELED){ // Hata var
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                                                .show();
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_BILLING_UNAVAILABLE){ // Hata var
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                                                .show();
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ITEM_UNAVAILABLE){ // Hata var
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                                                .show();
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } else if (buyIntentBundle.getInt("RESPONSE_CODE") == RESULT_ERROR){ // Hata var
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                                                .show();
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    } else { //  Beklenmedik Hata var
                                        Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                                                .show();
                                        downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                    }

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                    downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                    downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                } catch (Exception e){
                                    e.printStackTrace();
                                    downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
                                }
                            } else {
                                //Giris yapin uyarisi
                                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.login_warning_inapp_billing), Toast.LENGTH_SHORT)
                                        .show();
                                /*
                                * Mainactivity onActivityResult da logine yonlendirme yapacagiz
                                * */
                                Intent intent = ContentDetailPopupActivity.this.getIntent();
                                setResult(103, intent);
                                finish();
                            }
                        }
                    } else {
                        if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                                || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)){
                            downloadButton.startAnim();
                        }
                        GalePressApplication.getInstance().getDataApi().getPdf(content, ContentDetailPopupActivity.this);
                    }
                }
            }
        });

        cancelButton = (Button)findViewById(R.id.content_detail_cancel);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            cancelButton.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        else
            cancelButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.CANCEL_CONTENT_DOWNLOAD));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                GalePressApplication.getInstance().getDataApi().cancelDownload(false, ContentDetailPopupActivity.this, content);
            }
        });


        //progress
        progressBar = (ProgressWheel)findViewById(R.id.content_detail_progress_bar);
        overlay = (ImageView)findViewById(R.id.content_detail_download_overlay);
        overlay.setBackgroundColor(ApplicationThemeColor.getInstance().getDownloadProgressColorWithAlpha(70));

        progressBar.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        progressBar.setBarColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        progressBar.setRimColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(20));
        progressBar.setContourColor(Color.TRANSPARENT);

        loading = (CustomPulseProgress)findViewById(R.id.popup_image_loading);
        loading.startAnim();

        descriptionBase = (LinearLayout) findViewById(R.id.description_base);
        if(content.getDetail() == null || content.getDetail().length() == 0){
            descriptionBase.setVisibility(View.GONE);
        }
        descriptionBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float threshold = (descriptionTopYClose - descriptionTopYOpen)/4;
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        touchDownY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(isDescriptionShowing) {
                            float y = descriptionTopYOpen + event.getRawY()- touchDownY;
                            if(y >= descriptionTopYOpen && y <= descriptionTopYClose)
                                descriptionBase.setY(y);
                        } else {
                            float y = descriptionTopYClose + event.getRawY()- touchDownY;
                            if(y >= descriptionTopYOpen && y <= descriptionTopYClose)
                                descriptionBase.setY(y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        touchUpY = event.getRawY();

                        if(touchDownY > touchUpY) {
                            if(touchDownY - touchUpY >= threshold) {
                                descriptionBase.setY(descriptionTopYOpen);
                                isDescriptionShowing = true;
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                    descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                                else
                                    descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                            } else {
                                descriptionBase.setY(descriptionTopYClose);
                                isDescriptionShowing = false;
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                    descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                else
                                    descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                            }
                        } else if(touchDownY < touchUpY) {
                            if(touchUpY - touchDownY >= threshold) {
                                descriptionBase.setY(descriptionTopYClose);
                                isDescriptionShowing = false;
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                    descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                else
                                    descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                            } else {
                                descriptionBase.setY(descriptionTopYOpen);
                                isDescriptionShowing = true;
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                    descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                                else
                                    descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                            }
                        } else {

                            if(isDescriptionShowing) {
                                descriptionBase.animate().y(descriptionTopYClose).setInterpolator(new AccelerateInterpolator()).setDuration(500).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        descriptionBase.setEnabled(false);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        descriptionBase.setEnabled(true);
                                        isDescriptionShowing = false;
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                            descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                        else
                                            descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            } else {
                                descriptionBase.animate().y(descriptionTopYOpen).setInterpolator(new AccelerateInterpolator()).setDuration(500).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        descriptionBase.setEnabled(false);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        descriptionBase.setEnabled(true);
                                        isDescriptionShowing = true;
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                            descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                                        else
                                            descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            }
                        }


                        break;
                }
                return true;
            }
        });

        descriptionLabel = (TextView)findViewById(R.id.content_detail_description_label);
        descriptionLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(this));
        descriptionLabel.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        descriptionLabel.setBackgroundColor(ApplicationThemeColor.getInstance().getPoupDescritionColorWithAlpha(90));
        descriptionLabel.setText(content.getDetail());

        descriptionIcon = ((ImageView)findViewById(R.id.popup_swipe_icon));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));
        else
            descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this, ApplicationThemeColor.POPUP_DESCRIPTION_CLOSE));

        update();

        //setImage
        image = (ImageView)findViewById(R.id.content_detail_image);

        File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), content.getBigCoverImageFileName());

        if(coverImageFile.exists()){
            displayImage(false, false, image, loading, "file://"+coverImageFile.getPath());
        } else if(content.getLargeCoverImageDownloadPath() != null){
            if(content.getSmallCoverImageDownloadPath() != null){
                File thumnailFile = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());
                if(thumnailFile.exists()){
                    displayImage(false, true, image, loading, "file://"+thumnailFile.getPath());
                } else {
                    displayImage(true, false, image, loading, content.getLargeCoverImageDownloadPath());
                }
            }
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(content != null && content.isPdfDownloaded())
                    viewContent();
            }
        });

        contentHolder = new ContentHolder(updateButton,
                viewButton,
                deleteButton,
                downloadButton,
                progressBar,
                overlay,
                cancelButton);

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
                    scaleLast.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(content.getDetail() != null && content.getDetail().length() > 0){
                                descriptionTopYOpen = descriptionBase.getY();
                                isDescriptionShowing = false;
                                descriptionTopYClose = descriptionTopYOpen + descriptionBase.getHeight()-findViewById(R.id.popup_swipe_open).getHeight();

                                descriptionBase.animate().y(descriptionTopYClose).setInterpolator(new AccelerateInterpolator()).setDuration(750).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        descriptionBase.setEnabled(false);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        descriptionBase.setEnabled(true);
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                            descriptionIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                        else
                                            descriptionIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(ContentDetailPopupActivity.this, ApplicationThemeColor.POPUP_DESCRIPTION_OPEN));
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    popup.startAnimation(scaleLast);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            popup.startAnimation(scale);
        }
    }

    private void initDownloadButton(){


        if(content.isBuyable()) {
            if(content.isContentBought() || GalePressApplication.getInstance().isUserHaveActiveSubscription()) {
                downloadButton.init(CustomDownloadButton.RESTORE_PURCHASED_DOWNLOAD, "");
            } else {
                AsyncTask<Void, Void ,String> getPrice = new AsyncTask<Void, Void, String>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        downloadButton.init(CustomDownloadButton.PURCHASE_DOWNLOAD, "");
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        String price = "";
                        if(GalePressApplication.getInstance().isUserHaveActiveSubscription() || GalePressApplication.getInstance().getmService() == null){
                            return price;
                        } else {
                            //Satin alinabilen urunse fiyati kontrol ediliyor
                            ArrayList<String> skuList = new ArrayList<String>();
                            skuList.add(content.getIdentifier());
                            Bundle querySkus = new Bundle();
                            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                            Bundle skuDetails;
                            try {
                                skuDetails = GalePressApplication.getInstance().getmService().getSkuDetails(3, getPackageName(), "inapp", querySkus);

                                int response = skuDetails.getInt("RESPONSE_CODE");

                                if (response == 0) {
                                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                                    if (responseList.size() != 0) {
                                        for (String thisResponse : responseList) {
                                            JSONObject object = null;
                                            try {
                                                object = new JSONObject(thisResponse);
                                                price = object.getString("price");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                price = "";
                            }
                            if (price == null || price.length() == 0) {
                                price = (content.getMarketPrice() == null || content.getMarketPrice().length() == 0) ? "" : content.getMarketPrice();
                            }
                        }
                        return price;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        if(s.compareTo("") != 0)
                            downloadButton.getPriceTextView().setText(s);
                        else {
                            Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.product_price_error), Toast.LENGTH_SHORT).show();
                            downloadButton.getPriceTextView().setText(content.getPrice());
                        }
                        //downloadButton.getPriceTextView().setText("12.99 TL");
                        downloadButton.invalidate();
                    }
                };
                getPrice.execute();
            }
        } else {
            downloadButton.init(CustomDownloadButton.FREE_DOWNLOAD, "");
        }

    }

    private void displayImage(final boolean isDownload, final boolean isThumnail, final ImageView image, final CustomPulseProgress loading, String imagePath) {
        DisplayImageOptions displayConfig;
        if (isThumnail) {
            displayConfig = new DisplayImageOptions.Builder()
                    .showImageOnFail(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INTERNET_CONNECTION_ERROR))
                    .cacheInMemory(true).build();
        } else {
            displayConfig = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).build();
        }
        ImageLoader.getInstance().displayImage(imagePath, image, displayConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                loading.setVisibility(View.GONE);
                if(!isDownload && isThumnail)
                    loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                loading.setVisibility(View.GONE);
                if(isDownload) {
                    GalePressApplication.getInstance().getDataApi().saveImage(bitmap, content.getBigCoverImageFileName());
                }
                if(isThumnail && GalePressApplication.getInstance().getDataApi().isConnectedToInternet()) {
                    displayImage(true, false, image, loading, content.getLargeCoverImageDownloadPath());
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void finishActivityWithAnimation(){

        ScaleAnimation scale = new ScaleAnimation(1f, 1.05f, 1f, 1.05f, Animation.RELATIVE_TO_SELF, animationStartX, Animation.RELATIVE_TO_SELF, animationStartY);
        scale.setFillAfter(true);
        scale.setDuration(100);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFinishActionStart = true;
            }
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
                        /*
                        * Eger indirme islemi devame diyorsa library ve downloads ekranlari update ediliyor.
                        * */
                        if (GalePressApplication.getInstance().getMainActivity() != null && GalePressApplication.getInstance().getDataApi().downloadPdfTask != null && (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING)){
                            if(GalePressApplication.getInstance().getMainActivity().getLibraryFragment() != null) {
                                LibraryFragment libraryFragment = GalePressApplication.getInstance().getMainActivity().getLibraryFragment();
                                libraryFragment.updateGridView();
                            }

                            if(GalePressApplication.getInstance().getMainActivity().getDownloadedLibraryFragment() != null) {
                                LibraryFragment libraryFragment = GalePressApplication.getInstance().getMainActivity().getDownloadedLibraryFragment();
                                libraryFragment.updateGridView();
                            }
                        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 101) {
            if (resultCode == 101) {
                Intent intent = getIntent();
                intent.putExtra("SelectedTab", data.getIntExtra("SelectedTab", 0));
                setResult(101, intent);
                finish();
            }
        } else if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
                try {
                    Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESPONSE_RESULT_OK), Toast.LENGTH_SHORT)
                            .show();

                    JSONObject jo = new JSONObject(purchaseData);
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, ContentDetailPopupActivity.this);
                }
                catch (JSONException e) {
                    Toast.makeText(ContentDetailPopupActivity.this, "act result json parse error - "+e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if(resultCode == RESULT_OK && responseCode == RESULT_ITEM_ALREADY_OWNED){

                try {
                    Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_ITEM_ALREADY_OWNED), Toast.LENGTH_SHORT)
                            .show();

                    JSONObject jo = new JSONObject(purchaseData);
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.purchase_validation_checking));
                    progress.setCancelable(false);
                    progress.show();
                    GalePressApplication.getInstance().getDataApi().sendReceipt(jo.getString("productId"), jo.getString("purchaseToken"), jo.getString("packageName"), progress, ContentDetailPopupActivity.this);
                }
                catch (JSONException e) {
                    Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (responseCode == RESULT_USER_CANCELED){ // Hata var
                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_USER_CANCELED), Toast.LENGTH_SHORT)
                        .show();
                downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == RESULT_BILLING_UNAVAILABLE){ // Hata var
                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_BILLING_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == RESULT_ITEM_UNAVAILABLE){ // Hata var
                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLIN_RESULT_ITEM_UNAVAILABLE), Toast.LENGTH_SHORT)
                        .show();
                downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else if (responseCode == RESULT_ERROR){ // Hata var
                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_RESULT_ERROR), Toast.LENGTH_SHORT)
                        .show();
                downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            } else { //  Beklenmedik Hata var
                Toast.makeText(ContentDetailPopupActivity.this, ContentDetailPopupActivity.this.getResources().getString(R.string.BILLING_UNEXPECTED), Toast.LENGTH_SHORT)
                        .show();
                downloadButton.getPriceTextView().setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (content != null)
            outState.putSerializable("savedContent", content);
        super.onSaveInstanceState(outState);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isFinishActionStart && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    finishActivityWithAnimation();
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public void update(){
        boolean downloaded = content.isPdfDownloaded();
        boolean updateAvailable = content.isPdfUpdateAvailable();
        boolean downloading = content.isPdfDownloading()
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask !=null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content !=null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content.getId().compareTo(content.getId()) == 0;

        //Cancel butonu aktif oldugu her durumda download butonunun animasyonunu durdurmak icin
        if(cancelButton.getVisibility() == View.VISIBLE){
            downloadButton.stopAnim();
        }

        if(downloaded){
            // Content is downloaded and ready to view.
            downloadButton.setVisibility(View.GONE);

            viewButton.setVisibility(View.VISIBLE);
            viewButton.setEnabled(true);

            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setEnabled(true);

            progressBar.setVisibility(View.INVISIBLE);
            overlay.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.GONE);
            cancelButton.setEnabled(false);
            downloadButton.stopAnim();

            if(updateAvailable){
                updateButton.setVisibility(View.VISIBLE);
                updateButton.setEnabled(true);

                if(downloading){
                    // update downloading
                    updateButton.setVisibility(View.GONE);
                    viewButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                    cancelButton.setEnabled(true);
                    cancelButton.setVisibility(View.VISIBLE);
                    downloadButton.stopAnim();
                    progressBar.setVisibility(View.VISIBLE);
                    overlay.setVisibility(View.VISIBLE);
                    updateButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }
            }
            else{
                // update not available
                updateButton.setVisibility(View.GONE);
            }
        }
        else{
            // not downloaded
            if(downloading){
                //Content is not downloaded but downloading
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.setEnabled(true);
                progressBar.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE);
                downloadButton.setEnabled(false);
                downloadButton.setVisibility(View.GONE);
                downloadButton.stopAnim();
                updateButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                viewButton.setVisibility(View.GONE);
            }
            else{
                // Content Download edilmemis. ilk acildigi durum.
                downloadButton.setVisibility(View.VISIBLE);
                downloadButton.setEnabled(true);
                deleteButton.setVisibility(View.GONE);
                updateButton.setVisibility(View.GONE);
                viewButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                overlay.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.GONE);
                cancelButton.setEnabled(false);
            }
        }

        if(viewButton.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
            overlay.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.GONE);
            cancelButton.setEnabled(false);
            downloadButton.stopAnim();
        }
        baseView.invalidate();
    }

    public void progressUpdate(long total, long fileLength){
        if(progressBar.getVisibility() != View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
        }

        if(overlay.getVisibility() != View.VISIBLE){
           overlay.setVisibility(View.VISIBLE);
        }
        progressBar.setProgress((int)((total*360)/fileLength));
        progressBar.setText("%"+(int) (total * 100 / fileLength));
    }

    public L_Content getContent() {
        return content;
    }

    public void setContent(L_Content content) {
        this.content = content;
    }

    public void completePurchase(){
        Toast.makeText(this, getResources().getString(R.string.purchase_validation_success), Toast.LENGTH_LONG).show();
        content.setOwnedProduct(true);
        GalePressApplication.getInstance().getDataApi().getDatabaseApi().updateContent(content, false);
        if (GalePressApplication.getInstance().getDataApi().downloadPdfTask == null
                || (GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() != AsyncTask.Status.RUNNING)){
            downloadButton.startAnim();
        }
        GalePressApplication.getInstance().getDataApi().getPdf(content, ContentDetailPopupActivity.this);
    }


    public void viewContent(){
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
            Intent intent = new Intent(this, MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("searchPage", searchPage);
            intent.putExtra("searchQuery", searchQuery);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivityForResult(intent, 101);
            GalePressApplication.getInstance().getDataApi().updateApplication();
        }
    }
}
