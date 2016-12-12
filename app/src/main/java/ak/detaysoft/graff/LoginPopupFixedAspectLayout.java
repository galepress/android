package ak.detaysoft.graff;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by p1025 on 31.08.2015.
 */
public class LoginPopupFixedAspectLayout extends LinearLayout {

    private float aspect = 1.0f;
    int width;
    int height;

    public LoginPopupFixedAspectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        width = size.x;
        height = size.y;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FixedLoginPopup);
        aspect = a.getFloat(R.styleable.FixedLoginPopup_aspectRatioLogin, 1.50f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /*float padding = 50;

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if(w > h){
            h = (int)(w*aspect);
        } else {
            w -= padding;
            h = (int)((w)*aspect);
            w = (int)(h/aspect);
        } */

        int w = (width*10)/13;
        int h = (height*10)/14;



        super.onMeasure(
                MeasureSpec.makeMeasureSpec(w,
                        MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(h,
                        MeasureSpec.getMode(heightMeasureSpec)));
    }
}
