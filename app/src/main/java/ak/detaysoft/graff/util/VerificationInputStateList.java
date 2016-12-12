package ak.detaysoft.graff.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by p1025 on 18.04.2016.
 */
public class VerificationInputStateList extends StateListDrawable {
    public VerificationInputStateList(Drawable res, Drawable selectedRes) {
        super();
        addState(new int[]{android.R.attr.state_pressed}, selectedRes);
        addState(new int[]{android.R.attr.state_selected}, selectedRes);
        addState(new int[]{android.R.attr.state_enabled}, res);
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
            setColorFilter(ApplicationThemeColor.getInstance().getDarkThemeColorFilter());
        }
        else {
            setColorFilter(ApplicationThemeColor.getInstance().getLightThemeColorFilter());
        }
        return super.onStateChange(stateSet);
    }
}
