package ak.detaysoft.galepress.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ak.detaysoft.galepress.R;

/**
 * Created by p1025 on 29.07.2015.
 */
public class CustomDownloadButton extends RelativeLayout {

    private ImageView arrowIcon;
    private ImageView downloadIcon;
    private TextView priceTextView;
    private TranslateAnimation moveDown;
    private TranslateAnimation moveUp;
    private Context context;

    private int type = 0;
    public final static int FREE_DOWNLOAD = 0;
    public final static int PURCHASE_DOWNLOAD = 1;
    public final static int RESTORE_PURCHASED = 2;

    public CustomDownloadButton(Context context) {
        super(context);
        this.context = context;
    }

    public CustomDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void init(int typ, String price){
        this.type = typ;
        int defaultWith = 0;

        if(this.type != PURCHASE_DOWNLOAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_BUTTON_BACKGROUND));
            else
                setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_BUTTON_BACKGROUND));
        }

        if(this.type == PURCHASE_DOWNLOAD){
            RelativeLayout.LayoutParams downloadButtonParams = (RelativeLayout.LayoutParams)getLayoutParams();
            defaultWith = downloadButtonParams.width;
            downloadButtonParams.width = downloadButtonParams.width * 3;  //Burada width tipe göre belirlenecek
            setLayoutParams(downloadButtonParams);
        }

        if(this.type == PURCHASE_DOWNLOAD){
            priceTextView  = new TextView(context);
            RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
            priceParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            priceTextView.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(context));
            priceTextView.setTextColor(ApplicationThemeColor.getInstance().getPopupTextColor());
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, context.getResources().getDimension(R.dimen.content_popup_small_textsize));
            priceTextView.setId(R.id.price_text);
            priceTextView.setLayoutParams(priceParams);
            priceTextView.setBackgroundColor(Color.TRANSPARENT);
            priceTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            priceTextView.setText(price);
            this.addView(priceTextView);
        }

        arrowIcon = new ImageView(context);
        RelativeLayout.LayoutParams img1Params;
        if(this.type == FREE_DOWNLOAD){
            img1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img1Params.addRule(RelativeLayout.ALIGN_TOP);
        } else if(this.type == PURCHASE_DOWNLOAD){
            img1Params = new RelativeLayout.LayoutParams(defaultWith,LayoutParams.MATCH_PARENT);
            img1Params.setMargins(0,0,-10,0);
            img1Params.addRule(RelativeLayout.LEFT_OF, priceTextView.getId());
        } else {
            img1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img1Params.addRule(RelativeLayout.ALIGN_TOP);
        }
        arrowIcon.setLayoutParams(img1Params);
        if(this.type == FREE_DOWNLOAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                arrowIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE_ARROW));
            else
                arrowIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE_ARROW));
        } else if(type == RESTORE_PURCHASED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                arrowIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD_ARROW));
            else
                arrowIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD_ARROW));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                arrowIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_ARROW));
            else
                arrowIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_ARROW));
        }

        this.addView(arrowIcon);


        if(this.type == FREE_DOWNLOAD || type == RESTORE_PURCHASED){
            downloadIcon = new ImageView(context);
            RelativeLayout.LayoutParams img2Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img2Params.addRule(RelativeLayout.ALIGN_TOP);
            downloadIcon.setLayoutParams(img2Params);
            if(this.type == FREE_DOWNLOAD){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    downloadIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE));
                else
                    downloadIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    downloadIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD));
                else
                    downloadIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD));
            }
            this.addView(downloadIcon);
        }

    }

    public void startAnim(){

        int fromY = -5;
        int toY = -5;

        if(type == RESTORE_PURCHASED){
            fromY = 5;
            toY = 5;
        }

        moveUp = new TranslateAnimation(0,0,0,toY);
        moveUp.setDuration(400);
        moveUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                arrowIcon.startAnimation(moveDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        moveDown = new TranslateAnimation(0,0,fromY,0);
        moveDown.setDuration(400);
        moveDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                arrowIcon.startAnimation(moveUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(type == PURCHASE_DOWNLOAD){
            arrowIcon.startAnimation(moveDown);
        } else if(type == RESTORE_PURCHASED){
            arrowIcon.startAnimation(moveDown);
        } else {
            arrowIcon.startAnimation(moveUp);
        }


    }

    public void stopAnim(){
        arrowIcon.clearAnimation();
    }
}
