package ak.detaysoft.galepress.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by p1025 on 15.01.2016.
 */
public class ReaderTabbarStateList extends StateListDrawable {

    private boolean isActive;

    public ReaderTabbarStateList(boolean isActive, Drawable res, Drawable selectedRes){
        super();
        this.isActive = isActive;
        if (isActive) {
            addState(new int[]{android.R.attr.state_selected}, selectedRes);
            addState(new int[] { android.R.attr.state_pressed }, selectedRes);
            addState(new int[] { android.R.attr.state_enabled }, res);
        }
        else {
            addState(new int[] { android.R.attr.state_pressed }, res);
            addState(new int[]{android.R.attr.state_selected}, res);
            addState(new int[] { android.R.attr.state_enabled }, selectedRes);
        }

    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean isClicked = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_selected || state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }

        if(isActive) {
            if (isClicked) {
                setColorFilter(ApplicationThemeColor.getInstance().getForegroundColorFilter());
            }
            else {
                setColorFilter(ApplicationThemeColor.getInstance().getForeGroundColorFilterWithAlpha((float)0.5));
            }
        } else {
            if (isClicked) {
                setColorFilter(ApplicationThemeColor.getInstance().getForeGroundColorFilterWithAlpha((float)0.5));
            }
            else {
                setColorFilter(ApplicationThemeColor.getInstance().getForegroundColorFilter());
            }
        }



        return super.onStateChange(stateSet);
    }

}
