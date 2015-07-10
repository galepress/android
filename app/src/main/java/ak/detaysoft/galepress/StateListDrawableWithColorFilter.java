package ak.detaysoft.galepress;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 15.04.2015.
 */
public class StateListDrawableWithColorFilter extends StateListDrawable {

    public StateListDrawableWithColorFilter(boolean isSelected, Drawable res, Drawable selectedRes){
        super();
        if (isSelected)
            addState(new int[]{android.R.attr.state_selected}, selectedRes);
        else
            addState(new int[] { android.R.attr.state_pressed }, selectedRes);
        addState(new int[] { android.R.attr.state_enabled }, res);
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean isClicked = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_selected || state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }
        if (isClicked) {
            setColorFilter(ApplicationThemeColor.getInstance().getForeGroundColorFilterWithAlpha((float)0.5));
        }
        else {
            setColorFilter(ApplicationThemeColor.getInstance().getForegroundColorFilter());
        }

        return super.onStateChange(stateSet);
    }
}
