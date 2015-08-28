package ak.detaysoft.galepress;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 29.06.2015.
 */
public class StateListDrawableForPopupButtons extends StateListDrawable {

    public StateListDrawableForPopupButtons(Drawable res, Drawable selectedRes) {
        super();
        addState(new int[]{android.R.attr.state_pressed}, selectedRes);
        addState(new int[]{android.R.attr.state_enabled}, res);
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        /*boolean isClicked = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_selected || state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }
        if (isClicked) {
            setColorFilter(ApplicationThemeColor.getInstance().getThemeColorFilter());
        }
        else {
            setColorFilter(ApplicationThemeColor.getInstance().getReverseThemeColorFilter());
        }*/
        return super.onStateChange(stateSet);
    }
}
