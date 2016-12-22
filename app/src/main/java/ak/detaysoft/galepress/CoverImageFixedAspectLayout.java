package ak.detaysoft.galepress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 18.06.2015.
 */

public class CoverImageFixedAspectLayout extends RelativeLayout {

    private float aspect = 1.0f;

    // .. alternative constructors omitted

    public CoverImageFixedAspectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CoverImageFixedAspectLayout);
        aspect = a.getFloat(R.styleable.CoverImageFixedAspectLayout_aspectRatioCover, 0.75f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if(w > h){
            w = ((int)((h)*aspect));
            h = (int)(w/aspect);
        } else {
            h = (int)((w)/aspect);
            w = (int)(h*aspect);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(w,
                        MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(h,
                        MeasureSpec.getMode(heightMeasureSpec)));
    }
}
