package ak.detaysoft.galepress.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
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
public class CustomDownloadButton extends RelativeLayout{

    private ImageView arrowIcon;
    private ImageView downloadIcon;
    private TextView priceTextView;
    private TranslateAnimation moveDown;
    private TranslateAnimation moveUp;
    private Context context;

    private int type = 0;
    public final static int FREE = 0;
    public final static int PURCHASE = 1;
    public final static int RESTORE = 2;

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

        if(this.type == PURCHASE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_BUTTON_BACKGROUND));
            else
                setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_BUTTON_BACKGROUND));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_BUTTON_BACKGROUND));
            else
                setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_BUTTON_BACKGROUND));
        }

        if(this.type == PURCHASE){
            RelativeLayout.LayoutParams downloadButtonParams = (RelativeLayout.LayoutParams)getLayoutParams();
            defaultWith = downloadButtonParams.width;
            downloadButtonParams.width = downloadButtonParams.width * 3;  //Burada width tipe göre belirlenecek
            setLayoutParams(downloadButtonParams);
        }

        if(this.type == PURCHASE){
            priceTextView  = new TextView(context);
            priceTextView.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(context));
            priceTextView.setTextColor(ApplicationThemeColor.getInstance().downloadButtonPriceColorStateList());
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.content_popup_large_textsize));
            priceTextView.setId(R.id.price_text);
            priceTextView.setBackgroundColor(Color.TRANSPARENT);
            priceTextView.setGravity(Gravity.CENTER);
            priceTextView.setText(price);
            this.addView(priceTextView);
        }

        arrowIcon = new ImageView(context);
        arrowIcon.setId(R.id.arrow_icon);
        RelativeLayout.LayoutParams arrowIconParams;
        if(this.type == FREE){
            arrowIconParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            arrowIconParams.addRule(RelativeLayout.ALIGN_TOP);
        } else if(this.type == RESTORE){
            arrowIconParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            arrowIconParams.addRule(RelativeLayout.ALIGN_TOP);
        } else {
            arrowIconParams = new RelativeLayout.LayoutParams(defaultWith,LayoutParams.MATCH_PARENT);
            arrowIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        arrowIcon.setLayoutParams(arrowIconParams);

        if(this.type == FREE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                arrowIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE_ARROW));
            else
                arrowIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE_ARROW));
        } else if(type == RESTORE){
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

        downloadIcon = new ImageView(context);
        downloadIcon.setId(R.id.bottom_icon);
        RelativeLayout.LayoutParams downloadIconParams;
        if(this.type == FREE){
            downloadIconParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            downloadIconParams.addRule(RelativeLayout.ALIGN_TOP);
        } else if(this.type == RESTORE){
            downloadIconParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            downloadIconParams.addRule(RelativeLayout.ALIGN_TOP);
        } else {
            downloadIconParams = new RelativeLayout.LayoutParams(defaultWith,LayoutParams.MATCH_PARENT);
            downloadIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        downloadIcon.setLayoutParams(downloadIconParams);


        if(this.type == FREE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                downloadIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE));
            else
                downloadIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_FREE));
        } else if(this.type == RESTORE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                downloadIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD));
            else
                downloadIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_CLOUD));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                downloadIcon.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_BOTTOM));
            else
                downloadIcon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_BOTTOM));
        }
        this.addView(downloadIcon);


        if(this.type == PURCHASE){
            RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            priceParams.setMargins(0,0,-downloadIcon.getLayoutParams().width*3/10,0);
            priceParams.addRule(RelativeLayout.LEFT_OF, downloadIcon.getId());
            priceTextView.setLayoutParams(priceParams);
        }

    }

    public void startAnim(){

        int fromY = -5;
        int toY = -5;

        if(type == RESTORE){
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

        if(type == RESTORE){
            arrowIcon.startAnimation(moveDown);
        } else if(type == FREE){
            arrowIcon.startAnimation(moveUp);
        } else if(type == PURCHASE){
            arrowIcon.startAnimation(moveDown);
        }


    }

    public void stopAnim(){
        arrowIcon.clearAnimation();
    }

    public TextView getPriceTextView() {
        return priceTextView;
    }

    public ImageView getArrowIcon() {
        return arrowIcon;
    }
}