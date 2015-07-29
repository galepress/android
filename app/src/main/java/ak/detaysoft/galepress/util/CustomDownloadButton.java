package ak.detaysoft.galepress.util;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by p1025 on 29.07.2015.
 */
public class CustomDownloadButton extends RelativeLayout {

    private ImageView img1;
    private ImageView img2;
    private Context context;
    private TranslateAnimation moveBottom;
    private TranslateAnimation moveTop;

    public CustomDownloadButton(Context context) {
        super(context);
        init(context);
    }

    public CustomDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){

        this.context = context;

        img1 = new ImageView(context);
        RelativeLayout.LayoutParams img1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        img1Params.addRule(RelativeLayout.ALIGN_TOP);
        img1.setLayoutParams(img1Params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            img1.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG1));
        else
            img1.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG1));
        this.addView(img1);


        img2 = new ImageView(context);
        RelativeLayout.LayoutParams img2Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        img2Params.addRule(RelativeLayout.ALIGN_TOP);
        img2.setLayoutParams(img2Params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            img2.setBackground(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG2));
        else
            img2.setBackgroundDrawable(ApplicationThemeColor.getInstance().getPopupButtonDrawable(this.context, ApplicationThemeColor.DOWNLOAD_CONTENT_IMG2));
        this.addView(img2);
    }

    public void startAnim(){
        moveTop = new TranslateAnimation(0,0,0,-5);
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
        img1.startAnimation(moveTop);


        moveBottom = new TranslateAnimation(0,0,-5,0);
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
    }

    public void stopAnim(){
        img1.clearAnimation();
    }
}
