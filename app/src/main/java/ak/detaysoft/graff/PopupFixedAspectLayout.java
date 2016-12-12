package ak.detaysoft.graff;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by p1025 on 16.04.2015.
 */
public class PopupFixedAspectLayout extends FrameLayout {

    private float aspect = 1.0f;

    // .. alternative constructors omitted

    public PopupFixedAspectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PopupFixedAspectLayout);
        aspect = a.getFloat(R.styleable.PopupFixedAspectLayout_aspectRatio, 1.3333f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /*
        * Boyutu degistirmek icin values klasorlerindeki(farklı ekranlara gore farkli degerler kullaniliyor) "content_popup_padding" degerinin degistirilmesi yeterli.
        * Bu deger ekranın sagindaki ve solundaki boslukları ayarlıyor. Width ayarlandıktan sonra height, aspectRatio oranina gore asagida ayarlanıyor (MG)
        * */

        float pxh = (float)(getResources().getDimension(R.dimen.content_popup_detail)
                + getResources().getDimension(R.dimen.content_popup_detail)
                + getResources().getDimension(R.dimen.content_popup_layer_padding)
                + getResources().getDimension(R.dimen.content_popup_layer_padding));
        float padding = (float)getResources().getDimension(R.dimen.content_popup_padding);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if(w > h){
            h -=pxh;
            w = (int)(((h)/aspect) - padding);
            h = (int)(w*aspect);
            h += (pxh);
        } else {
            w -= padding;
            h = (int)((w)*aspect);
            w = (int)(h/aspect);
            h += (pxh);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(w,
                        MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(h,
                        MeasureSpec.getMode(heightMeasureSpec)));
    }
}