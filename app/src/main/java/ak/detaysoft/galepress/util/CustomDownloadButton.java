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
public class CustomDownloadButton extends RelativeLayout {

    private ImageView img1;
    private ImageView img2;
    private TextView priceTextView;
    private TranslateAnimation moveBottom;
    private TranslateAnimation moveTop;
    private Context context;

    private int type = 0;
    public final static int DOWNLOAD = 0;
    public final static int PURCHASE = 1;
    public final static int DOWNLOAD_PURCHASED = 2;

    public CustomDownloadButton(Context context) {
        super(context);
        this.context = context;
        //init();
    }

    public CustomDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //init();
    }

    public void init(int type, String price){

        this.type = type;
        int defaultWith = 0;

        if(type == PURCHASE){
            RelativeLayout.LayoutParams downloadButtonParams = (RelativeLayout.LayoutParams)getLayoutParams();
            defaultWith = downloadButtonParams.width;
            downloadButtonParams.width = downloadButtonParams.width * 3;  //Burada width tipe göre belirlenecek
            setLayoutParams(downloadButtonParams);
        }

        if(type == PURCHASE){
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

        img1 = new ImageView(context);
        RelativeLayout.LayoutParams img1Params;
        if(type == DOWNLOAD){
            img1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img1Params.addRule(RelativeLayout.ALIGN_TOP);
        } else if(type == PURCHASE){
            img1Params = new RelativeLayout.LayoutParams(defaultWith,LayoutParams.MATCH_PARENT);
            img1Params.addRule(RelativeLayout.LEFT_OF, priceTextView.getId());
        } else {
            img1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img1Params.addRule(RelativeLayout.ALIGN_TOP);
        }
        img1.setLayoutParams(img1Params);
        if(type == DOWNLOAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                img1.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG1));
            else
                img1.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG1));
        } else if(type == DOWNLOAD_PURCHASED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                img1.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_DOWNLOAD_PURCHASED_OK));
            else
                img1.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_DOWNLOAD_PURCHASED_OK));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                img1.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_OK));
            else
                img1.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_PURCHASE_OK));
        }

        this.addView(img1);


        if(type == DOWNLOAD || type == DOWNLOAD_PURCHASED){
            img2 = new ImageView(context);
            RelativeLayout.LayoutParams img2Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            img2Params.addRule(RelativeLayout.ALIGN_TOP);
            img2.setLayoutParams(img2Params);
            if(type == DOWNLOAD){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    img2.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG2));
                else
                    img2.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG2));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    img2.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_DOWNLOAD_PURCHASED));
                else
                    img2.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_DOWNLOAD_PURCHASED));
            }
            this.addView(img2);
        }

    }

    public void startAnim(){

        int fromY = -5;
        int toY = -5;

        if(type == DOWNLOAD_PURCHASED){
            fromY = 5;
            toY = 5;
        }

        moveTop = new TranslateAnimation(0,0,0,toY);
        moveTop.setDuration(400);
        moveTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img1.startAnimation(moveBottom);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        moveBottom = new TranslateAnimation(0,0,fromY,0);
        moveBottom.setDuration(400);
        moveBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img1.startAnimation(moveTop);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(type == PURCHASE){
            img1.startAnimation(moveBottom);
        } else if(type == DOWNLOAD_PURCHASED){
            img1.startAnimation(moveBottom);
        } else {
            img1.startAnimation(moveTop);
        }


    }

    public void stopAnim(){
        img1.clearAnimation();
    }
}
