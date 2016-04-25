package ak.detaysoft.galepress;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

/**
 * Created by p1025 on 13.04.2016.
 */
public class VerficationBannerView extends LinearLayout {

    private float aspect = 1.0f;

    // .. alternative constructors omitted

    public VerficationBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.VerficationBannerView);
        aspect = a.getFloat(R.styleable.VerficationBannerView_aspectRatioBanner, (320f/740f));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = (int)(w*aspect);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(w,
                        MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(h,
                        MeasureSpec.getMode(heightMeasureSpec)));
    }
}
